package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LuxuryCard
import com.example.ui.components.PrecisionPingBadge
import com.example.ui.components.ProtocolBadge
import com.example.ui.components.formatBytes
import com.example.ui.components.formatDuration
import com.example.ui.components.formatSpeed
import com.example.ui.components.getCountryFlagEmoji
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningSubtle
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ChampagneGoldSubtle
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldLuxury
import com.example.ui.theme.EmeraldLuxuryBorder
import com.example.ui.theme.EmeraldLuxurySubtle
import com.example.ui.theme.OnyxDeep
import com.example.ui.theme.SapphireAccent
import com.example.ui.theme.SapphireBorder
import com.example.ui.theme.SapphireGlow
import com.example.ui.theme.SapphireLight
import com.example.ui.theme.SapphireSubtle
import com.example.ui.theme.TextLuxuryDisabled
import com.example.ui.theme.TextLuxuryPrimary
import com.example.ui.theme.TextLuxurySecondary
import com.example.ui.theme.TextLuxuryTertiary
import com.example.ui.theme.TitaniumActive
import com.example.ui.theme.TitaniumBorder
import com.example.ui.theme.TitaniumBorderHighlight
import com.example.ui.theme.TitaniumCard
import com.example.ui.theme.TitaniumElevated
import com.example.ui.theme.TitaniumSurface
import com.example.vpn.model.ConnectionState
import com.example.vpn.model.TrafficStatsSnapshot
import com.example.vpn.model.VpnProfile

