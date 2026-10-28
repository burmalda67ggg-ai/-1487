package com.example.vpn.core

import android.util.Base64
import com.example.vpn.model.VpnProfile
import com.example.vpn.model.VpnProtocol
import java.net.URI

object ConfigParsers {

    /**
     * Parse WireGuard or AmneziaWG .conf file
     */
    fun parseWireguard(content: String, profileName: String = "Imported WireGuard"): Result<VpnProfile> {
        return runCatching {
            var privateKey: String? = null
            var address: String? = "10.0.0.2/32"
            var dns: String? = "1.1.1.1"
            var mtu = 1420
            var publicKey: String? = null
            var presharedKey: String? = null
            var endpoint: String? = null
            var allowedIps: String? = "0.0.0.0/0, ::/0"

            var isAmnezia = false
            var inInterface = false
            var inPeer = false

            content.lineSequence().forEach { rawLine ->
                val line = rawLine.trim()
                if (line.startsWith("#") || line.isEmpty()) return@forEach

                when {
                    line.equals("[Interface]", ignoreCase = true) -> {
                        inInterface = true
                        inPeer = false
                    }
                    line.equals("[Peer]", ignoreCase = true) -> {
                        inInterface = false
                        inPeer = true
                    }
                    line.contains("=") -> {
                        val parts = line.split("=", limit = 2)
                        val key = parts[0].trim().lowercase()
                        val value = parts[1].trim()

                        if (inInterface) {
                            when (key) {
                                "privatekey" -> privateKey = value
                                "address" -> address = value
                                "dns" -> dns = value
                                "mtu" -> mtu = value.toIntOrNull() ?: 1420
                                "jc", "jmin", "jmax", "s1", "s2", "h1", "h2", "h3", "h4" -> isAmnezia = true
                            }
                        } else if (inPeer) {
                            when (key) {
                                "publickey" -> publicKey = value
                                "presharedkey" -> presharedKey = value
                                "endpoint" -> endpoint = value
                                "allowedips" -> allowedIps = value
                            }
                        }
                    }
                }
            }

            val endpointHost = endpoint?.substringBefore(":") ?: "127.0.0.1"
            val endpointPort = endpoint?.substringAfter(":", "51820")?.toIntOrNull() ?: 51820

            VpnProfile(
                name = profileName,
                protocol = if (isAmnezia) VpnProtocol.AMNEZIA_WG else VpnProtocol.WIREGUARD,
                serverHost = endpointHost,
                serverPort = endpointPort,
                privateKey = privateKey,
                publicKey = publicKey,
                presharedKey = presharedKey,
                endpoint = endpoint,
                allowedIps = allowedIps,
                dnsServer = dns ?: "1.1.1.1",
                mtu = mtu,
                rawConfig = content,
                isCustom = true,
                countryCode = "WG",
                countryName = if (isAmnezia) "AmneziaWG Tunnel" else "WireGuard Tunnel",
                city = endpointHost
            )
        }
    }

    /**
     * Parse OpenVPN .ovpn profile
     */
    fun parseOpenVpn(content: String, profileName: String = "Imported OpenVPN"): Result<VpnProfile> {
        return runCatching {
            var remoteHost = "127.0.0.1"
            var remotePort = 1194
            var proto = "udp"

            content.lineSequence().forEach { rawLine ->
                val line = rawLine.trim()
                if (line.startsWith("#") || line.startsWith(";") || line.isEmpty()) return@forEach

                val tokens = line.split("\\s+".toRegex())
                when (tokens[0].lowercase()) {
                    "remote" -> {
                        if (tokens.size >= 2) remoteHost = tokens[1]
                        if (tokens.size >= 3) remotePort = tokens[2].toIntOrNull() ?: 1194
                    }
                    "proto" -> {
                        if (tokens.size >= 2) proto = tokens[1].lowercase()
                    }
                }
            }

            VpnProfile(
                name = profileName,
                protocol = VpnProtocol.OPENVPN,
                serverHost = remoteHost,
                serverPort = remotePort,
                rawConfig = content,
                isCustom = true,
                countryCode = "OVPN",
                countryName = "OpenVPN ($proto)",
                city = "$remoteHost:$remotePort"
            )
        }
    }

