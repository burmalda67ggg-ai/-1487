package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ProtocolBadge
import com.example.ui.components.formatBytes
import com.example.ui.components.getCountryFlagEmoji
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.EmeraldLuxury
import com.example.ui.theme.EmeraldLuxuryBorder
import com.example.ui.theme.SapphireAccent
import com.example.ui.theme.SapphireBorder
import com.example.ui.theme.TextLuxuryDisabled
import com.example.ui.theme.TextLuxuryPrimary
import com.example.ui.theme.TextLuxurySecondary
import com.example.ui.theme.TextLuxuryTertiary
import com.example.ui.theme.TitaniumBorder
import com.example.ui.theme.TitaniumBorderHighlight
import com.example.ui.theme.TitaniumCard
import com.example.ui.theme.TitaniumElevated
import com.example.ui.theme.TitaniumSurface
import com.example.vpn.model.ConnectionHistoryItem
import com.example.vpn.model.VpnProfile
import com.example.vpn.model.VpnProtocol
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfilesScreen(
    profiles: List<VpnProfile>,
    selectedProfile: VpnProfile?,
    history: List<ConnectionHistoryItem>,
    onSelectProfile: (VpnProfile) -> Unit,
    onSaveProfile: (VpnProfile) -> Unit,
    onDeleteProfile: (VpnProfile) -> Unit,
    onImportText: (String, String?) -> Result<VpnProfile>,
    onClearHistory: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }
    var exportProfile by remember { mutableStateOf<VpnProfile?>(null) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // High-End Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = TitaniumSurface,
            contentColor = SapphireAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = SapphireAccent
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "ПРОФИЛИ (${profiles.size})",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = 1.sp,
                        color = if (selectedTab == 0) TextLuxuryPrimary else TextLuxuryTertiary,
                        fontSize = 11.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "ЖУРНАЛ СЕССИЙ",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = 1.sp,
                        color = if (selectedTab == 1) TextLuxuryPrimary else TextLuxuryTertiary,
                        fontSize = 11.sp
                    )
                }
            )
        }

        if (selectedTab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Actions Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showImportDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("import_config_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireAccent)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Импорт", color = TextLuxuryPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showManualDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manual_profile_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, TitaniumBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextLuxuryPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Создать", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(profiles, key = { it.id }) { profile ->
                        LuxuryProfileItem(
                            profile = profile,
                            isSelected = selectedProfile?.id == profile.id,
                            onSelect = { onSelectProfile(profile) },
                            onExport = { exportProfile = profile },
                            onDelete = { onDeleteProfile(profile) }
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ХРОНОЛОГИЯ ПОДКЛЮЧЕНИЙ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        ),
                        color = TextLuxuryTertiary
                    )
                    if (history.isNotEmpty()) {
                        TextButton(onClick = onClearHistory) {
                            Text("Очистить", color = CoralAlert, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = TextLuxuryDisabled,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Журнал сессий пуст",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextLuxuryTertiary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(history, key = { it.id }) { item ->
                            LuxuryHistoryRow(item)
                        }
                    }
                }
            }
        }
    }

    // Import Dialog
    if (showImportDialog) {
        ImportConfigDialog(
            onDismiss = { showImportDialog = false },
            onImport = { text, name ->
                val result = onImportText(text, name)
                if (result.isSuccess) {
                    Toast.makeText(context, "Профиль успешно импортирован", Toast.LENGTH_SHORT).show()
                    showImportDialog = false
                } else {
                    Toast.makeText(context, result.exceptionOrNull()?.message ?: "Ошибка импорта", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    // Manual Creation Dialog
    if (showManualDialog) {
        ManualProfileDialog(
            onDismiss = { showManualDialog = false },
            onSave = { profile ->
                onSaveProfile(profile)
                Toast.makeText(context, "Профиль '${profile.name}' сохранен", Toast.LENGTH_SHORT).show()
                showManualDialog = false
            }
        )
    }

    // Export Dialog
    exportProfile?.let { prof ->
        ExportConfigDialog(
            profile = prof,
            onDismiss = { exportProfile = null },
            onCopy = {
                val textToCopy = prof.rawConfig ?: """
                    # OmniVPN Profile: ${prof.name}
                    [Connection]
                    Protocol = ${prof.protocol.displayName}
                    Host = ${prof.serverHost}
                    Port = ${prof.serverPort}
                    Username = ${prof.username ?: ""}
                    DNS = ${prof.dnsServer ?: "1.1.1.1"}
                    MTU = ${prof.mtu}
                """.trimIndent()
                clipboardManager.setText(AnnotatedString(textToCopy))
                Toast.makeText(context, "Конфигурация скопирована в буфер", Toast.LENGTH_SHORT).show()
                exportProfile = null
            }
        )
    }
}

@Composable
fun LuxuryProfileItem(
    profile: VpnProfile,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("profile_card_${profile.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, if (isSelected) EmeraldLuxuryBorder else TitaniumBorder)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = if (isSelected) listOf(TitaniumElevated, TitaniumCard)
                        else listOf(TitaniumCard, TitaniumSurface)
                    )
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val flag = getCountryFlagEmoji(profile.countryCode)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(TitaniumElevated)
                        .border(1.dp, TitaniumBorderHighlight, RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = flag, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextLuxuryPrimary
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Check, contentDescription = "Выбран", tint = EmeraldLuxury, modifier = Modifier.size(15.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProtocolBadge(protocol = profile.protocol)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${profile.serverHost}:${profile.serverPort}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextLuxuryTertiary,
                            maxLines = 1
                        )
                    }
                }

                IconButton(onClick = onExport, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Экспорт", tint = TextLuxurySecondary, modifier = Modifier.size(16.dp))
                }

                if (profile.isCustom) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = CoralAlert.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LuxuryHistoryRow(item: ConnectionHistoryItem) {
    val dateStr = remember(item.connectedAt) {
        if (item.connectedAt > 0) {
            SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(item.connectedAt))
        } else "Недавно"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
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
                .padding(13.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (item.status == "COMPLETED") EmeraldLuxury else CoralAlert)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.serverName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextLuxuryPrimary
                        )
                    }
                    Text(
                        text = "${item.durationSeconds}s",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextLuxuryTertiary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$dateStr • ${item.protocol}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextLuxuryTertiary
                    )
                    Text(
                        text = "↓ ${formatBytes(item.bytesDownloaded)}  ↑ ${formatBytes(item.bytesUploaded)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                        color = SapphireAccent
                    )
                }
            }
        }
    }
}

