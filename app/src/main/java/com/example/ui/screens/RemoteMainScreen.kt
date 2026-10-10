package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionStatus
import com.example.model.RemoteControlMode
import com.example.model.SamsungKey
import com.example.model.SamsungTVDevice
import com.example.model.StreamingApp
import com.example.model.TvChannel
import com.example.model.TvState
import com.example.ui.components.ChannelNumpadSheet
import com.example.ui.components.FavoriteChannelsQuickSurf
import com.example.ui.components.JogWheelVolumeChannelDial
import com.example.ui.components.RemoteDpad
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.StreamingAppsRow
import com.example.ui.components.TactileChannelRocker
import com.example.ui.components.TactileVolumeRocker
import com.example.ui.components.TvConnectPromptDialog
import com.example.ui.components.TvDiscoveryDialog
import com.example.ui.components.VirtualSamsungTvDisplay
import com.example.ui.components.VoiceControlDialog
import com.example.ui.theme.RemoteBorder
import com.example.ui.theme.RemoteDarkBg
import com.example.ui.theme.RemoteSurface
import com.example.ui.theme.RemoteSurfaceElevated
import com.example.ui.theme.RemoteSurfacePill
import com.example.ui.theme.SamsungCyan
import com.example.ui.theme.SamsungGreen
import com.example.ui.theme.SamsungPowerRed
import com.example.ui.theme.SamsungPowerRedGlow
import com.example.ui.theme.SamsungYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteMainScreen(
    state: TvState,
    favoriteChannels: List<TvChannel>,
    savedDevices: List<SamsungTVDevice>,
    discoveredDevices: List<SamsungTVDevice>,
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit,
    onToggleMute: () -> Unit,
    onChannelUp: () -> Unit,
    onChannelDown: () -> Unit,
    onPreviousChannel: () -> Unit,
    onSelectChannel: (TvChannel) -> Unit,
    onKeypadDigit: (Int) -> Unit,
    onTuneDirectChannel: (Int) -> Unit,
    onTogglePower: () -> Unit,
    onSendKey: (SamsungKey) -> Unit,
    onSetControlMode: (RemoteControlMode) -> Unit,
    onSelectDevice: (SamsungTVDevice) -> Unit,
    onAddManualDevice: (name: String, ip: String, mac: String) -> Unit,
    onRefreshScan: () -> Unit,
    onStartVoiceListening: () -> Unit,
    onStopVoiceListening: () -> Unit,
    onExecuteVoiceCommand: (String) -> Unit,
    onStartSleepTimer: (Int) -> Unit,
    onCancelSleepTimer: () -> Unit,
    onSurfNextFavorite: () -> Unit,
    onSurfPrevFavorite: () -> Unit,
    onToggleFavoriteSurfingOnly: () -> Unit,
    onAcceptPromptConnect: (SamsungTVDevice) -> Unit,
    onDismissPrompt: () -> Unit,
    onLaunchStreamingApp: (StreamingApp) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDiscoveryDialog by remember { mutableStateOf(false) }
    var showNumpadSheet by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RemoteDarkBg)
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Top Bar: Device Selector, Sleep Timer, Voice, Numpad & Power ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Active TV Selector Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(RemoteSurfaceElevated)
                    .border(1.dp, RemoteBorder, RoundedCornerShape(20.dp))
                    .clickable { showDiscoveryDialog = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("tv_selector_pill"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
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
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = (state.connectedDevice?.name ?: "Samsung TV").take(14),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = "Change TV",
                    tint = SamsungCyan,
                    modifier = Modifier.size(13.dp)
                )
            }

            // Right Action Controls: Sleep Timer, Mic, 123, Power
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sleep Timer Button
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (state.sleepTimerRemainingSeconds != null) SamsungGreen.copy(alpha = 0.2f)
                            else RemoteSurfaceElevated
                        )
                        .border(
                            1.dp,
                            if (state.sleepTimerRemainingSeconds != null) SamsungGreen else RemoteBorder,
                            RoundedCornerShape(18.dp)
                        )
                        .clickable { showSleepTimerDialog = true }
                        .padding(horizontal = 8.dp)
                        .testTag("open_sleep_timer_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Sleep Timer",
                            tint = if (state.sleepTimerRemainingSeconds != null) SamsungGreen else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        if (state.sleepTimerRemainingSeconds != null) {
                            val mins = state.sleepTimerRemainingSeconds / 60
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${mins}m",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SamsungGreen
                            )
                        }
                    }
                }

                // Voice Control Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (state.isVoiceListening) SamsungCyan.copy(alpha = 0.3f) else RemoteSurfaceElevated)
                        .border(1.dp, if (state.isVoiceListening) SamsungCyan else RemoteBorder, CircleShape)
                        .clickable { showVoiceDialog = true }
                        .testTag("open_voice_control_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Control",
                        tint = if (state.isVoiceListening) SamsungCyan else SamsungCyan.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // 123 Numpad launcher button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(RemoteSurfaceElevated)
                        .border(1.dp, RemoteBorder, CircleShape)
                        .clickable { showNumpadSheet = true }
                        .testTag("open_numpad_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "123",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SamsungCyan
                    )
                }

                // Power Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(8.dp, CircleShape, spotColor = SamsungPowerRedGlow)
                        .clip(CircleShape)
                        .background(if (state.isPoweredOn) RemoteSurfaceElevated else SamsungPowerRed.copy(alpha = 0.2f))
                        .border(
                            1.5.dp,
                            if (state.isPoweredOn) SamsungPowerRed else RemoteBorder,
                            CircleShape
                        )
                        .clickable(onClick = onTogglePower)
                        .testTag("remote_power_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Power",
                        tint = SamsungPowerRed,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // --- Wi-Fi Network Scanning Pill Indicator ---
        if (state.isNetworkScanning) {
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(RemoteSurfaceElevated)
                    .border(1.dp, SamsungCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    color = SamsungCyan,
                    strokeWidth = 1.5.dp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Scanning local Wi-Fi for Samsung TVs...",
                    fontSize = 11.sp,
                    color = SamsungCyan
                )
            }
        }

        // --- Virtual TV Display Preview ---
        if (state.showVirtualTvScreen) {
            VirtualSamsungTvDisplay(
                state = state,
                onTogglePower = onTogglePower,
                onAppSelected = { appName ->
                    val app = when {
                        appName.contains("Netflix", ignoreCase = true) -> StreamingApp.NETFLIX
                        appName.contains("YouTube", ignoreCase = true) -> StreamingApp.YOUTUBE
                        appName.contains("Disney", ignoreCase = true) -> StreamingApp.DISNEY_PLUS
                        appName.contains("Prime", ignoreCase = true) -> StreamingApp.PRIME_VIDEO
                        appName.contains("TV+", ignoreCase = true) || appName.contains("Samsung", ignoreCase = true) -> StreamingApp.SAMSUNG_TV_PLUS
                        else -> null
                    }
                    if (app != null) {
                        onLaunchStreamingApp(app)
                    } else {
                        val matching = favoriteChannels.find { it.name.contains(appName, ignoreCase = true) }
                        if (matching != null) {
                            onSelectChannel(matching)
                        } else {
                            onSendKey(SamsungKey.HOME)
                        }
                    }
                }
            )
        }

        // --- Dedicated Favorite Channels Quick Surf Bar ---
        FavoriteChannelsQuickSurf(
            favoriteChannels = favoriteChannels,
            currentChannelNumber = state.currentChannelNumber,
            isFavoriteSurfingOnly = state.isFavoriteSurfingOnly,
            onSelectChannel = onSelectChannel,
            onSurfNext = onSurfNextFavorite,
            onSurfPrev = onSurfPrevFavorite,
            onToggleFavoriteSurfingOnly = onToggleFavoriteSurfingOnly,
            onOpenAllChannels = {}
        )

        Spacer(modifier = Modifier.height(4.dp))

        // --- Mode Switcher (Tactile Rockers vs Jog-Wheel Scrub Dial) ---
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(RemoteSurfaceElevated)
                .border(1.dp, RemoteBorder, RoundedCornerShape(20.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (state.controlMode == RemoteControlMode.TACTILE_ROCKER) SamsungCyan else Color.Transparent
                    )
                    .clickable { onSetControlMode(RemoteControlMode.TACTILE_ROCKER) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("mode_rocker_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ViewAgenda,
                        contentDescription = null,
                        tint = if (state.controlMode == RemoteControlMode.TACTILE_ROCKER) RemoteDarkBg else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tactile Rockers",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (state.controlMode == RemoteControlMode.TACTILE_ROCKER) RemoteDarkBg else TextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (state.controlMode == RemoteControlMode.JOG_WHEEL) SamsungCyan else Color.Transparent
                    )
                    .clickable { onSetControlMode(RemoteControlMode.JOG_WHEEL) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("mode_jog_wheel_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Dialpad,
                        contentDescription = null,
                        tint = if (state.controlMode == RemoteControlMode.JOG_WHEEL) RemoteDarkBg else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Rotary Dial",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (state.controlMode == RemoteControlMode.JOG_WHEEL) RemoteDarkBg else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- PRIMARY CONTROL STATION: Volume & Channel Adjustments ---
        if (state.controlMode == RemoteControlMode.TACTILE_ROCKER) {
            // Dual Tactile Rocker Switches (Left = Volume, Right = Channel)
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT: Tactile Volume Rocker
                TactileVolumeRocker(
                    volume = state.volume,
                    isMuted = state.isMuted,
                    onVolumeUp = onVolumeUp,
                    onVolumeDown = onVolumeDown,
                    onToggleMute = onToggleMute
                )

                // CENTER: Smart Hub, Voice Mic & Media Cluster
                Column(
                    modifier = Modifier.width(100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Smart Hub (Home)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(RemoteSurfaceElevated)
                            .border(1.dp, RemoteBorder, CircleShape)
                            .clickable { onSendKey(SamsungKey.HOME) }
                            .testTag("center_home_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Smart Hub",
                            tint = SamsungCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Samsung OneRemote Center Voice Mic Button
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(8.dp, CircleShape, spotColor = SamsungCyan)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF0F264A), Color(0xFF1B3B6F))
                                )
                            )
                            .border(1.5.dp, SamsungCyan, CircleShape)
                            .clickable { showVoiceDialog = true }
                            .testTag("center_voice_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Control",
                                tint = SamsungCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Play / Pause Toggle
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(RemoteSurfaceElevated)
                            .border(1.dp, RemoteBorder, CircleShape)
                            .clickable { onSendKey(SamsungKey.PLAY) }
                            .testTag("center_play_pause_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = SamsungCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // RIGHT: Tactile Channel Rocker
                TactileChannelRocker(
                    channelNumber = state.currentChannelNumber,
                    channelName = state.currentChannelName,
                    onChannelUp = onChannelUp,
                    onChannelDown = onChannelDown,
                    onPreviousChannel = onPreviousChannel,
                    onOpenNumpad = { showNumpadSheet = true }
                )
            }
        } else {
            // Mode: Rotary Jog-Wheel Mode
            JogWheelVolumeChannelDial(
                volume = state.volume,
                isMuted = state.isMuted,
                channelNumber = state.currentChannelNumber,
                channelName = state.currentChannelName,
                onVolumeUp = onVolumeUp,
                onVolumeDown = onVolumeDown,
                onChannelUp = onChannelUp,
                onChannelDown = onChannelDown,
                onToggleMute = onToggleMute
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- SECONDARY: Circular D-Pad & Navigation ---
        RemoteDpad(
            onSendKey = onSendKey
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- DEDICATED STREAMING APPS LAUNCHER ROW ---
        StreamingAppsRow(
            activeAppId = state.activeStreamingAppId,
            onLaunchApp = onLaunchStreamingApp
        )
    }

    // Direct Channel Numpad Bottom Sheet
    if (showNumpadSheet) {
        ChannelNumpadSheet(
            sheetState = sheetState,
            favoriteChannels = favoriteChannels,
            onTuneToChannel = onTuneDirectChannel,
            onDigitPress = onKeypadDigit,
            onDismiss = { showNumpadSheet = false }
        )
    }

    // TV Connection & Discovery Dialog
    if (showDiscoveryDialog) {
        TvDiscoveryDialog(
            currentDevice = state.connectedDevice,
            connectionStatus = state.connectionStatus,
            savedDevices = savedDevices,
            discoveredDevices = discoveredDevices,
            onSelectDevice = onSelectDevice,
            onAddManualDevice = onAddManualDevice,
            onRefreshScan = onRefreshScan,
            onDismiss = { showDiscoveryDialog = false }
        )
    }

    // Voice Control Modal Dialog
    if (showVoiceDialog) {
        VoiceControlDialog(
            isListening = state.isVoiceListening,
            recognizedText = state.voiceRecognizedText,
            feedbackMessage = state.voiceFeedbackMessage,
            voiceRmsDb = state.voiceRmsDb,
            onStartListening = onStartVoiceListening,
            onStopListening = onStopVoiceListening,
            onExecuteCommand = onExecuteVoiceCommand,
            onDismiss = {
                onStopVoiceListening()
                showVoiceDialog = false
            }
        )
    }

    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            remainingSeconds = state.sleepTimerRemainingSeconds,
            initialSeconds = state.sleepTimerInitialSeconds,
            onStartTimer = onStartSleepTimer,
            onCancelTimer = onCancelSleepTimer,
            onDismiss = { showSleepTimerDialog = false }
        )
    }

    // Samsung TV Connect Prompt Dialog
    state.promptTvToConnect?.let { discoveredTv ->
        TvConnectPromptDialog(
            device = discoveredTv,
            onConnect = onAcceptPromptConnect,
            onDismiss = onDismissPrompt
        )
    }
}