@Composable
fun HomeScreen(
    connectionState: ConnectionState,
    currentProfile: VpnProfile?,
    trafficStats: TrafficStatsSnapshot,
    lastError: String?,
    externalIp: String?,
    onConnectToggle: () -> Unit,
    onSelectServerClick: () -> Unit,
    onAutoBestClick: () -> Unit,
    onRefreshIp: () -> Unit,
    onClearError: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Subtle Error Banner
        AnimatedVisibility(visible = lastError != null) {
            if (lastError != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CoralAlert.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, CoralAlert.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("error_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lastError,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLuxuryPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onClearError, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Закрыть",
                                tint = CoralAlert,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        // Luxury Centerpiece: Crown Power Dial & Hub
        LuxuryCrownHub(
            connectionState = connectionState,
            currentProfile = currentProfile,
            onConnectToggle = onConnectToggle
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Node Token Card
        LuxuryCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("active_server_card"),
            borderColor = if (connectionState == ConnectionState.CONNECTED) EmeraldLuxuryBorder else TitaniumBorder,
            onClick = onSelectServerClick
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // National Flag Token in Chamfered Titanium housing
                val flag = getCountryFlagEmoji(currentProfile?.countryCode ?: "US")
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(TitaniumElevated, TitaniumCard)
                            )
                        )
                        .border(1.dp, TitaniumBorderHighlight, RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = flag, fontSize = 23.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentProfile?.name ?: "Выберите сервер",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextLuxuryPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        currentProfile?.protocol?.let {
                            ProtocolBadge(protocol = it)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "${currentProfile?.city ?: "Auto"} • ${currentProfile?.serverHost ?: "–"}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextLuxuryTertiary,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    PrecisionPingBadge(pingMs = currentProfile?.pingMs)
                    Spacer(modifier = Modifier.height(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Сменить сервер",
                        tint = TextLuxuryTertiary,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Smart Optimal Node Bar
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, TitaniumBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAutoBestClick)
                .testTag("auto_best_button")
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(TitaniumCard, TitaniumSurface)
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ChampagneGoldSubtle)
                                .border(1.dp, ChampagneGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = ChampagneGold,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Интеллектуальный маршрут",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextLuxuryPrimary
                            )
                            Text(
                                text = "Автоматический выбор узла с минимальной задержкой",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextLuxuryTertiary
                            )
                        }
                    }

                    Text(
                        text = "OPTIMAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.2.sp,
                            color = ChampagneGold
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ChampagneGoldSubtle)
                            .border(1.dp, ChampagneGold.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Precision Telemetry Instruments (2x2 Monolithic Grid)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InstrumentGaugeCard(
                modifier = Modifier.weight(1f),
                label = "DOWNLINK",
                mainValue = formatSpeed(trafficStats.rxSpeedBytesPerSec),
                detail = "Volume: ${formatBytes(trafficStats.rxBytes)}",
                icon = Icons.Default.ArrowDownward,
                accent = EmeraldLuxury,
                tag = "metric_dl"
            )

            InstrumentGaugeCard(
                modifier = Modifier.weight(1f),
                label = "UPLINK",
                mainValue = formatSpeed(trafficStats.txSpeedBytesPerSec),
                detail = "Volume: ${formatBytes(trafficStats.txBytes)}",
                icon = Icons.Default.ArrowUpward,
                accent = SapphireLight,
                tag = "metric_ul"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InstrumentGaugeCard(
                modifier = Modifier.weight(1f),
                label = "SESSION TIME",
                mainValue = formatDuration(trafficStats.sessionDurationSeconds),
                detail = if (connectionState == ConnectionState.CONNECTED) "Tunnel active" else "Standby",
                icon = Icons.Default.Timer,
                accent = AmberWarning,
                tag = "metric_time"
            )

            // External IP Verified Readout
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("metric_ip"),
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
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PUBLIC IP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 9.sp
                                ),
                                color = TextLuxuryTertiary
                            )
                            IconButton(onClick = onRefreshIp, modifier = Modifier.size(20.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Обновить IP",
                                    tint = TextLuxurySecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = externalIp ?: "Querying...",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (connectionState == ConnectionState.CONNECTED) EmeraldLuxury else TextLuxuryPrimary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (connectionState == ConnectionState.CONNECTED) "Encrypted Tunnel" else "Direct Carrier",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextLuxuryTertiary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun LuxuryCrownHub(
    connectionState: ConnectionState,
    currentProfile: VpnProfile?,
    onConnectToggle: () -> Unit
) {
    val isConnected = connectionState == ConnectionState.CONNECTED
    val isConnecting = connectionState == ConnectionState.CONNECTING || connectionState == ConnectionState.RECONNECTING

    val cardBorderColor by animateColorAsState(
        targetValue = when {
            isConnected -> EmeraldLuxuryBorder
            isConnecting -> AmberWarning.copy(alpha = 0.35f)
            else -> TitaniumBorder
        },
        label = "crownBorder"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_connection_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(TitaniumCard, TitaniumSurface)
                    )
                )
                .padding(vertical = 26.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // The Luxury Machined Power Crown
                MachinedPowerCrown(
                    state = connectionState,
                    onClick = onConnectToggle
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Primary state headline in tracked uppercase
                Text(
                    text = when {
                        isConnected -> "SHIELD ACTIVE"
                        isConnecting -> "SYNCHRONIZING..."
                        connectionState == ConnectionState.ERROR -> "CONNECTION ALERT"
                        else -> "PROTECTION STANDBY"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 12.sp
                    ),
                    color = when {
                        isConnected -> EmeraldLuxury
                        isConnecting -> AmberWarning
                        connectionState == ConnectionState.ERROR -> CoralAlert
                        else -> TextLuxurySecondary
                    }
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = when {
                        isConnected -> "256-Bit Encrypted Virtual Tunnel • IP Masked"
                        isConnecting -> "Negotiating cryptographic handshake"
                        connectionState == ConnectionState.ERROR -> "Gateway unreachable. Check route or settings."
                        else -> "Tap the crown to initiate secure private routing"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = TextLuxuryTertiary
                )
            }
        }
    }
}

@Composable
fun MachinedPowerCrown(
    state: ConnectionState,
    onClick: () -> Unit
) {
    val isConnected = state == ConnectionState.CONNECTED
    val isConnecting = state == ConnectionState.CONNECTING || state == ConnectionState.RECONNECTING

    val infiniteTransition = rememberInfiniteTransition(label = "crownPulse")
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnecting) 1.05f else if (isConnected) 1.02f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientPulse"
    )

    val jewelAccent by animateColorAsState(
        targetValue = when {
            isConnected -> EmeraldLuxury
            isConnecting -> AmberWarning
            state == ConnectionState.ERROR -> CoralAlert
            else -> SapphireAccent
        },
        label = "jewelAccent"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(126.dp)
            .scale(ambientPulse)
    ) {
        // Ambient soft halo
        Box(
            modifier = Modifier
                .size(126.dp)
                .clip(CircleShape)
                .background(
                    if (isConnected) EmeraldGlow
                    else if (isConnecting) AmberWarningSubtle
                    else SapphireGlow
                )
        )

        // Outer precision titanium bezel
        Box(
            modifier = Modifier
                .size(114.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(TitaniumElevated, TitaniumSurface)
                    )
                )
                .border(
                    BorderStroke(
                        width = 1.dp,
                        color = if (isConnected) EmeraldLuxuryBorder else TitaniumBorderHighlight
                    ),
                    shape = CircleShape
                )
        )

        // Inner tactile crown core
        Surface(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick)
                .testTag("vpn_power_button"),
            shape = CircleShape,
            color = Color.Transparent,
            border = BorderStroke(
                width = if (isConnected) 2.dp else 1.dp,
                color = if (isConnected) EmeraldLuxury else SapphireBorder
            )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(TitaniumActive, TitaniumSurface)
                        )
                    )
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(38.dp),
                        strokeWidth = 2.dp,
                        color = AmberWarning
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Power switch",
                        tint = jewelAccent,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InstrumentGaugeCard(
    modifier: Modifier = Modifier,
    label: String,
    mainValue: String,
    detail: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    tag: String
) {
    Card(
        modifier = modifier.testTag(tag),
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
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 9.sp
                        ),
                        color = TextLuxuryTertiary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = mainValue,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextLuxuryPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextLuxuryTertiary
                )
            }
        }
    }
}
