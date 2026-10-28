package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LuxuryCard
import com.example.ui.theme.EmeraldLuxury
import com.example.ui.theme.EmeraldLuxuryBorder
import com.example.ui.theme.EmeraldLuxurySubtle
import com.example.ui.theme.SapphireAccent
import com.example.ui.theme.SapphireSubtle
import com.example.ui.theme.TextLuxuryPrimary
import com.example.ui.theme.TextLuxurySecondary
import com.example.ui.theme.TextLuxuryTertiary
import com.example.ui.theme.TitaniumBorder
import com.example.ui.theme.TitaniumElevated

@Composable
fun PrivacyScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Zero Logs Luxury Card
        LuxuryCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = EmeraldLuxuryBorder
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldLuxurySubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = EmeraldLuxury,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ПОЛИТИКА ZERO-LOGS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = TextLuxuryPrimary
                        )
                        Text(
                            text = "Абсолютная приватность на уровне протокола",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextLuxuryTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "OmniVPN строго изолирует и защищает пользовательские метаданные:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLuxurySecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                ZeroLogBullet("DNS-запросы и посещённые сайты не записываются и не кэшируются.")
                ZeroLogBullet("Ключи шифрования и конфигурации хранятся локально в защищенном анклаве.")
                ZeroLogBullet("Журнал соединений фиксирует только сессионный тайминг без привязки к трафику.")
                ZeroLogBullet("Отсутствуют сторонние трекеры, рекламные фреймворки и телеметрические SDK.")
            }
        }

        // Permissions Breakdown
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "СИСТЕМНЫЕ ПРИВИЛЕГИИ ANDROID",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = TextLuxuryTertiary,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )

            PermissionItem(
                icon = Icons.Default.VpnKey,
                name = "BIND_VPN_SERVICE (Системный VPN)",
                description = "Формирование интерфейса tun0 ядра Linux для перенаправления сетевых пакетов в туннель."
            )

            PermissionItem(
                icon = Icons.Default.Lock,
                name = "INTERNET (Шифрованный сокет)",
                description = "Транспортировка зашифрованного потока данных через сокеты протокола к удаленному узлу."
            )

            PermissionItem(
                icon = Icons.Default.SignalCellularAlt,
                name = "ACCESS_NETWORK_STATE (Монитор сетей)",
                description = "Мгновенное обнаружение смены маршрутов между Wi-Fi и LTE для автовосстановления сессии."
            )

            PermissionItem(
                icon = Icons.Default.Wifi,
                name = "FOREGROUND_SERVICE (Служба туннеля)",
                description = "Предотвращение принудительной остановки туннеля системой энергосбережения Doze."
            )

            PermissionItem(
                icon = Icons.Default.Notifications,
                name = "POST_NOTIFICATIONS (Индикатор шторки)",
                description = "Отображение защищенного статуса и оперативное отключение в один клик из центра уведомлений."
            )

            PermissionItem(
                icon = Icons.Default.PowerSettingsNew,
                name = "RECEIVE_BOOT_COMPLETED (Автостарт)",
                description = "Автоматический запуск туннелирования при загрузке системы, если задано пользователем."
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ZeroLogBullet(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = EmeraldLuxury,
            modifier = Modifier
                .size(15.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
            color = TextLuxuryPrimary
        )
    }
}

@Composable
private fun PermissionItem(icon: ImageVector, name: String, description: String) {
    LuxuryCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = TitaniumBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SapphireSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SapphireAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = TextLuxuryPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    ),
                    color = TextLuxuryTertiary
                )
            }
        }
    }
}
