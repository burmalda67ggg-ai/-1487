package com.example

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.OmniTopBar
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PrivacyScreen
import com.example.ui.screens.ProfilesScreen
import com.example.ui.screens.ServersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplitTunnelScreen
import com.example.ui.theme.OmniVpnTheme
import com.example.ui.theme.SapphireAccent
import com.example.ui.theme.TextLuxuryPrimary
import com.example.ui.theme.TextLuxurySecondary
import com.example.ui.theme.TextLuxuryTertiary
import com.example.ui.theme.TitaniumBorder
import com.example.ui.theme.TitaniumElevated
import com.example.ui.theme.TitaniumSurface
import com.example.ui.viewmodel.DiagnosticsViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.vpn.model.ConnectionState
import com.example.vpn.model.VpnProfile

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Главная", Icons.Filled.Home, Icons.Outlined.Home),
    SERVERS("Серверы", Icons.Filled.Dns, Icons.Outlined.Dns),
    PROFILES("Профили", Icons.Filled.VpnKey, Icons.Outlined.VpnKey),
    SPLIT("Туннель", Icons.Filled.Apps, Icons.Outlined.Apps),
    SETTINGS("Настройки", Icons.Filled.Settings, Icons.Outlined.Settings)
}

enum class SubScreen {
    NONE,
    DIAGNOSTICS,
    PRIVACY
}

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val diagnosticsViewModel: DiagnosticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by settingsViewModel.settings.collectAsState()
            val isDarkTheme = settings.appTheme == "DARK"

            OmniVpnTheme(darkTheme = isDarkTheme) {
                MainAppScreen(
                    mainViewModel = mainViewModel,
                    settingsViewModel = settingsViewModel,
                    diagnosticsViewModel = diagnosticsViewModel
                )
            }
        }
    }
}

