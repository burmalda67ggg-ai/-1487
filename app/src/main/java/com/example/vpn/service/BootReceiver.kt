package com.example.vpn.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.vpn.data.AppDatabase
import com.example.vpn.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val settingsRepo = SettingsRepository(context)
            if (settingsRepo.settings.value.autoConnectOnBoot) {
                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getInstance(context)
                    val lastProfile = db.vpnProfileDao().getLastUsedProfile()
                        ?: db.vpnProfileDao().getProfileById(settingsRepo.settings.value.selectedProfileId)

                    lastProfile?.let {
                        VpnController.startVpn(context, it)
                    }
                }
            }
        }
    }
}
