package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionStatus
import com.example.model.SamsungTVDevice
import com.example.model.TvState
import com.example.ui.theme.RemoteBorder
import com.example.ui.theme.RemoteDarkBg
import com.example.ui.theme.RemoteSurface
import com.example.ui.theme.RemoteSurfaceElevated
import com.example.ui.theme.SamsungCyan
import com.example.ui.theme.SamsungGreen
import com.example.ui.theme.SamsungPowerRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TvDevicesScreen(
    state: TvState,
    savedDevices: List<SamsungTVDevice>,
    discoveredDevices: List<SamsungTVDevice>,
    onConnectDevice: (SamsungTVDevice) -> Unit,
    onDeleteDevice: (SamsungTVDevice) -> Unit,
    onOpenDiscovery: () -> Unit,
    onTriggerNetworkScan: () -> Unit,
    onSimulateTvFound: () -> Unit,
    onToggleHaptic: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleVirtualTv: () -> Unit,
    onSendWakeOnLan: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(RemoteDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active TV Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = RemoteSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (state.connectionStatus == ConnectionStatus.CONNECTED) SamsungGreen else SamsungCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_tv_status_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (state.connectionStatus) {
                                            ConnectionStatus.CONNECTED -> SamsungGreen
                                            ConnectionStatus.VIRTUAL_SIMULATOR -> SamsungCyan
                                            ConnectionStatus.CONNECTING, ConnectionStatus.DISCOVERING -> Color(0xFFFBBF24)
                                            else -> SamsungPowerRed
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (state.connectionStatus) {
                                    ConnectionStatus.CONNECTED -> "CONNECTED"
                                    ConnectionStatus.VIRTUAL_SIMULATOR -> "VIRTUAL TV SIMULATOR"
                                    ConnectionStatus.CONNECTING -> "CONNECTING..."
                                    ConnectionStatus.DISCOVERING -> "DISCOVERING..."
                                    ConnectionStatus.PAIRING_REQUIRED -> "PAIRING REQUIRED"
                                    else -> "DISCONNECTED"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (state.connectionStatus) {
                                    ConnectionStatus.CONNECTED -> SamsungGreen
                                    ConnectionStatus.VIRTUAL_SIMULATOR -> SamsungCyan
                                    else -> TextSecondary
                                },
                                letterSpacing = 1.sp
                            )
                        }

                        IconButton(onClick = onOpenDiscovery) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = "Scan",
                                tint = SamsungCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = state.connectedDevice?.name ?: "Samsung Smart TV",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "IP: ${state.connectedDevice?.ipAddress ?: "192.168.1.105"} • Port: ${state.connectedDevice?.port ?: 8001}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    if (!state.connectedDevice?.macAddress.isNullOrBlank()) {
                        Text(
                            text = "MAC: ${state.connectedDevice?.macAddress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onSendWakeOnLan,
                            colors = ButtonDefaults.buttonColors(containerColor = RemoteSurfaceElevated),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = SamsungCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Wake-on-LAN", fontSize = 12.sp, color = TextPrimary)
                        }

                        OutlinedButton(
                            onClick = onOpenDiscovery,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SamsungCyan),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = SamsungCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change TV", fontSize = 12.sp, color = SamsungCyan)
                        }
                    }
                }
            }
        }

        // Automatic Network Scanning Service Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = RemoteSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RemoteBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("network_scanner_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = SamsungCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Wi-Fi TV Discovery Service",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SamsungGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (state.isNetworkScanning) "SCANNING" else "AUTO-SCAN ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.isNetworkScanning) SamsungCyan else SamsungGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scans local subnet and SSDP multicast for Samsung Smart TVs and prompts to connect whenever a TV is discovered.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onTriggerNetworkScan,
                            enabled = !state.isNetworkScanning,
                            colors = ButtonDefaults.buttonColors(containerColor = SamsungCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trigger_net_scan_btn")
                        ) {
                            if (state.isNetworkScanning) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = RemoteDarkBg,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scanning...", fontSize = 11.sp, color = RemoteDarkBg)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = RemoteDarkBg,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan Wi-Fi Now", fontSize = 11.sp, color = RemoteDarkBg, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = onSimulateTvFound,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SamsungCyan),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("simulate_tv_found_btn")
                        ) {
                            Text("Simulate Discovery", fontSize = 11.sp, color = SamsungCyan)
                        }
                    }
                }
            }
        }

        // Saved Devices List
        item {
            Text(
                text = "Saved Samsung TVs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        items(savedDevices) { dev ->
            val isCurrent = state.connectedDevice?.id == dev.id
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(RemoteSurface)
                    .border(
                        1.dp,
                        if (isCurrent) SamsungCyan else RemoteBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onConnectDevice(dev) }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = if (isCurrent) SamsungCyan else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = dev.name,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${dev.ipAddress} • ${if (dev.isVirtual) "Simulator" else dev.modelName}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isCurrent) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active",
                                tint = SamsungGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            IconButton(onClick = { onDeleteDevice(dev) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Preferences Section
        item {
            Text(
                text = "Remote Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = RemoteSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Haptic Feedback Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = null,
                                tint = SamsungCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Haptic Vibration", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Vibrate on rocker & button press", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = state.isHapticEnabled,
                            onCheckedChange = onToggleHaptic,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SamsungCyan,
                                checkedTrackColor = SamsungCyan.copy(alpha = 0.4f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Virtual TV Screen Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = SamsungCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Virtual TV Preview", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Show on-screen channel & volume HUD", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = state.showVirtualTvScreen,
                            onCheckedChange = { onToggleVirtualTv() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SamsungCyan,
                                checkedTrackColor = SamsungCyan.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }

        // Live Command Transmission Log Console
        item {
            Text(
                text = "Command Transmission Log",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF060910))
                    .border(1.dp, RemoteBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "LAST COMMANDS SENT VIA WEBSOCKET:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SamsungCyan,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    state.logMessages.take(6).forEach { log ->
                        Text(
                            text = "› $log",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}
