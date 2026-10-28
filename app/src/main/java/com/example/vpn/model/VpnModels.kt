package com.example.vpn.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class VpnProtocol(val displayName: String, val category: String) {
    ANDROID_PLATFORM("Android L3 TUN", "Native VPN"),
    SOCKS5_PROXY("SOCKS5 Proxy", "Proxy Mode"),
    HTTP_PROXY("HTTP CONNECT", "Proxy Mode"),
    IKEV2_IPSEC("IKEv2 / IPsec", "Native VPN (Android 11+)"),
    WIREGUARD("WireGuard", "Modern VPN"),
    OPENVPN("OpenVPN", "Classic VPN"),
    SHADOWSOCKS("Shadowsocks", "Encrypted Proxy"),
    AMNEZIA_WG("AmneziaWG", "Anti-Censorship VPN")
}

enum class EngineStatus(val label: String, val isOperable: Boolean) {
    ACTIVE_SUPPORTED("Активен и работает", true),
    ACTIVE_PROXIED("Режим проксирования (L4)", true),
    REQUIRES_API_30("Поддерживается на Android 11+", true),
    REQUIRES_NDK_BINARY("Требует NDK бинарник (.so)", false)
}

data class EngineInfo(
    val protocol: VpnProtocol,
    val displayName: String,
    val status: EngineStatus,
    val license: String,
    val description: String,
    val requirements: String
)

@Entity(tableName = "vpn_profiles")
data class VpnProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val protocol: VpnProtocol,
    val serverHost: String,
    val serverPort: Int,
    val username: String? = null,
    val password: String? = null,
    val privateKey: String? = null,
    val publicKey: String? = null,
    val presharedKey: String? = null,
    val endpoint: String? = null,
    val allowedIps: String? = "0.0.0.0/0, ::/0",
    val dnsServer: String? = "1.1.1.1",
    val mtu: Int = 1420,
    val rawConfig: String? = null,
    val isCustom: Boolean = false,
    val countryCode: String = "US",
    val countryName: String = "United States",
    val city: String = "New York",
    val pingMs: Long? = null,
    val isFavorite: Boolean = false,
    val lastUsed: Long = 0
)

@Entity(tableName = "connection_history")
data class ConnectionHistoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serverName: String,
    val countryCode: String,
    val protocol: String,
    val connectedAt: Long,
    val durationSeconds: Long,
    val bytesDownloaded: Long,
    val bytesUploaded: Long,
    val status: String,
    val errorMessage: String? = null
)

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    RECONNECTING,
    ERROR
}

data class TrafficStatsSnapshot(
    val rxBytes: Long = 0L,
    val txBytes: Long = 0L,
    val rxSpeedBytesPerSec: Long = 0L,
    val txSpeedBytesPerSec: Long = 0L,
    val sessionDurationSeconds: Long = 0L
)

data class DiagnosticsResult(
    val externalIp: String? = null,
    val country: String? = null,
    val isp: String? = null,
    val isVpnActive: Boolean = false,
    val dnsServers: List<String> = emptyList(),
    val pingLatencyMs: Long? = null,
    val testHost: String = "1.1.1.1",
    val statusMessage: String = "",
    val isChecking: Boolean = false
)
