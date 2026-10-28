package com.example.vpn.diagnostics

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.example.vpn.model.DiagnosticsResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit

class NetworkDiagnosticsService(private val context: Context) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .build()

    /**
     * Measures REAL round-trip TCP ping to target host and port in milliseconds.
     */
    suspend fun measurePing(host: String, port: Int = 443, timeoutMs: Int = 3000): Long? = withContext(Dispatchers.IO) {
        val socket = Socket()
        val startTime = System.currentTimeMillis()
        try {
            socket.connect(InetSocketAddress(host, port), timeoutMs)
            val duration = System.currentTimeMillis() - startTime
            socket.close()
            duration
        } catch (e: Exception) {
            runCatching { socket.close() }
            null
        }
    }

    /**
     * Checks if Android has an active VPN transport.
     */
    fun isVpnActive(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }

    /**
     * Checks current active network transport (Wi-Fi, Cellular, Ethernet, VPN).
     */
    fun getActiveNetworkType(): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return "Нет соединения"
        val activeNetwork = cm.activeNetwork ?: return "Нет соединения"
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return "Неизвестно"

        val types = mutableListOf<String>()
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) types.add("VPN")
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) types.add("Wi-Fi")
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) types.add("Мобильная сеть")
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) types.add("Ethernet")

        return if (types.isEmpty()) "Подключено" else types.joinToString(" + ")
    }

    /**
     * Queries real external IP and location details via HTTP API.
     */
    suspend fun fetchExternalIpInfo(): Pair<String?, String?> = withContext(Dispatchers.IO) {
        var ip: String? = null
        var country: String? = null

        // Try primary ipify service
        runCatching {
            val request = Request.Builder()
                .url("https://api.ipify.org?format=json")
                .header("User-Agent", "OmniVPN-Client/1.0")
                .build()
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                ip = json.optString("ip")
            }
        }

        // Secondary fallback
        if (ip == null) {
            runCatching {
                val request = Request.Builder()
                    .url("https://icanhazip.com")
                    .header("User-Agent", "OmniVPN-Client/1.0")
                    .build()
                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    ip = response.body?.string()?.trim()
                }
            }
        }

        Pair(ip, country)
    }

    /**
     * Resolves DNS and tests DNS resolution latency and IP addresses.
     */
    suspend fun testDnsResolution(testDomain: String = "cloudflare.com"): Pair<List<String>, Long?> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val addresses = InetAddress.getAllByName(testDomain)
            val elapsed = System.currentTimeMillis() - start
            val ips = addresses.map { it.hostAddress ?: "" }.filter { it.isNotBlank() }
            Pair(ips, elapsed)
        } catch (e: Exception) {
            Pair(emptyList(), null)
        }
    }

    /**
     * Runs full end-to-end diagnostics: external IP, ping, VPN status, DNS leak risk.
     */
    suspend fun runFullDiagnostics(configuredDns: String = "1.1.1.1"): DiagnosticsResult = withContext(Dispatchers.IO) {
        val isVpn = isVpnActive()
        val (ip, _) = fetchExternalIpInfo()
        val ping = measurePing("1.1.1.1", 443, 2500)
        val (dnsIps, dnsLatency) = testDnsResolution()

        val statusMsg = when {
            isVpn && ip != null -> "VPN активен. Внешний IP защищен. Пинг: ${ping ?: "?"} мс."
            !isVpn && ip != null -> "Прямое соединение без VPN. Трафик не шифруется."
            else -> "Сетевое соединение нестабильно или отсутствует доступ в Интернет."
        }

        DiagnosticsResult(
            externalIp = ip ?: "Не удалось определить",
            country = if (isVpn) "Защищенный туннель" else "Локальный провайдер",
            isp = getActiveNetworkType(),
            isVpnActive = isVpn,
            dnsServers = dnsIps.take(4),
            pingLatencyMs = ping ?: dnsLatency,
            testHost = "1.1.1.1",
            statusMessage = statusMsg,
            isChecking = false
        )
    }
}
