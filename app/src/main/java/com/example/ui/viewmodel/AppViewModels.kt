package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.OmniVpnApp
import com.example.vpn.core.ConfigParsers
import com.example.vpn.core.EngineRegistry
import com.example.vpn.data.VpnSettings
import com.example.vpn.diagnostics.NetworkDiagnosticsService
import com.example.vpn.model.ConnectionHistoryItem
import com.example.vpn.model.ConnectionState
import com.example.vpn.model.DiagnosticsResult
import com.example.vpn.model.EngineInfo
import com.example.vpn.model.TrafficStatsSnapshot
import com.example.vpn.model.VpnProfile
import com.example.vpn.service.VpnController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InstalledAppItem(
    val name: String,
    val packageName: String,
    val icon: Drawable?,
    val isSystem: Boolean
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as OmniVpnApp
    private val vpnRepo = app.vpnRepository
    private val settingsRepo = app.settingsRepository
    private val diagnostics = app.diagnosticsService

    val connectionState: StateFlow<ConnectionState> = VpnController.connectionState
    val currentProfile: StateFlow<VpnProfile?> = VpnController.currentProfile
    val trafficStats: StateFlow<TrafficStatsSnapshot> = VpnController.trafficStats
    val lastError: StateFlow<String?> = VpnController.lastError

    val allProfiles: StateFlow<List<VpnProfile>> = vpnRepo.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val connectionHistory: StateFlow<List<ConnectionHistoryItem>> = vpnRepo.connectionHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isPinging = MutableStateFlow(false)
    val isPinging: StateFlow<Boolean> = _isPinging.asStateFlow()

    private val _quickIpInfo = MutableStateFlow<String?>("Проверка...")
    val quickIpInfo: StateFlow<String?> = _quickIpInfo.asStateFlow()

    init {
        // Set initial selected profile
        viewModelScope.launch {
            allProfiles.collect { profiles ->
                if (currentProfile.value == null && profiles.isNotEmpty()) {
                    val savedId = settingsRepo.settings.value.selectedProfileId
                    val matched = profiles.firstOrNull { it.id == savedId } ?: profiles.first()
                    VpnController.updateProfile(matched)
                }
            }
        }
        refreshQuickIp()
    }

    fun refreshQuickIp() {
        viewModelScope.launch {
            val (ip, _) = diagnostics.fetchExternalIpInfo()
            _quickIpInfo.value = ip ?: "Не удалось определить"
        }
    }

    fun selectProfile(profile: VpnProfile) {
        VpnController.updateProfile(profile)
        settingsRepo.setSelectedProfileId(profile.id)
    }

    fun connect(context: Context, profile: VpnProfile? = currentProfile.value) {
        val target = profile ?: return
        VpnController.startVpn(context, target)
        viewModelScope.launch {
            diagnostics.measurePing(target.serverHost, target.serverPort, 1500)
        }
    }

    fun disconnect(context: Context) {
        VpnController.stopVpn(context)
    }

    fun refreshAllPings() {
        viewModelScope.launch {
            _isPinging.value = true
            vpnRepo.refreshAllPings(allProfiles.value)
            _isPinging.value = false
        }
    }

    fun autoSelectBestServer(context: Context) {
        viewModelScope.launch {
            _isPinging.value = true
            val best = vpnRepo.findBestProfile(allProfiles.value)
            _isPinging.value = false
            if (best != null) {
                selectProfile(best)
                connect(context, best)
            }
        }
    }

    fun toggleFavorite(id: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            vpnRepo.toggleFavorite(id, isFavorite)
        }
    }

    fun deleteProfile(profile: VpnProfile) {
        viewModelScope.launch {
            vpnRepo.deleteProfile(profile)
        }
    }

    fun saveProfile(profile: VpnProfile) {
        viewModelScope.launch {
            val id = vpnRepo.saveProfile(profile)
            if (currentProfile.value?.id == id || currentProfile.value == null) {
                selectProfile(profile.copy(id = id))
            }
        }
    }

    fun importConfigText(rawText: String, customName: String? = null): Result<VpnProfile> {
        val result = ConfigParsers.autoDetectAndParse(rawText, customName)
        result.onSuccess { profile ->
            viewModelScope.launch {
                val savedId = vpnRepo.saveProfile(profile)
                selectProfile(profile.copy(id = savedId))
            }
        }
        return result
    }

    fun clearHistory() {
        viewModelScope.launch {
            vpnRepo.clearHistory()
        }
    }
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as OmniVpnApp
    private val settingsRepo = app.settingsRepository

    val settings: StateFlow<VpnSettings> = settingsRepo.settings

    private val _installedApps = MutableStateFlow<List<InstalledAppItem>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppItem>> = _installedApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingApps.value = true
            val pm = getApplication<Application>().packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(intent, 0)
            val items = resolveInfos.mapNotNull { resolveInfo ->
                runCatching {
                    val appInfo = resolveInfo.activityInfo.applicationInfo
                    InstalledAppItem(
                        name = pm.getApplicationLabel(appInfo).toString(),
                        packageName = appInfo.packageName,
                        icon = pm.getApplicationIcon(appInfo),
                        isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    )
                }.getOrNull()
            }.distinctBy { it.packageName }.sortedBy { it.name.lowercase() }

            _installedApps.value = items
            _isLoadingApps.value = false
        }
    }

    fun updateKillSwitch(enabled: Boolean) = settingsRepo.updateKillSwitch(enabled)
    fun updateAutoReconnect(enabled: Boolean) = settingsRepo.updateAutoReconnect(enabled)
    fun updateAutoBoot(enabled: Boolean) = settingsRepo.updateAutoBoot(enabled)
    fun updateDnsPreset(preset: String, customIp: String = "") = settingsRepo.updateDnsPreset(preset, customIp)
    fun updateMtu(mtu: Int) = settingsRepo.updateMtu(mtu)
    fun updateSplitTunnelMode(mode: String) = settingsRepo.updateSplitTunnel(mode, settings.value.splitTunnelApps)
    fun toggleAppInSplitTunnel(pkgName: String) = settingsRepo.toggleAppInSplitTunnel(pkgName)
    fun updateTheme(theme: String) = settingsRepo.updateTheme(theme)
    fun updateLanguage(lang: String) = settingsRepo.updateLanguage(lang)
}

class DiagnosticsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as OmniVpnApp
    private val diagnostics = app.diagnosticsService
    private val settings = app.settingsRepository

    private val _diagnostics = MutableStateFlow(DiagnosticsResult())
    val diagnosticsResult: StateFlow<DiagnosticsResult> = _diagnostics.asStateFlow()

    val protocolMatrix: List<EngineInfo> = EngineRegistry.getAllEngineInfos()

    init {
        runDiagnostics()
    }

    fun runDiagnostics() {
        viewModelScope.launch {
            _diagnostics.value = _diagnostics.value.copy(isChecking = true, statusMessage = "Выполняется диагностика сети...")
            val result = diagnostics.runFullDiagnostics(settings.getEffectiveDns())
            _diagnostics.value = result
        }
    }

    fun runCustomPing(host: String, port: Int = 443) {
        viewModelScope.launch {
            _diagnostics.value = _diagnostics.value.copy(isChecking = true, statusMessage = "Пинг к $host:$port...")
            val latency = diagnostics.measurePing(host, port, 3000)
            _diagnostics.value = _diagnostics.value.copy(
                isChecking = false,
                testHost = "$host:$port",
                pingLatencyMs = latency,
                statusMessage = if (latency != null) "Ответ от $host:$port за $latency мс" else "Превышен таймаут ожидания от $host:$port"
            )
        }
    }
}
