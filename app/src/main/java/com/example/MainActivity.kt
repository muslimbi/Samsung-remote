package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.SamsungKey
import com.example.model.SamsungTVDevice
import com.example.model.TvChannel
import com.example.ui.screens.ChannelsScreen
import com.example.ui.screens.RemoteMainScreen
import com.example.ui.screens.TvDevicesScreen
import com.example.ui.theme.RemoteBorder
import com.example.ui.theme.RemoteDarkBg
import com.example.ui.theme.RemoteSurface
import com.example.ui.theme.SamRemoteTheme
import com.example.ui.theme.SamsungCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.RemoteViewModel

enum class AppScreen {
    REMOTE,
    CHANNELS,
    DEVICES
}

class MainActivity : ComponentActivity() {

    private val viewModel: RemoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SamRemoteTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val allChannels by viewModel.allChannels.collectAsStateWithLifecycle()
                val favoriteChannels by viewModel.favoriteChannels.collectAsStateWithLifecycle()
                val savedDevices by viewModel.savedDevices.collectAsStateWithLifecycle()
                val discoveredDevices by viewModel.discoveredDevices.collectAsStateWithLifecycle()

                var currentScreen by remember { mutableStateOf(AppScreen.REMOTE) }

                Scaffold(
                    containerColor = RemoteDarkBg,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        NavigationBar(
                            containerColor = RemoteSurface,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.REMOTE,
                                onClick = { currentScreen = AppScreen.REMOTE },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Tv,
                                        contentDescription = "Remote",
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Remote",
                                        fontSize = 11.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = RemoteDarkBg,
                                    selectedTextColor = SamsungCyan,
                                    indicatorColor = SamsungCyan,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_remote")
                            )

                            NavigationBarItem(
                                selected = currentScreen == AppScreen.CHANNELS,
                                onClick = { currentScreen = AppScreen.CHANNELS },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.LiveTv,
                                        contentDescription = "Channels",
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Channels",
                                        fontSize = 11.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = RemoteDarkBg,
                                    selectedTextColor = SamsungCyan,
                                    indicatorColor = SamsungCyan,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_channels")
                            )

                            NavigationBarItem(
                                selected = currentScreen == AppScreen.DEVICES,
                                onClick = { currentScreen = AppScreen.DEVICES },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Devices,
                                        contentDescription = "Devices",
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Devices",
                                        fontSize = 11.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = RemoteDarkBg,
                                    selectedTextColor = SamsungCyan,
                                    indicatorColor = SamsungCyan,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_devices")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            AppScreen.REMOTE -> {
                                RemoteMainScreen(
                                    state = state,
                                    favoriteChannels = favoriteChannels,
                                    savedDevices = savedDevices,
                                    discoveredDevices = discoveredDevices,
                                    onVolumeUp = viewModel::onVolumeUp,
                                    onVolumeDown = viewModel::onVolumeDown,
                                    onToggleMute = viewModel::onToggleMute,
                                    onChannelUp = viewModel::onChannelUp,
                                    onChannelDown = viewModel::onChannelDown,
                                    onPreviousChannel = viewModel::onPreviousChannel,
                                    onSelectChannel = viewModel::onSelectChannel,
                                    onKeypadDigit = viewModel::onKeypadDigit,
                                    onTuneDirectChannel = { num ->
                                        val existing = allChannels.find { it.number == num }
                                        if (existing != null) {
                                            viewModel.onSelectChannel(existing)
                                        } else {
                                            viewModel.onSelectChannel(TvChannel(number = num, name = "Channel $num"))
                                        }
                                    },
                                    onTogglePower = viewModel::onTogglePower,
                                    onSendKey = viewModel::onSendKey,
                                    onSetControlMode = viewModel::setControlMode,
                                    onSelectDevice = viewModel::connectToDevice,
                                    onAddManualDevice = viewModel::addManualDevice,
                                    onRefreshScan = viewModel::startDiscovery,
                                    onStartVoiceListening = viewModel::startVoiceListening,
                                    onStopVoiceListening = viewModel::stopVoiceListening,
                                    onExecuteVoiceCommand = viewModel::executeVoiceCommand,
                                    onStartSleepTimer = viewModel::startSleepTimer,
                                    onCancelSleepTimer = viewModel::cancelSleepTimer,
                                    onSurfNextFavorite = viewModel::surfNextFavorite,
                                    onSurfPrevFavorite = viewModel::surfPrevFavorite,
                                    onToggleFavoriteSurfingOnly = viewModel::toggleFavoriteSurfingOnly,
                                    onAcceptPromptConnect = viewModel::acceptPromptAndConnect,
                                    onDismissPrompt = viewModel::dismissPrompt,
                                    onLaunchStreamingApp = viewModel::launchStreamingApp
                                )
                            }
                            AppScreen.CHANNELS -> {
                                BackHandler {
                                    currentScreen = AppScreen.REMOTE
                                }
                                ChannelsScreen(
                                    channels = allChannels,
                                    currentChannelNumber = state.currentChannelNumber,
                                    onTuneChannel = { chan ->
                                        viewModel.onSelectChannel(chan)
                                        currentScreen = AppScreen.REMOTE
                                    },
                                    onToggleFavorite = viewModel::toggleFavoriteChannel,
                                    onAddChannel = viewModel::addCustomChannel
                                )
                            }
                            AppScreen.DEVICES -> {
                                BackHandler {
                                    currentScreen = AppScreen.REMOTE
                                }
                                TvDevicesScreen(
                                    state = state,
                                    savedDevices = savedDevices,
                                    discoveredDevices = discoveredDevices,
                                    onConnectDevice = viewModel::connectToDevice,
                                    onDeleteDevice = viewModel::deleteDevice,
                                    onOpenDiscovery = viewModel::startDiscovery,
                                    onTriggerNetworkScan = { viewModel.startNetworkScan(showPromptIfFound = true) },
                                    onSimulateTvFound = viewModel::simulateDiscoveredTv,
                                    onToggleHaptic = viewModel::toggleHaptic,
                                    onToggleSound = viewModel::toggleSound,
                                    onToggleVirtualTv = viewModel::toggleVirtualTvScreen,
                                    onSendWakeOnLan = {
                                        val dev = state.connectedDevice
                                        if (dev != null) {
                                            viewModel.onTogglePower()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
