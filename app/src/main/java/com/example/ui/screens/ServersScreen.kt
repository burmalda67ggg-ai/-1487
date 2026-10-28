package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PrecisionPingBadge
import com.example.ui.components.ProtocolBadge
import com.example.ui.components.getCountryFlagEmoji
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.EmeraldLuxury
import com.example.ui.theme.EmeraldLuxuryBorder
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
import com.example.vpn.model.VpnProfile

@Composable
fun ServersScreen(
    profiles: List<VpnProfile>,
    selectedProfile: VpnProfile?,
    isPinging: Boolean,
    onSelectProfile: (VpnProfile) -> Unit,
    onRefreshPings: () -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val regionFiltered = remember(profiles, selectedFilter) {
        when (selectedFilter) {
            "FAVORITES" -> profiles.filter { it.isFavorite }
            "EUROPE" -> profiles.filter { it.countryCode in listOf("DE", "NL", "GB", "PL", "CH", "FR", "SE", "FI", "IT", "ES") }
            "AMERICAS" -> profiles.filter { it.countryCode in listOf("US", "CA") }
            "ASIA" -> profiles.filter { it.countryCode in listOf("SG", "JP") }
            else -> profiles
        }
    }

    val finalProfiles = remember(regionFiltered, searchQuery) {
        if (searchQuery.isBlank()) regionFiltered
        else regionFiltered.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.countryName.contains(searchQuery, ignoreCase = true) ||
            it.city.contains(searchQuery, ignoreCase = true) ||
            it.serverHost.contains(searchQuery, ignoreCase = true) ||
            it.protocol.displayName.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Luxury Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Поиск локации, страны или протокола...", color = TextLuxuryTertiary, fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Поиск",
                    tint = TextLuxurySecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Очистить",
                            tint = TextLuxurySecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("server_search_input"),
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

        Spacer(modifier = Modifier.height(10.dp))

        // Region Filter Pills with Champagne & Sapphire highlights
        val filterScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(filterScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "ALL" to "ВСЕ УЗЛЫ (${profiles.size})",
                "FAVORITES" to "ИЗБРАННОЕ ★",
                "EUROPE" to "ЕВРОПА",
                "AMERICAS" to "АМЕРИКА",
                "ASIA" to "АЗИЯ"
            ).forEach { (key, label) ->
                val isSelected = selectedFilter == key
                val isFav = key == "FAVORITES"
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = key },
                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (isFav) ChampagneGold.copy(alpha = 0.15f) else SapphireSubtle,
                        selectedLabelColor = if (isFav) ChampagneGold else SapphireAccent,
                        containerColor = TitaniumSurface,
                        labelColor = TextLuxurySecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) (if (isFav) ChampagneGold.copy(alpha = 0.5f) else SapphireBorder) else TitaniumBorder,
                        selectedBorderColor = if (isFav) ChampagneGold.copy(alpha = 0.6f) else SapphireBorder,
                        borderWidth = 1.dp,
                        enabled = true,
                        selected = isSelected
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Header Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${finalProfiles.size} ЛОКАЦИЙ ОНЛАЙН",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontSize = 10.sp
                ),
                color = TextLuxuryTertiary
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TitaniumSurface,
                border = BorderStroke(1.dp, TitaniumBorder),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = !isPinging, onClick = onRefreshPings)
                    .testTag("ping_all_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPinging) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.5.dp,
                            color = SapphireAccent
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ЗАМЕР...",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = TextLuxurySecondary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Измерить пинг",
                            tint = SapphireAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ЗАМЕРИТЬ ПИНГ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = TextLuxuryPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Server List
        if (finalProfiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "Серверы не найдены в выбранной категории",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextLuxuryTertiary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(finalProfiles, key = { it.id }) { profile ->
                    LuxuryServerRow(
                        profile = profile,
                        isSelected = selectedProfile?.id == profile.id,
                        isTesting = isPinging,
                        onClick = { onSelectProfile(profile) },
                        onToggleFavorite = { onToggleFavorite(profile.id, !profile.isFavorite) }
                    )
                }
            }
        }
    }
}

@Composable
fun LuxuryServerRow(
    profile: VpnProfile,
    isSelected: Boolean,
    isTesting: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val borderColor = if (isSelected) EmeraldLuxuryBorder else TitaniumBorder

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("server_item_${profile.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, borderColor)
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
                // National Flag Token
                val flag = getCountryFlagEmoji(profile.countryCode)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TitaniumElevated)
                        .border(1.dp, TitaniumBorderHighlight, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = flag, fontSize = 21.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Node Specifications
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextLuxuryPrimary
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Выбран",
                                tint = EmeraldLuxury,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProtocolBadge(protocol = profile.protocol)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${profile.city} • ${profile.serverHost}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextLuxuryTertiary,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Chronometer Readout & Favorite
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PrecisionPingBadge(pingMs = profile.pingMs, isTesting = isTesting)
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(34.dp)) {
                        Icon(
                            imageVector = if (profile.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Избранное",
                            tint = if (profile.isFavorite) ChampagneGold else TextLuxuryDisabled,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
