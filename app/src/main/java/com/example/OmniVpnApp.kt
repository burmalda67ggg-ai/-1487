package com.example

import android.app.Application
import com.example.vpn.data.AppDatabase
import com.example.vpn.data.SettingsRepository
import com.example.vpn.data.VpnRepository
import com.example.vpn.diagnostics.NetworkDiagnosticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OmniVpnApp : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var vpnRepository: VpnRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var diagnosticsService: NetworkDiagnosticsService
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        diagnosticsService = NetworkDiagnosticsService(this)
        settingsRepository = SettingsRepository(this)
        vpnRepository = VpnRepository(database.vpnProfileDao(), database.connectionHistoryDao(), diagnosticsService)

        // Seed default profiles if first run
        CoroutineScope(Dispatchers.IO).launch {
            vpnRepository.initDefaultProfilesIfEmpty()
        }
    }
}
