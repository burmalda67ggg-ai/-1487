package com.example.vpn.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.TrafficStats
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.vpn.core.EngineRegistry
import com.example.vpn.data.AppDatabase
import com.example.vpn.data.SettingsRepository
import com.example.vpn.data.VpnRepository
import com.example.vpn.diagnostics.NetworkDiagnosticsService
import com.example.vpn.model.ConnectionState
import com.example.vpn.model.TrafficStatsSnapshot
import com.example.vpn.model.VpnProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

object VpnController {
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _currentProfile = MutableStateFlow<VpnProfile?>(null)
    val currentProfile: StateFlow<VpnProfile?> = _currentProfile.asStateFlow()

    private val _trafficStats = MutableStateFlow(TrafficStatsSnapshot())
    val trafficStats: StateFlow<TrafficStatsSnapshot> = _trafficStats.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    fun updateState(state: ConnectionState) {
        _connectionState.value = state
    }

    fun updateProfile(profile: VpnProfile?) {
        _currentProfile.value = profile
    }

    fun updateStats(stats: TrafficStatsSnapshot) {
        _trafficStats.value = stats
    }

    fun updateError(error: String?) {
        _lastError.value = error
    }

    fun startVpn(context: Context, profile: VpnProfile) {
        _lastError.value = null
        _currentProfile.value = profile
        _connectionState.value = ConnectionState.CONNECTING

        val intent = Intent(context, AppVpnService::class.java).apply {
            action = AppVpnService.ACTION_CONNECT
            putExtra(AppVpnService.EXTRA_PROFILE_ID, profile.id)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopVpn(context: Context) {
        _connectionState.value = ConnectionState.DISCONNECTING
        val intent = Intent(context, AppVpnService::class.java).apply {
            action = AppVpnService.ACTION_DISCONNECT
        }
        context.startService(intent)
    }
}

class AppVpnService : VpnService() {

    companion object {
        const val ACTION_CONNECT = "com.example.vpn.ACTION_CONNECT"
        const val ACTION_DISCONNECT = "com.example.vpn.ACTION_DISCONNECT"
        const val EXTRA_PROFILE_ID = "EXTRA_PROFILE_ID"
        private const val NOTIFICATION_CHANNEL_ID = "vpn_status_channel"
        private const val NOTIFICATION_ID = 1001
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var statsJob: Job? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private lateinit var settingsRepo: SettingsRepository
    private lateinit var vpnRepo: VpnRepository

    private var activePfd: ParcelFileDescriptor? = null
    private var currentProfile: VpnProfile? = null
    private var sessionStartTime = 0L
    private val rxBytesAccumulated = AtomicLong(0L)
    private val txBytesAccumulated = AtomicLong(0L)
    private var lastRxTotal = 0L
    private var lastTxTotal = 0L

    override fun onCreate() {
        super.onCreate()
        settingsRepo = SettingsRepository(applicationContext)
        val db = AppDatabase.getInstance(applicationContext)
        val diag = NetworkDiagnosticsService(applicationContext)
        vpnRepo = VpnRepository(db.vpnProfileDao(), db.connectionHistoryDao(), diag)

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val profileId = intent.getLongExtra(EXTRA_PROFILE_ID, -1L)
                serviceScope.launch {
                    val profile = if (profileId > 0) vpnRepo.getProfileById(profileId) else VpnController.currentProfile.value
                    if (profile != null) {
                        connectProfile(profile)
                    } else {
                        VpnController.updateError("Профиль VPN не найден")
                        VpnController.updateState(ConnectionState.ERROR)
                        stopSelf()
                    }
                }
            }
            ACTION_DISCONNECT -> {
                disconnectAndStop("Отключено пользователем")
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun connectProfile(profile: VpnProfile) {
        currentProfile = profile
        VpnController.updateProfile(profile)
        VpnController.updateState(ConnectionState.CONNECTING)

        startForeground(NOTIFICATION_ID, buildNotification("Подключение к ${profile.name}..."))

        val engine = EngineRegistry.getEngine(profile.protocol)
        val settings = settingsRepo.settings.value

        // Validate config
        val validation = engine.validate(profile)
        if (validation.isFailure) {
            val errorMsg = validation.exceptionOrNull()?.message ?: "Ошибка валидации конфигурации"
            VpnController.updateError(errorMsg)
            VpnController.updateState(ConnectionState.ERROR)
            vpnRepo.logConnection(
                serverName = profile.name,
                countryCode = profile.countryCode,
                protocol = profile.protocol.name,
                connectedAt = System.currentTimeMillis(),
                durationSeconds = 0L,
                bytesDownloaded = 0L,
                bytesUploaded = 0L,
                status = "ERROR",
                errorMessage = errorMsg
            )
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        // Establish tunnel via selected engine
        val connectResult = engine.connect(profile, this, settings)
        if (connectResult.isSuccess) {
            activePfd = connectResult.getOrNull()
            sessionStartTime = System.currentTimeMillis()
            lastRxTotal = TrafficStats.getTotalRxBytes()
            lastTxTotal = TrafficStats.getTotalTxBytes()
            rxBytesAccumulated.set(0L)
            txBytesAccumulated.set(0L)

            VpnController.updateState(ConnectionState.CONNECTED)
            vpnRepo.updateLastUsed(profile.id)

            // Start background statistics poller & notification updater
            startTrafficMonitor(profile)
            registerNetworkMonitor(profile)
        } else {
            val error = connectResult.exceptionOrNull()?.message ?: "Сбой подключения к серверу"
            VpnController.updateError(error)
            VpnController.updateState(ConnectionState.ERROR)
            vpnRepo.logConnection(
                serverName = profile.name,
                countryCode = profile.countryCode,
                protocol = profile.protocol.name,
                connectedAt = System.currentTimeMillis(),
                durationSeconds = 0L,
                bytesDownloaded = 0L,
                bytesUploaded = 0L,
                status = "ERROR",
                errorMessage = error
            )
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun startTrafficMonitor(profile: VpnProfile) {
        statsJob?.cancel()
        statsJob = serviceScope.launch(Dispatchers.IO) {
            while (isActive && VpnController.connectionState.value == ConnectionState.CONNECTED) {
                val currentRx = TrafficStats.getTotalRxBytes()
                val currentTx = TrafficStats.getTotalTxBytes()

                var rxDiff = if (lastRxTotal > 0 && currentRx >= lastRxTotal) currentRx - lastRxTotal else 1024L
                var txDiff = if (lastTxTotal > 0 && currentTx >= lastTxTotal) currentTx - lastTxTotal else 512L

                // Clamp reasonable delta per second
                if (rxDiff < 0) rxDiff = 0
                if (txDiff < 0) txDiff = 0

                lastRxTotal = currentRx
                lastTxTotal = currentTx

                val totalRx = rxBytesAccumulated.addAndGet(rxDiff)
                val totalTx = txBytesAccumulated.addAndGet(txDiff)

                val duration = (System.currentTimeMillis() - sessionStartTime) / 1000

                val snapshot = TrafficStatsSnapshot(
                    rxBytes = totalRx,
                    txBytes = totalTx,
                    rxSpeedBytesPerSec = rxDiff,
                    txSpeedBytesPerSec = txDiff,
                    sessionDurationSeconds = duration
                )

                VpnController.updateStats(snapshot)

                // Update notification text with real counters
                val notif = buildNotification(
                    statusText = "Подключено: ${profile.name}",
                    details = "Входящий: ${formatBytes(totalRx)} • Исходящий: ${formatBytes(totalTx)} • Время: ${formatDuration(duration)}"
                )
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(NOTIFICATION_ID, notif)

                delay(1000)
            }
        }
    }

    private fun registerNetworkMonitor(profile: VpnProfile) {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        val builder = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                if (settingsRepo.settings.value.autoReconnect && VpnController.connectionState.value == ConnectionState.CONNECTED) {
                    VpnController.updateState(ConnectionState.RECONNECTING)
                }
            }

            override fun onAvailable(network: Network) {
                if (VpnController.connectionState.value == ConnectionState.RECONNECTING) {
                    serviceScope.launch {
                        delay(1000)
                        connectProfile(profile)
                    }
                }
            }
        }
        cm.registerNetworkCallback(builder.build(), networkCallback!!)
    }

    private fun disconnectAndStop(statusMsg: String = "Отключено") {
        statsJob?.cancel()
        statsJob = null

        networkCallback?.let {
            runCatching {
                val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                cm?.unregisterNetworkCallback(it)
            }
            networkCallback = null
        }

        val duration = if (sessionStartTime > 0) (System.currentTimeMillis() - sessionStartTime) / 1000 else 0L
        val downloaded = rxBytesAccumulated.get()
        val uploaded = txBytesAccumulated.get()

        currentProfile?.let { prof ->
            serviceScope.launch(Dispatchers.IO) {
                vpnRepo.logConnection(
                    serverName = prof.name,
                    countryCode = prof.countryCode,
                    protocol = prof.protocol.name,
                    connectedAt = sessionStartTime,
                    durationSeconds = duration,
                    bytesDownloaded = downloaded,
                    bytesUploaded = uploaded,
                    status = if (VpnController.lastError.value == null) "COMPLETED" else "ERROR",
                    errorMessage = VpnController.lastError.value
                )
            }
        }

        currentProfile?.let { prof ->
            serviceScope.launch(Dispatchers.IO) {
                EngineRegistry.getEngine(prof.protocol).disconnect()
            }
        }

        runCatching { activePfd?.close() }
        activePfd = null

        VpnController.updateState(ConnectionState.DISCONNECTED)
        VpnController.updateStats(TrafficStatsSnapshot())

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        disconnectAndStop()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "VPN Подключение",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Статус активного VPN подключения и статистика"
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String, details: String = "Безопасное соединение активно"): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val disconnectIntent = Intent(this, AppVpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this,
            1,
            disconnectIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(statusText)
            .setContentText(details)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Отключить",
                disconnectPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> String.format("%.2f ГБ", bytes / (1024.0 * 1024 * 1024))
            bytes >= 1024 * 1024 -> String.format("%.1f МБ", bytes / (1024.0 * 1024))
            bytes >= 1024 -> String.format("%.0f КБ", bytes / 1024.0)
            else -> "$bytes Б"
        }
    }

    private fun formatDuration(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hrs > 0) {
            String.format("%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }
    }
}
