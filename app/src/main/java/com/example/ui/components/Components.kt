package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.EmeraldLuxury
import com.example.ui.theme.EmeraldLuxuryBorder
import com.example.ui.theme.EmeraldLuxurySubtle
import com.example.ui.theme.OnyxDeep
import com.example.ui.theme.SapphireAccent
import com.example.ui.theme.SapphireBorder
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
import com.example.vpn.model.EngineStatus
import com.example.vpn.model.VpnProtocol

fun getCountryFlagEmoji(countryCode: String): String {
    return when (countryCode.uppercase()) {
        "US" -> "🇺🇸"
        "DE" -> "🇩🇪"
        "NL" -> "🇳🇱"
        "GB" -> "🇬🇧"
        "SG" -> "🇸🇬"
        "JP" -> "🇯🇵"
        "PL" -> "🇵🇱"
        "CH" -> "🇨🇭"
        "FR" -> "🇫🇷"
        "CA" -> "🇨🇦"
        "SE" -> "🇸🇪"
        "FI" -> "🇫🇮"
        "UA" -> "🇺🇦"
        "IT" -> "🇮🇹"
        "ES" -> "🇪🇸"
        "AU" -> "🇦🇺"
        "WG" -> "⚡"
        "OVPN" -> "🔒"
        "SS" -> "🛡️"
        "SK5" -> "🌐"
        "HTTP" -> "🔗"
        else -> "🌐"
    }
}

fun formatSpeed(bytesPerSec: Long): String {
    return when {
        bytesPerSec >= 1024 * 1024 -> String.format("%.1f MB/s", bytesPerSec / (1024.0 * 1024))
        bytesPerSec >= 1024 -> String.format("%.0f KB/s", bytesPerSec / 1024.0)
        else -> "$bytesPerSec B/s"
    }
}

fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 * 1024 -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
        bytes >= 1024 -> String.format("%.0f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}

fun formatDuration(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        String.format("%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmniTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    connectionState: ConnectionState? = null,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Luxury Monogram Token
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(TitaniumElevated, TitaniumCard)
                            )
                        )
                        .border(1.dp, TitaniumBorderHighlight, RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "OmniVPN Logo",
                        tint = SapphireAccent,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp
                            ),
                            color = TextLuxuryPrimary
                        )
                        if (connectionState != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            LuxuryStatusPill(state = connectionState)
                        }
                    }
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextLuxuryTertiary,
                            maxLines = 1
                        )
                    }
                }
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("nav_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = TextLuxurySecondary
                    )
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun LuxuryCard(
    modifier: Modifier = Modifier,
    borderColor: Color = TitaniumBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, borderColor)
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
            content()
        }
    }
}

@Composable
fun LuxuryStatusPill(state: ConnectionState) {
    val (color, text) = when (state) {
        ConnectionState.CONNECTED -> Pair(EmeraldLuxury, "SECURE")
        ConnectionState.CONNECTING -> Pair(AmberWarning, "CONNECTING")
        ConnectionState.RECONNECTING -> Pair(AmberWarning, "RECONNECTING")
        ConnectionState.DISCONNECTING -> Pair(TextLuxuryTertiary, "DISCONNECTING")
        ConnectionState.ERROR -> Pair(CoralAlert, "ALERT")
        ConnectionState.DISCONNECTED -> Pair(TextLuxuryTertiary, "STANDBY")
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = Modifier.testTag("status_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                ),
                color = color
            )
        }
    }
}

@Composable
fun PrecisionPingBadge(pingMs: Long?, isTesting: Boolean = false) {
    val (dotColor, text) = when {
        isTesting -> Pair(AmberWarning, "...")
        pingMs == null -> Pair(TextLuxuryDisabled, "–")
        pingMs < 80 -> Pair(EmeraldLuxury, "${pingMs}ms")
        pingMs < 160 -> Pair(AmberWarning, "${pingMs}ms")
        else -> Pair(CoralAlert, "${pingMs}ms")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(TitaniumElevated)
            .border(1.dp, TitaniumBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("ping_badge")
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = TextLuxurySecondary
        )
    }
}

@Composable
fun ProtocolBadge(protocol: VpnProtocol) {
    val (label, tint) = when (protocol) {
        VpnProtocol.ANDROID_PLATFORM -> Pair("L3 TUN", SapphireLight)
        VpnProtocol.SOCKS5_PROXY -> Pair("SOCKS5", EmeraldLuxury)
        VpnProtocol.HTTP_PROXY -> Pair("HTTP", EmeraldLuxury)
        VpnProtocol.IKEV2_IPSEC -> Pair("IKEv2", AmberWarning)
        VpnProtocol.WIREGUARD -> Pair("WIREGUARD", SapphireLight)
        VpnProtocol.OPENVPN -> Pair("OPENVPN", AmberWarning)
        VpnProtocol.SHADOWSOCKS -> Pair("SHADOWSOCKS", Color(0xFFC084FC))
        VpnProtocol.AMNEZIA_WG -> Pair("AMNEZIA", Color(0xFFF472B6))
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = TitaniumElevated,
        border = BorderStroke(1.dp, TitaniumBorder)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 0.8.sp
            ),
            color = tint
        )
    }
}

@Composable
fun EngineStatusBadge(status: EngineStatus) {
    val (color, text) = when (status) {
        EngineStatus.ACTIVE_SUPPORTED -> Pair(EmeraldLuxury, status.label)
        EngineStatus.ACTIVE_PROXIED -> Pair(SapphireLight, status.label)
        EngineStatus.REQUIRES_API_30 -> Pair(AmberWarning, status.label)
        EngineStatus.REQUIRES_NDK_BINARY -> Pair(TextLuxuryTertiary, status.label)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = color
        )
    }
}
