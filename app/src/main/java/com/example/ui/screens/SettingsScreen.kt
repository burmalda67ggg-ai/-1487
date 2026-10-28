package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldLuxury
import com.example.ui.theme.SapphireAccent
import com.example.ui.theme.TextLuxuryPrimary
import com.example.ui.theme.TextLuxurySecondary
import com.example.ui.theme.TextLuxuryTertiary
import com.example.ui.theme.TitaniumBorder
import com.example.ui.theme.TitaniumCard
import com.example.ui.theme.TitaniumElevated
import com.example.ui.theme.TitaniumSurface
import com.example.vpn.data.VpnSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: VpnSettings,
    onKillSwitchToggle: (Boolean) -> Unit,
    onAutoReconnectToggle: (Boolean) -> Unit,
    onAutoBootToggle: (Boolean) -> Unit,
    onDnsPresetChange: (String, String) -> Unit,
    onMtuChange: (Int) -> Unit,
    onThemeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onOpenDiagnostics: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var dnsDropdownExpanded by remember { mutableStateOf(false) }
    var customDnsText by remember { mutableStateOf(settings.customDns) }

    val dnsPresets = listOf(
        "CLOUDFLARE" to "Cloudflare (1.1.1.1 - Быстрый)",
        "GOOGLE" to "Google DNS (8.8.8.8)",
        "QUAD9" to "Quad9 (9.9.9.9 - Защита от угроз)",
        "ADGUARD" to "AdGuard (94.140.14.14 - Без рекламы)",
        "CUSTOM" to "Собственный DNS"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Security
        LuxurySectionHeader(title = "БЕЗОПАСНОСТЬ И ЗАЩИТА")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, TitaniumBorder)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(TitaniumCard, TitaniumSurface)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Kill Switch
                    LuxuryToggleRow(
                        icon = Icons.Default.Security,
                        iconColor = AmberWarning,
                        title = "Kill Switch",
                        subtitle = "Блокировка исходящего трафика при внезапном разрыве VPN",
                        checked = settings.killSwitchEnabled,
                        onCheckedChange = onKillSwitchToggle,
                        testTag = "kill_switch_toggle"
                    )

                    // DNS Settings
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = SapphireAccent,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "DNS резолвер и предотвращение утечек",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextLuxuryPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Инкапсуляция всех сетевых имён внутри защищённого туннеля.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLuxuryTertiary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        ExposedDropdownMenuBox(
                            expanded = dnsDropdownExpanded,
                            onExpandedChange = { dnsDropdownExpanded = !dnsDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = dnsPresets.firstOrNull { it.first == settings.dnsPreset }?.second ?: "Cloudflare",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dnsDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SapphireAccent,
                                    unfocusedBorderColor = TitaniumBorder,
                                    focusedTextColor = TextLuxuryPrimary,
                                    unfocusedTextColor = TextLuxuryPrimary,
                                    focusedContainerColor = TitaniumElevated,
                                    unfocusedContainerColor = TitaniumElevated
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = dnsDropdownExpanded,
                                onDismissRequest = { dnsDropdownExpanded = false },
                                modifier = Modifier.background(TitaniumElevated)
                            ) {
                                dnsPresets.forEach { (presetKey, presetLabel) ->
                                    DropdownMenuItem(
                                        text = { Text(presetLabel, color = TextLuxuryPrimary) },
                                        onClick = {
                                            dnsDropdownExpanded = false
                                            onDnsPresetChange(presetKey, customDnsText)
                                        }
                                    )
                                }
                            }
                        }

                        if (settings.dnsPreset == "CUSTOM") {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = customDnsText,
                                onValueChange = {
                                    customDnsText = it
                                    onDnsPresetChange("CUSTOM", it)
                                },
                                placeholder = { Text("IP-адрес DNS (например 1.1.1.1)", color = TextLuxuryTertiary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SapphireAccent,
                                    unfocusedBorderColor = TitaniumBorder,
                                    focusedTextColor = TextLuxuryPrimary,
                                    unfocusedTextColor = TextLuxuryPrimary,
                                    focusedContainerColor = TitaniumElevated,
                                    unfocusedContainerColor = TitaniumElevated
                                ),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        }

        // Section: Network & Infrastructure
        LuxurySectionHeader(title = "СЕТЬ И ПАРАМЕТРЫ МАРШРУТИЗАЦИИ")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, TitaniumBorder)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(TitaniumCard, TitaniumSurface)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Auto Reconnect
                    LuxuryToggleRow(
                        icon = Icons.Default.Sync,
                        iconColor = SapphireAccent,
                        title = "Автопереподключение",
                        subtitle = "Интеллектуальный роуминг между сетями Wi-Fi и сотовой связью (5G/LTE)",
                        checked = settings.autoReconnect,
                        onCheckedChange = onAutoReconnectToggle,
                        testTag = "auto_reconnect_toggle"
                    )

                    // Auto Boot
                    LuxuryToggleRow(
                        icon = Icons.Default.PowerSettingsNew,
                        iconColor = EmeraldLuxury,
                        title = "Подключение при старте устройства",
                        subtitle = "Инициализация защищённого канала сразу после загрузки системы",
                        checked = settings.autoConnectOnBoot,
                        onCheckedChange = onAutoBootToggle,
                        testTag = "auto_boot_toggle"
                    )

                    // MTU Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = TextLuxurySecondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Размер полезной нагрузки MTU",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextLuxuryPrimary
                                )
                            }
                            Text(
                                text = "${settings.mtu} B",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = SapphireAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Оптимальный размер: 1420 (WireGuard / Amnezia) или 1500 (Стандартный)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLuxuryTertiary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Slider(
                            value = settings.mtu.toFloat(),
                            onValueChange = { onMtuChange(it.toInt()) },
                            valueRange = 1280f..1500f,
                            steps = 21,
                            colors = SliderDefaults.colors(
                                thumbColor = SapphireAccent,
                                activeTrackColor = SapphireAccent,
                                inactiveTrackColor = TitaniumElevated
                            ),
                            modifier = Modifier.testTag("mtu_slider")
                        )
                    }
                }
            }
        }

        // Section: Diagnostics & Auditing
        LuxurySectionHeader(title = "ДИАГНОСТИКА И АУДИТ")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, TitaniumBorder)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(TitaniumCard, TitaniumSurface)
                        )
                    )
            ) {
                Column {
                    LuxuryActionRow(
                        title = "Диагностика туннеля и статус движков",
                        subtitle = "Проверка внешнего IP, сокетного пинга и таблица протоколов",
                        icon = Icons.Default.NetworkCheck,
                        onClick = onOpenDiagnostics
                    )
                    LuxuryActionRow(
                        title = "Политика Zero-Logs и аудит разрешений",
                        subtitle = "Прозрачное техническое обоснование каждого системного доступа",
                        icon = Icons.Default.Security,
                        onClick = onOpenPrivacy
                    )
                }
            }
        }

        // Section: Interface
        LuxurySectionHeader(title = "КОНФИГУРАЦИЯ ПРИЛОЖЕНИЯ")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, TitaniumBorder)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(TitaniumCard, TitaniumSurface)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Тема оформления", style = MaterialTheme.typography.bodyMedium, color = TextLuxuryPrimary)
                        Text(
                            text = if (settings.appTheme == "DARK") "Onyx Titanium" else "Light Architectural",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = SapphireAccent
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Язык интерфейса", style = MaterialTheme.typography.bodyMedium, color = TextLuxuryPrimary)
                        Text(
                            text = if (settings.appLanguage == "RU") "Русский" else "English",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = SapphireAccent
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun LuxurySectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            fontSize = 10.sp
        ),
        color = TextLuxuryTertiary,
        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
    )
}

@Composable
fun LuxuryToggleRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextLuxuryPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextLuxuryTertiary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextLuxuryPrimary,
                checkedTrackColor = SapphireAccent,
                uncheckedThumbColor = TextLuxuryTertiary,
                uncheckedTrackColor = TitaniumElevated
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
fun LuxuryActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SapphireAccent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextLuxuryPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextLuxuryTertiary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = TextLuxuryTertiary,
            modifier = Modifier.size(11.dp)
        )
    }
}
