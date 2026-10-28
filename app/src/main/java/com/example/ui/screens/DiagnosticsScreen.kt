package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.components.EngineStatusBadge
import com.example.ui.components.ProtocolBadge
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningSubtle
import com.example.ui.theme.EmeraldLuxury
import com.example.ui.theme.EmeraldLuxurySubtle
import com.example.ui.theme.SapphireAccent
import com.example.ui.theme.TextLuxuryPrimary
import com.example.ui.theme.TextLuxurySecondary
import com.example.ui.theme.TextLuxuryTertiary
import com.example.ui.theme.TitaniumBorder
import com.example.ui.theme.TitaniumCard
import com.example.ui.theme.TitaniumElevated
import com.example.ui.theme.TitaniumSurface
import com.example.vpn.model.DiagnosticsResult
import com.example.vpn.model.EngineInfo

@Composable
fun DiagnosticsScreen(
    diagnostics: DiagnosticsResult,
    protocolMatrix: List<EngineInfo>,
    onRunDiagnostics: () -> Unit,
    onCustomPing: (String, Int) -> Unit
) {
    val scrollState = rememberScrollState()
    var pingHost by remember { mutableStateOf("1.1.1.1") }
    var pingPort by remember { mutableStateOf("443") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Live Diagnostics Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
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
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NetworkCheck,
                                contentDescription = null,
                                tint = SapphireAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ДИАГНОСТИКА СЕТИ",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 11.sp
                                ),
                                color = TextLuxuryPrimary
                            )
                        }

                        Button(
                            onClick = onRunDiagnostics,
                            enabled = !diagnostics.isChecking,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireAccent),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("run_diagnostics_button")
                        ) {
                            if (diagnostics.isChecking) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = TextLuxuryPrimary)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Проверить", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Status Message Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (diagnostics.isVpnActive) EmeraldLuxurySubtle else AmberWarningSubtle)
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (diagnostics.isVpnActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (diagnostics.isVpnActive) EmeraldLuxury else AmberWarning,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = diagnostics.statusMessage.ifBlank { "Запустите проверку параметров сети" },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (diagnostics.isVpnActive) EmeraldLuxury else AmberWarning
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LuxuryDiagRow(label = "Внешний IP адрес", value = diagnostics.externalIp ?: "Не определен", isHighlight = true)
                    LuxuryDiagRow(label = "Активный интерфейс", value = diagnostics.isp ?: "–")
                    LuxuryDiagRow(label = "Виртуальный туннель", value = if (diagnostics.isVpnActive) "АКТИВЕН (tun0)" else "НЕ АКТИВЕН")
                    LuxuryDiagRow(label = "Задержка (1.1.1.1)", value = diagnostics.pingLatencyMs?.let { "${it} ms" } ?: "–")
                    LuxuryDiagRow(
                        label = "DNS Резолверы",
                        value = if (diagnostics.dnsServers.isNotEmpty()) diagnostics.dnsServers.joinToString(", ") else "Защищен туннелем"
                    )
                }
            }
        }

        // Custom Ping Tool Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
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
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ТОЧНЫЙ СОКЕТНЫЙ ПИНГ (TCP)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 10.sp
                            ),
                            color = TextLuxuryPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Прямой замер миллисекунд соединения сокета к целевому шлюзу.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLuxuryTertiary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pingHost,
                            onValueChange = { pingHost = it },
                            placeholder = { Text("Хост или IP", color = TextLuxuryTertiary) },
                            modifier = Modifier
                                .weight(2f)
                                .testTag("ping_host_input"),
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
                        OutlinedTextField(
                            value = pingPort,
                            onValueChange = { pingPort = it },
                            placeholder = { Text("Порт", color = TextLuxuryTertiary) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ping_port_input"),
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
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onCustomPing(pingHost, pingPort.toIntOrNull() ?: 443) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_custom_ping_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TitaniumElevated, contentColor = TextLuxuryPrimary),
                        border = BorderStroke(1.dp, TitaniumBorder)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = SapphireAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Измерить задержку", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Protocol Matrix Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "ТЕХНИЧЕСКИЙ СТАТУС ДВИЖКОВ",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, fontSize = 10.sp),
                color = TextLuxuryTertiary,
                modifier = Modifier.padding(start = 4.dp)
            )

            protocolMatrix.forEach { engine ->
                LuxuryProtocolCard(engine)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun LuxuryDiagRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextLuxuryTertiary)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium),
            color = if (isHighlight) SapphireAccent else TextLuxuryPrimary
        )
    }
}

@Composable
fun LuxuryProtocolCard(engine: EngineInfo) {
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
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = engine.displayName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextLuxuryPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ProtocolBadge(protocol = engine.protocol)
                    }
                    EngineStatusBadge(status = engine.status)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = engine.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLuxurySecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Лицензия: ${engine.license} • ${engine.requirements}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextLuxuryTertiary
                )
            }
        }
    }
}