@Composable
fun MainAppScreen(
    mainViewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    diagnosticsViewModel: DiagnosticsViewModel
) {
    val context = LocalContext.current
    var currentTab by remember { mutableIntStateOf(MainTab.HOME.ordinal) }
    var currentSubScreen by remember { mutableStateOf(SubScreen.NONE) }

    val connectionState by mainViewModel.connectionState.collectAsState()
    val currentProfile by mainViewModel.currentProfile.collectAsState()
    val trafficStats by mainViewModel.trafficStats.collectAsState()
    val lastError by mainViewModel.lastError.collectAsState()
    val allProfiles by mainViewModel.allProfiles.collectAsState()
    val connectionHistory by mainViewModel.connectionHistory.collectAsState()
    val isPinging by mainViewModel.isPinging.collectAsState()
    val quickIp by mainViewModel.quickIpInfo.collectAsState()

    val settings by settingsViewModel.settings.collectAsState()
    val installedApps by settingsViewModel.installedApps.collectAsState()
    val isLoadingApps by settingsViewModel.isLoadingApps.collectAsState()

    val diagnostics by diagnosticsViewModel.diagnosticsResult.collectAsState()
    val protocolMatrix = diagnosticsViewModel.protocolMatrix

    // Notification permission launcher for Android 13+
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* permission handled */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // VPN Permission Launcher
    var pendingProfileToConnect by remember { mutableStateOf<VpnProfile?>(null) }
    val vpnPrepareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val target = pendingProfileToConnect ?: currentProfile
            if (target != null) {
                mainViewModel.connect(context, target)
            }
        } else {
            Toast.makeText(context, "Разрешение на создание VPN не предоставлено", Toast.LENGTH_SHORT).show()
        }
        pendingProfileToConnect = null
    }

    fun requestConnect(profile: VpnProfile?) {
        val target = profile ?: currentProfile ?: return
        val vpnIntent = VpnService.prepare(context)
        if (vpnIntent != null) {
            pendingProfileToConnect = target
            vpnPrepareLauncher.launch(vpnIntent)
        } else {
            mainViewModel.connect(context, target)
        }
    }

    // Back handling: pop SubScreen first, then go back to HOME tab
    BackHandler(enabled = currentSubScreen != SubScreen.NONE || currentTab != MainTab.HOME.ordinal) {
        if (currentSubScreen != SubScreen.NONE) {
            currentSubScreen = SubScreen.NONE
        } else {
            currentTab = MainTab.HOME.ordinal
        }
    }

    val activeTab = MainTab.values()[currentTab]

    val topBarTitle = when (currentSubScreen) {
        SubScreen.DIAGNOSTICS -> "Диагностика"
        SubScreen.PRIVACY -> "Конфиденциальность"
        SubScreen.NONE -> when (activeTab) {
            MainTab.HOME -> "OmniVPN"
            MainTab.SERVERS -> "Серверы"
            MainTab.PROFILES -> "Профили"
            MainTab.SPLIT -> "Split Tunnel"
            MainTab.SETTINGS -> "Настройки"
        }
    }

    val topBarSubtitle = when {
        currentSubScreen != SubScreen.NONE -> null
        activeTab == MainTab.HOME && currentProfile != null -> currentProfile?.name
        else -> null
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            OmniTopBar(
                title = topBarTitle,
                subtitle = topBarSubtitle,
                connectionState = if (activeTab == MainTab.HOME && currentSubScreen == SubScreen.NONE) connectionState else null,
                onBack = if (currentSubScreen != SubScreen.NONE) {
                    { currentSubScreen = SubScreen.NONE }
                } else if (currentTab != MainTab.HOME.ordinal) {
                    { currentTab = MainTab.HOME.ordinal }
                } else null,
                actions = {
                    if (currentSubScreen == SubScreen.NONE) {
                        IconButton(
                            onClick = { currentSubScreen = SubScreen.PRIVACY },
                            modifier = Modifier.testTag("privacy_info_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Конфиденциальность",
                                tint = TextLuxurySecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (currentSubScreen == SubScreen.NONE) {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .height(64.dp)
                        .testTag("bottom_navigation_bar"),
                    containerColor = TitaniumSurface,
                    tonalElevation = 0.dp
                ) {
                    MainTab.values().forEach { tab ->
                        val isSelected = currentTab == tab.ordinal
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab.ordinal },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SapphireAccent,
                                selectedTextColor = SapphireAccent,
                                indicatorColor = TitaniumElevated,
                                unselectedIconColor = TextLuxuryTertiary,
                                unselectedTextColor = TextLuxuryTertiary
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentSubScreen) {
                SubScreen.DIAGNOSTICS -> {
                    DiagnosticsScreen(
                        diagnostics = diagnostics,
                        protocolMatrix = protocolMatrix,
                        onRunDiagnostics = { diagnosticsViewModel.runDiagnostics() },
                        onCustomPing = { host, port -> diagnosticsViewModel.runCustomPing(host, port) }
                    )
                }
                SubScreen.PRIVACY -> {
                    PrivacyScreen()
                }
                SubScreen.NONE -> {
                    when (activeTab) {
                        MainTab.HOME -> {
                            HomeScreen(
                                connectionState = connectionState,
                                currentProfile = currentProfile,
                                trafficStats = trafficStats,
                                lastError = lastError,
                                externalIp = quickIp,
                                onConnectToggle = {
                                    if (connectionState == ConnectionState.CONNECTED || connectionState == ConnectionState.CONNECTING) {
                                        mainViewModel.disconnect(context)
                                    } else {
                                        requestConnect(currentProfile)
                                    }
                                },
                                onSelectServerClick = { currentTab = MainTab.SERVERS.ordinal },
                                onAutoBestClick = { mainViewModel.autoSelectBestServer(context) },
                                onRefreshIp = { mainViewModel.refreshQuickIp() },
                                onClearError = { /* dismiss error banner */ }
                            )
                        }
                        MainTab.SERVERS -> {
                            ServersScreen(
                                profiles = allProfiles,
                                selectedProfile = currentProfile,
                                isPinging = isPinging,
                                onSelectProfile = { profile ->
                                    mainViewModel.selectProfile(profile)
                                    currentTab = MainTab.HOME.ordinal
                                },
                                onRefreshPings = { mainViewModel.refreshAllPings() },
                                onToggleFavorite = { id, isFav -> mainViewModel.toggleFavorite(id, isFav) }
                            )
                        }
                        MainTab.PROFILES -> {
                            ProfilesScreen(
                                profiles = allProfiles,
                                selectedProfile = currentProfile,
                                history = connectionHistory,
                                onSelectProfile = { profile ->
                                    mainViewModel.selectProfile(profile)
                                    currentTab = MainTab.HOME.ordinal
                                },
                                onSaveProfile = { profile -> mainViewModel.saveProfile(profile) },
                                onDeleteProfile = { profile -> mainViewModel.deleteProfile(profile) },
                                onImportText = { text, name -> mainViewModel.importConfigText(text, name) },
                                onClearHistory = { mainViewModel.clearHistory() }
                            )
                        }
                        MainTab.SPLIT -> {
                            SplitTunnelScreen(
                                currentMode = settings.splitTunnelMode,
                                selectedApps = settings.splitTunnelApps,
                                installedApps = installedApps,
                                isLoading = isLoadingApps,
                                onModeChange = { mode -> settingsViewModel.updateSplitTunnelMode(mode) },
                                onToggleApp = { pkg -> settingsViewModel.toggleAppInSplitTunnel(pkg) }
                            )
                        }
                        MainTab.SETTINGS -> {
                            SettingsScreen(
                                settings = settings,
                                onKillSwitchToggle = { settingsViewModel.updateKillSwitch(it) },
                                onAutoReconnectToggle = { settingsViewModel.updateAutoReconnect(it) },
                                onAutoBootToggle = { settingsViewModel.updateAutoBoot(it) },
                                onDnsPresetChange = { preset, custom -> settingsViewModel.updateDnsPreset(preset, custom) },
                                onMtuChange = { settingsViewModel.updateMtu(it) },
                                onThemeChange = { settingsViewModel.updateTheme(it) },
                                onLanguageChange = { settingsViewModel.updateLanguage(it) },
                                onOpenDiagnostics = { currentSubScreen = SubScreen.DIAGNOSTICS },
                                onOpenPrivacy = { currentSubScreen = SubScreen.PRIVACY }
                            )
                        }
                    }
                }
            }
        }
    }
}