@Composable
fun ImportConfigDialog(
    onDismiss: () -> Unit,
    onImport: (String, String?) -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    var profileName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TitaniumSurface,
        titleContentColor = TextLuxuryPrimary,
        textContentColor = TextLuxurySecondary,
        title = { Text("Импорт конфигурации") },
        text = {
            Column {
                Text(
                    text = "Поддерживаются форматы .conf, .ovpn, ss://, socks5://, http://",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLuxuryTertiary
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    placeholder = { Text("Название профиля (опционально)", color = TextLuxuryTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SapphireAccent,
                        unfocusedBorderColor = TitaniumBorder,
                        focusedTextColor = TextLuxuryPrimary,
                        unfocusedTextColor = TextLuxuryPrimary,
                        focusedContainerColor = TitaniumCard,
                        unfocusedContainerColor = TitaniumCard
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    placeholder = { Text("Конфигурация или URI...", color = TextLuxuryTertiary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SapphireAccent,
                        unfocusedBorderColor = TitaniumBorder,
                        focusedTextColor = TextLuxuryPrimary,
                        unfocusedTextColor = TextLuxuryPrimary,
                        focusedContainerColor = TitaniumCard,
                        unfocusedContainerColor = TitaniumCard
                    ),
                    maxLines = 8
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onImport(rawText, profileName.ifBlank { null }) },
                enabled = rawText.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SapphireAccent)
            ) {
                Text("Импортировать", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = TextLuxurySecondary) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualProfileDialog(
    onDismiss: () -> Unit,
    onSave: (VpnProfile) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("1080") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedProtocol by remember { mutableStateOf(VpnProtocol.SOCKS5_PROXY) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TitaniumSurface,
        titleContentColor = TextLuxuryPrimary,
        textContentColor = TextLuxurySecondary,
        title = { Text("Новый профиль") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Название профиля", color = TextLuxuryTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SapphireAccent,
                        unfocusedBorderColor = TitaniumBorder,
                        focusedTextColor = TextLuxuryPrimary,
                        unfocusedTextColor = TextLuxuryPrimary,
                        focusedContainerColor = TitaniumCard,
                        unfocusedContainerColor = TitaniumCard
                    ),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedProtocol.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SapphireAccent,
                            unfocusedBorderColor = TitaniumBorder,
                            focusedTextColor = TextLuxuryPrimary,
                            unfocusedTextColor = TextLuxuryPrimary,
                            focusedContainerColor = TitaniumCard,
                            unfocusedContainerColor = TitaniumCard
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(TitaniumElevated)
                    ) {
                        VpnProtocol.values().forEach { proto ->
                            DropdownMenuItem(
                                text = { Text(proto.displayName, color = TextLuxuryPrimary) },
                                onClick = {
                                    selectedProtocol = proto
                                    expanded = false
                                    if (proto == VpnProtocol.SOCKS5_PROXY) port = "1080"
                                    if (proto == VpnProtocol.HTTP_PROXY) port = "8080"
                                    if (proto == VpnProtocol.WIREGUARD) port = "51820"
                                    if (proto == VpnProtocol.OPENVPN) port = "1194"
                                }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        placeholder = { Text("Хост", color = TextLuxuryTertiary) },
                        modifier = Modifier.weight(2f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SapphireAccent,
                            unfocusedBorderColor = TitaniumBorder,
                            focusedTextColor = TextLuxuryPrimary,
                            unfocusedTextColor = TextLuxuryPrimary,
                            focusedContainerColor = TitaniumCard,
                            unfocusedContainerColor = TitaniumCard
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        placeholder = { Text("Порт", color = TextLuxuryTertiary) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SapphireAccent,
                            unfocusedBorderColor = TitaniumBorder,
                            focusedTextColor = TextLuxuryPrimary,
                            unfocusedTextColor = TextLuxuryPrimary,
                            focusedContainerColor = TitaniumCard,
                            unfocusedContainerColor = TitaniumCard
                        ),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = { Text("Логин (опционально)", color = TextLuxuryTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SapphireAccent,
                        unfocusedBorderColor = TitaniumBorder,
                        focusedTextColor = TextLuxuryPrimary,
                        unfocusedTextColor = TextLuxuryPrimary,
                        focusedContainerColor = TitaniumCard,
                        unfocusedContainerColor = TitaniumCard
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Пароль (опционально)", color = TextLuxuryTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SapphireAccent,
                        unfocusedBorderColor = TitaniumBorder,
                        focusedTextColor = TextLuxuryPrimary,
                        unfocusedTextColor = TextLuxuryPrimary,
                        focusedContainerColor = TitaniumCard,
                        unfocusedContainerColor = TitaniumCard
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        VpnProfile(
                            name = name.ifBlank { "Профиль $host" },
                            protocol = selectedProtocol,
                            serverHost = host,
                            serverPort = port.toIntOrNull() ?: 1080,
                            username = username.ifBlank { null },
                            password = password.ifBlank { null },
                            isCustom = true,
                            countryCode = "MAN",
                            countryName = "Пользовательский",
                            city = host
                        )
                    )
                },
                enabled = host.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SapphireAccent)
            ) {
                Text("Сохранить", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = TextLuxurySecondary) }
        }
    )
}

@Composable
fun ExportConfigDialog(
    profile: VpnProfile,
    onDismiss: () -> Unit,
    onCopy: () -> Unit
) {
    val exportContent = profile.rawConfig ?: """
        # OmniVPN Profile: ${profile.name}
        [Interface]
        Protocol = ${profile.protocol.displayName}
        Host = ${profile.serverHost}
        Port = ${profile.serverPort}
        Username = ${profile.username ?: ""}
        DNS = ${profile.dnsServer ?: "1.1.1.1"}
        MTU = ${profile.mtu}
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TitaniumSurface,
        titleContentColor = TextLuxuryPrimary,
        textContentColor = TextLuxurySecondary,
        title = { Text("Экспорт: ${profile.name}") },
        text = {
            Column {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TitaniumCard),
                    border = BorderStroke(1.dp, TitaniumBorder)
                ) {
                    Text(
                        text = exportContent,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextLuxurySecondary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCopy,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SapphireAccent)
            ) {
                Text("Скопировать", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть", color = TextLuxurySecondary) }
        }
    )
}