    /**
     * Parse Shadowsocks URI: ss://BASE64@host:port or ss://BASE64#name
     */
    fun parseShadowsocks(uriString: String): Result<VpnProfile> {
        return runCatching {
            val trimmed = uriString.trim()
            require(trimmed.startsWith("ss://", ignoreCase = true)) { "Not a Shadowsocks URI" }

            val fragment = if (trimmed.contains("#")) trimmed.substringAfter("#") else null
            val mainPart = trimmed.removePrefix("ss://").substringBefore("#")

            val name = fragment?.let { runCatching { java.net.URLDecoder.decode(it, "UTF-8") }.getOrNull() } ?: "Shadowsocks Profile"

            var host = "127.0.0.1"
            var port = 8388
            var cipher = "aes-256-gcm"
            var password = ""

            if (mainPart.contains("@")) {
                val userInfo = mainPart.substringBefore("@")
                val hostPort = mainPart.substringAfter("@")
                host = hostPort.substringBefore(":")
                port = hostPort.substringAfter(":", "8388").toIntOrNull() ?: 8388

                val decodedUser = String(Base64.decode(userInfo, Base64.URL_SAFE or Base64.NO_WRAP))
                if (decodedUser.contains(":")) {
                    cipher = decodedUser.substringBefore(":")
                    password = decodedUser.substringAfter(":")
                }
            } else {
                // Entire string is base64 encoded
                val decoded = String(Base64.decode(mainPart, Base64.URL_SAFE or Base64.NO_WRAP))
                val decodedUri = URI.create("ss://$decoded")
                host = decodedUri.host ?: "127.0.0.1"
                port = if (decodedUri.port != -1) decodedUri.port else 8388
                val userInfo = decodedUri.userInfo ?: ""
                if (userInfo.contains(":")) {
                    cipher = userInfo.substringBefore(":")
                    password = userInfo.substringAfter(":")
                }
            }

            VpnProfile(
                name = name,
                protocol = VpnProtocol.SHADOWSOCKS,
                serverHost = host,
                serverPort = port,
                username = cipher,
                password = password,
                rawConfig = uriString,
                isCustom = true,
                countryCode = "SS",
                countryName = "Shadowsocks ($cipher)",
                city = "$host:$port"
            )
        }
    }

    /**
     * Parse SOCKS5 / HTTP URI: socks5://user:pass@host:port or http://host:port
     */
    fun parseProxyUri(uriString: String): Result<VpnProfile> {
        return runCatching {
            val uri = URI.create(uriString.trim())
            val scheme = uri.scheme?.lowercase()
            val isSocks = scheme == "socks5" || scheme == "socks"
            val isHttp = scheme == "http" || scheme == "https"

            require(isSocks || isHttp) { "Unsupported proxy scheme: $scheme" }

            val host = uri.host ?: error("Host is required")
            val port = if (uri.port != -1) uri.port else (if (isSocks) 1080 else 8080)
            var user: String? = null
            var pass: String? = null

            uri.userInfo?.let { info ->
                if (info.contains(":")) {
                    user = info.substringBefore(":")
                    pass = info.substringAfter(":")
                } else {
                    user = info
                }
            }

            VpnProfile(
                name = if (isSocks) "SOCKS5 Proxy ($host)" else "HTTP Proxy ($host)",
                protocol = if (isSocks) VpnProtocol.SOCKS5_PROXY else VpnProtocol.HTTP_PROXY,
                serverHost = host,
                serverPort = port,
                username = user,
                password = pass,
                isCustom = true,
                countryCode = if (isSocks) "SK5" else "HTTP",
                countryName = if (isSocks) "SOCKS5 Proxy" else "HTTP Proxy",
                city = "$host:$port"
            )
        }
    }

    /**
     * Generic auto-detector that tries to import any supported text configuration
     */
    fun autoDetectAndParse(input: String, customName: String? = null): Result<VpnProfile> {
        val trimmed = input.trim()
        val name = customName?.ifBlank { null }

        return when {
            trimmed.startsWith("ss://", ignoreCase = true) -> parseShadowsocks(trimmed)
            trimmed.startsWith("socks5://", ignoreCase = true) ||
            trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) -> parseProxyUri(trimmed)
            trimmed.contains("[Interface]", ignoreCase = true) || trimmed.contains("[Peer]", ignoreCase = true) ->
                parseWireguard(trimmed, name ?: "WireGuard Profile")
            trimmed.contains("dev tun", ignoreCase = true) || trimmed.contains("client", ignoreCase = true) || trimmed.contains("<ca>") ->
                parseOpenVpn(trimmed, name ?: "OpenVPN Profile")
            else -> Result.failure(IllegalArgumentException("Неизвестный формат конфигурации. Поддерживаются .conf (WireGuard), .ovpn (OpenVPN), ss://, socks5://, http://"))
        }
    }
}
