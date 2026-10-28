package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.ui.theme.EmeraldLuxury
import com.example.ui.theme.SapphireAccent
import com.example.ui.theme.SapphireBorder
import com.example.ui.theme.SapphireSubtle
import com.example.ui.theme.TextLuxuryDisabled
import com.example.ui.theme.TextLuxuryPrimary
import com.example.ui.theme.TextLuxurySecondary
import com.example.ui.theme.TextLuxuryTertiary
import com.example.ui.theme.TitaniumBorder
import com.example.ui.theme.TitaniumBorderHighlight
import com.example.ui.theme.TitaniumCard
import com.example.ui.theme.TitaniumElevated
import com.example.ui.theme.TitaniumSurface
import com.example.ui.viewmodel.InstalledAppItem

@Composable
fun SplitTunnelScreen(
    currentMode: String,
    selectedApps: Set<String>,
    installedApps: List<InstalledAppItem>,
    isLoading: Boolean,
    onModeChange: (String) -> Unit,
    onToggleApp: (String) -> Unit
) {
    var searchFilter by remember { mutableStateOf("") }

    val filteredApps = remember(installedApps, searchFilter) {
        if (searchFilter.isBlank()) installedApps
        else installedApps.filter {
            it.name.contains(searchFilter, ignoreCase = true) ||
            it.packageName.contains(searchFilter, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = "РАЗДЕЛЬНОЕ ТУННЕЛИРОВАНИЕ",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                fontSize = 11.sp
            ),
            color = SapphireAccent
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "Маршрутизация трафика отдельных приложений в обход или через туннель",
            style = MaterialTheme.typography.bodySmall,
            color = TextLuxuryTertiary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Mode Chips with luxury styling
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "OFF" to "ВЫКЛЮЧЕНО",
                "INCLUDE" to "ТОЛЬКО ВЫБРАННЫЕ",
                "EXCLUDE" to "ИСКЛЮЧИТЬ"
            ).forEach { (modeKey, modeLabel) ->
                val isSelected = currentMode == modeKey
                FilterChip(
                    selected = isSelected,
                    onClick = { onModeChange(modeKey) },
                    label = {
                        Text(
                            text = modeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SapphireSubtle,
                        selectedLabelColor = SapphireAccent,
                        containerColor = TitaniumSurface,
                        labelColor = TextLuxurySecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) SapphireBorder else TitaniumBorder,
                        selectedBorderColor = SapphireBorder,
                        borderWidth = 1.dp,
                        enabled = true,
                        selected = isSelected
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (currentMode != "OFF") {
            // App Search
            OutlinedTextField(
                value = searchFilter,
                onValueChange = { searchFilter = it },
                placeholder = { Text("Поиск установленного приложения...", color = TextLuxuryTertiary, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextLuxurySecondary, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchFilter.isNotEmpty()) {
                        IconButton(onClick = { searchFilter = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextLuxurySecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_search_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SapphireAccent,
                    unfocusedBorderColor = TitaniumBorder,
                    focusedContainerColor = TitaniumSurface,
                    unfocusedContainerColor = TitaniumSurface,
                    focusedTextColor = TextLuxuryPrimary,
                    unfocusedTextColor = TextLuxuryPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "АКТИВНО: ${selectedApps.size} ИЗ ${installedApps.size}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                    ),
                    color = TextLuxuryTertiary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SapphireAccent, modifier = Modifier.size(28.dp))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { appItem ->
                        val isChecked = selectedApps.contains(appItem.packageName)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            border = BorderStroke(1.dp, if (isChecked) SapphireBorder else TitaniumBorder)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Brush.verticalGradient(
                                            colors = if (isChecked) listOf(TitaniumElevated, TitaniumCard)
                                            else listOf(TitaniumCard, TitaniumSurface)
                                        )
                                    )
                                    .padding(horizontal = 14.dp, vertical = 11.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val bitmap = remember(appItem.packageName) {
                                        runCatching { appItem.icon?.toBitmap(96, 96)?.asImageBitmap() }.getOrNull()
                                    }
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = appItem.name,
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(9.dp))
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(9.dp))
                                                .background(TitaniumElevated),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(20.dp), tint = TextLuxurySecondary)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = appItem.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = TextLuxuryPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = appItem.packageName,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = TextLuxuryTertiary,
                                            maxLines = 1
                                        )
                                    }

                                    Switch(
                                        checked = isChecked,
                                        onCheckedChange = { onToggleApp(appItem.packageName) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = TextLuxuryPrimary,
                                            checkedTrackColor = SapphireAccent,
                                            uncheckedThumbColor = TextLuxuryTertiary,
                                            uncheckedTrackColor = TitaniumElevated
                                        ),
                                        modifier = Modifier.testTag("app_switch_${appItem.packageName}")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(TitaniumElevated)
                            .border(1.dp, TitaniumBorderHighlight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = TextLuxuryDisabled,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Полный охват сети",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextLuxuryPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Весь входящий и исходящий трафик устройства защищается глобальным туннелем OmniVPN.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLuxuryTertiary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
