package com.example.vpn.data

import com.example.vpn.diagnostics.NetworkDiagnosticsService
import com.example.vpn.model.ConnectionHistoryItem
import com.example.vpn.model.VpnProfile
import com.example.vpn.model.VpnProtocol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class VpnRepository(
    private val profileDao: VpnProfileDao,
    private val historyDao: ConnectionHistoryDao,
    private val diagnosticsService: NetworkDiagnosticsService
) {
    val allProfiles: Flow<List<VpnProfile>> = profileDao.getAllProfiles()
    val connectionHistory: Flow<List<ConnectionHistoryItem>> = historyDao.getHistory()

    suspend fun initDefaultProfilesIfEmpty() = withContext(Dispatchers.IO) {
        if (profileDao.getCount() == 0) {
            val defaults = listOf(
                VpnProfile(
                    name = "Frankfurt Fast Server",
                    protocol = VpnProtocol.ANDROID_PLATFORM,
                    serverHost = "1.1.1.1",
                    serverPort = 443,
                    countryCode = "DE",
                    countryName = "Germany",
                    city = "Frankfurt",
                    isCustom = false
                ),
                VpnProfile(
                    name = "Amsterdam Node 1",
                    protocol = VpnProtocol.ANDROID_PLATFORM,
                    serverHost = "8.8.8.8",
                    serverPort = 443,
                    countryCode = "NL",
                    countryName = "Netherlands",
                    city = "Amsterdam",
                    isCustom = false
                ),
                VpnProfile(
                    name = "London Central",
                    protocol = VpnProtocol.ANDROID_PLATFORM,
                    serverHost = "9.9.9.9",
                    serverPort = 443,
                    countryCode = "GB",
                    countryName = "United Kingdom",
                    city = "London",
                    isCustom = false
                ),
                VpnProfile(
                    name = "New York Metro",
                    protocol = VpnProtocol.ANDROID_PLATFORM,
                    serverHost = "208.67.222.222",
                    serverPort = 443,
                    countryCode = "US",
                    countryName = "United States",
                    city = "New York",
                    isCustom = false
                ),
                VpnProfile(
                    name = "Singapore Hub",
                    protocol = VpnProtocol.ANDROID_PLATFORM,
                    serverHost = "1.0.0.1",
                    serverPort = 443,
                    countryCode = "SG",
                    countryName = "Singapore",
                    city = "Singapore",
                    isCustom = false
                ),
                VpnProfile(
                    name = "Tokyo Express",
                    protocol = VpnProtocol.ANDROID_PLATFORM,
                    serverHost = "8.8.4.4",
                    serverPort = 443,
                    countryCode = "JP",
                    countryName = "Japan",
                    city = "Tokyo",
                    isCustom = false
                ),
                VpnProfile(
                    name = "Warsaw Secure",
                    protocol = VpnProtocol.ANDROID_PLATFORM,
                    serverHost = "94.140.14.14",
                    serverPort = 443,
                    countryCode = "PL",
                    countryName = "Poland",
                    city = "Warsaw",
                    isCustom = false
                ),
                VpnProfile(
                    name = "Zurich Alpine",
                    protocol = VpnProtocol.ANDROID_PLATFORM,
                    serverHost = "149.112.112.112",
                    serverPort = 443,
                    countryCode = "CH",
                    countryName = "Switzerland",
                    city = "Zurich",
                    isCustom = false
                )
            )
            profileDao.insertProfiles(defaults)
        }
    }

    suspend fun getProfileById(id: Long): VpnProfile? = withContext(Dispatchers.IO) {
        profileDao.getProfileById(id)
    }

    suspend fun saveProfile(profile: VpnProfile): Long = withContext(Dispatchers.IO) {
        if (profile.id == 0L) {
            profileDao.insertProfile(profile)
        } else {
            profileDao.updateProfile(profile)
            profile.id
        }
    }

    suspend fun deleteProfile(profile: VpnProfile) = withContext(Dispatchers.IO) {
        profileDao.deleteProfile(profile)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        profileDao.setFavorite(id, isFavorite)
    }

    suspend fun updateLastUsed(id: Long) = withContext(Dispatchers.IO) {
        profileDao.updateLastUsed(id, System.currentTimeMillis())
    }

    suspend fun refreshAllPings(profiles: List<VpnProfile>) = withContext(Dispatchers.IO) {
        profiles.map { profile ->
            async {
                val ping = diagnosticsService.measurePing(profile.serverHost, profile.serverPort, 2000)
                profileDao.updatePing(profile.id, ping)
            }
        }.awaitAll()
    }

    suspend fun findBestProfile(profiles: List<VpnProfile>): VpnProfile? = withContext(Dispatchers.IO) {
        if (profiles.isEmpty()) return@withContext null
        // Find profile with lowest ping, pinging on the fly if necessary
        var best: VpnProfile? = null
        var lowestPing = Long.MAX_VALUE

        profiles.forEach { profile ->
            val ping = profile.pingMs ?: diagnosticsService.measurePing(profile.serverHost, profile.serverPort, 1500)
            if (ping != null) {
                profileDao.updatePing(profile.id, ping)
                if (ping < lowestPing) {
                    lowestPing = ping
                    best = profile.copy(pingMs = ping)
                }
            }
        }
        best ?: profiles.firstOrNull()
    }

    suspend fun logConnection(
        serverName: String,
        countryCode: String,
        protocol: String,
        connectedAt: Long,
        durationSeconds: Long,
        bytesDownloaded: Long,
        bytesUploaded: Long,
        status: String,
        errorMessage: String? = null
    ) = withContext(Dispatchers.IO) {
        historyDao.insertHistory(
            ConnectionHistoryItem(
                serverName = serverName,
                countryCode = countryCode,
                protocol = protocol,
                connectedAt = connectedAt,
                durationSeconds = durationSeconds,
                bytesDownloaded = bytesDownloaded,
                bytesUploaded = bytesUploaded,
                status = status,
                errorMessage = errorMessage
            )
        )
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        historyDao.clearHistory()
    }
}
