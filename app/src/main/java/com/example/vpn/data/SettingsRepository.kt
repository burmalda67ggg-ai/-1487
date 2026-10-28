package com.example.vpn.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VpnSettings(
    val killSwitchEnabled: Boolean = false,
    val autoReconnect: Boolean = true,
    val autoConnectOnBoot: Boolean = false,
    val dnsPreset: String = "CLOUDFLARE", // CLOUDFLARE, GOOGLE, QUAD9, ADGUARD, CUSTOM
    val customDns: String = "1.1.1.1",
    val mtu: Int = 1420,
    val splitTunnelMode: String = "OFF", // OFF, EXCLUDE, INCLUDE
    val splitTunnelApps: Set<String> = emptySet(),
    val appTheme: String = "DARK", // DARK, LIGHT, SYSTEM
    val appLanguage: String = "RU", // RU, EN
    val selectedProfileId: Long = 1L
) {
    fun getEffectiveDns(): String {
        return when (dnsPreset) {
            "CLOUDFLARE" -> "1.1.1.1"
            "GOOGLE" -> "8.8.8.8"
            "QUAD9" -> "9.9.9.9"
            "ADGUARD" -> "94.140.14.14"
            else -> customDns.ifBlank { "1.1.1.1" }
        }
    }
}

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("omni_vpn_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<VpnSettings> = _settings.asStateFlow()

    private fun loadSettings(): VpnSettings {
        return VpnSettings(
            killSwitchEnabled = prefs.getBoolean("kill_switch", false),
            autoReconnect = prefs.getBoolean("auto_reconnect", true),
            autoConnectOnBoot = prefs.getBoolean("auto_boot", false),
            dnsPreset = prefs.getString("dns_preset", "CLOUDFLARE") ?: "CLOUDFLARE",
            customDns = prefs.getString("custom_dns", "1.1.1.1") ?: "1.1.1.1",
            mtu = prefs.getInt("mtu", 1420),
            splitTunnelMode = prefs.getString("split_tunnel_mode", "OFF") ?: "OFF",
            splitTunnelApps = prefs.getStringSet("split_tunnel_apps", emptySet()) ?: emptySet(),
            appTheme = prefs.getString("app_theme", "DARK") ?: "DARK",
            appLanguage = prefs.getString("app_lang", "RU") ?: "RU",
            selectedProfileId = prefs.getLong("selected_profile_id", 1L)
        )
    }

    fun updateKillSwitch(enabled: Boolean) {
        prefs.edit().putBoolean("kill_switch", enabled).apply()
        _settings.value = _settings.value.copy(killSwitchEnabled = enabled)
    }

    fun updateAutoReconnect(enabled: Boolean) {
        prefs.edit().putBoolean("auto_reconnect", enabled).apply()
        _settings.value = _settings.value.copy(autoReconnect = enabled)
    }

    fun updateAutoBoot(enabled: Boolean) {
        prefs.edit().putBoolean("auto_boot", enabled).apply()
        _settings.value = _settings.value.copy(autoConnectOnBoot = enabled)
    }

    fun updateDnsPreset(preset: String, customIp: String = _settings.value.customDns) {
        prefs.edit()
            .putString("dns_preset", preset)
            .putString("custom_dns", customIp)
            .apply()
        _settings.value = _settings.value.copy(dnsPreset = preset, customDns = customIp)
    }

    fun updateMtu(mtu: Int) {
        val clamped = mtu.coerceIn(1280, 1500)
        prefs.edit().putInt("mtu", clamped).apply()
        _settings.value = _settings.value.copy(mtu = clamped)
    }

    fun updateSplitTunnel(mode: String, apps: Set<String>) {
        prefs.edit()
            .putString("split_tunnel_mode", mode)
            .putStringSet("split_tunnel_apps", apps)
            .apply()
        _settings.value = _settings.value.copy(splitTunnelMode = mode, splitTunnelApps = apps)
    }

    fun toggleAppInSplitTunnel(packageName: String) {
        val currentApps = _settings.value.splitTunnelApps.toMutableSet()
        if (currentApps.contains(packageName)) {
            currentApps.remove(packageName)
        } else {
            currentApps.add(packageName)
        }
        updateSplitTunnel(_settings.value.splitTunnelMode, currentApps)
    }

    fun updateTheme(theme: String) {
        prefs.edit().putString("app_theme", theme).apply()
        _settings.value = _settings.value.copy(appTheme = theme)
    }

    fun updateLanguage(lang: String) {
        prefs.edit().putString("app_lang", lang).apply()
        _settings.value = _settings.value.copy(appLanguage = lang)
    }

    fun setSelectedProfileId(id: Long) {
        prefs.edit().putLong("selected_profile_id", id).apply()
        _settings.value = _settings.value.copy(selectedProfileId = id)
    }

    fun getEffectiveDns(): String {
        return when (_settings.value.dnsPreset) {
            "CLOUDFLARE" -> "1.1.1.1"
            "GOOGLE" -> "8.8.8.8"
            "QUAD9" -> "9.9.9.9"
            "ADGUARD" -> "94.140.14.14"
            else -> _settings.value.customDns.ifBlank { "1.1.1.1" }
        }
    }
}
