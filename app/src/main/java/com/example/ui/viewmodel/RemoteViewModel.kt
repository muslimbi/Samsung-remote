package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TvRepository
import com.example.data.db.AppDatabase
import com.example.model.ConnectionStatus
import com.example.model.RemoteControlMode
import com.example.model.SamsungKey
import com.example.model.SamsungTVDevice
import com.example.model.StreamingApp
import com.example.model.TvChannel
import com.example.model.TvState
import com.example.network.SamsungNetworkScannerService
import com.example.network.SamsungTvClient
import com.example.network.SamsungTvDiscovery
import com.example.network.TvConnectionEvent
import com.example.voice.VoiceAction
import com.example.voice.VoiceRemoteController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RemoteViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = TvRepository(db)
    private val client = SamsungTvClient()
    private val discovery = SamsungTvDiscovery(application)
    private val networkScanner = SamsungNetworkScannerService(application, viewModelScope)
    private val voiceController = VoiceRemoteController(application)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _uiState = MutableStateFlow(TvState())
    val uiState: StateFlow<TvState> = _uiState.asStateFlow()

    val allChannels: StateFlow<List<TvChannel>> = repository.allChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteChannels: StateFlow<List<TvChannel>> = repository.favoriteChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedDevices: StateFlow<List<SamsungTVDevice>> = repository.allDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _discoveredDevices = MutableStateFlow<List<SamsungTVDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<SamsungTVDevice>> = _discoveredDevices.asStateFlow()

    private var volumeOverlayJob: Job? = null
    private var discoveryJob: Job? = null
    private var sleepTimerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureDefaultData()
            val lastDevice = repository.getLastConnectedDevice()
            if (lastDevice != null) {
                connectToDevice(lastDevice)
            } else {
                val virtualTv = SamsungTVDevice(
                    name = "Samsung QLED 4K (Living Room)",
                    ipAddress = "192.168.1.105",
                    macAddress = "E4:7C:F9:11:22:33",
                    modelName = "QN90B Neo QLED",
                    isLastConnected = true,
                    isVirtual = true
                )
                repository.saveDevice(virtualTv)
                connectToDevice(virtualTv)
            }

            // Automatically discover available Samsung TVs on the local Wi-Fi and prompt to connect
            startNetworkScan(showPromptIfFound = true)
            networkScanner.startPeriodicAutoScan(intervalMs = 45_000) { tv: SamsungTVDevice ->
                onTvDiscovered(tv, showPrompt = true)
            }
        }

        // Listen for connection events
        viewModelScope.launch {
            client.events.collect { event ->
                when (event) {
                    is TvConnectionEvent.Connected -> {
                        _uiState.update {
                            it.copy(
                                connectionStatus = if (it.connectedDevice?.isVirtual == true)
                                    ConnectionStatus.VIRTUAL_SIMULATOR else ConnectionStatus.CONNECTED,
                                logMessages = (listOf("Connected to ${it.connectedDevice?.name}") + it.logMessages).take(20)
                            )
                        }
                        if (event.token != null && _uiState.value.connectedDevice != null) {
                            repository.updateDeviceToken(_uiState.value.connectedDevice!!.id, event.token)
                        }
                    }
                    is TvConnectionEvent.Connecting -> {
                        _uiState.update { it.copy(connectionStatus = ConnectionStatus.CONNECTING) }
                    }
                    is TvConnectionEvent.Disconnected -> {
                        _uiState.update {
                            it.copy(
                                connectionStatus = ConnectionStatus.DISCONNECTED,
                                logMessages = (listOf("Disconnected: ${event.reason}") + it.logMessages).take(20)
                            )
                        }
                    }
                    is TvConnectionEvent.PairingRequired -> {
                        _uiState.update {
                            it.copy(
                                connectionStatus = ConnectionStatus.PAIRING_REQUIRED,
                                logMessages = (listOf("Action required: Accept pairing on TV") + it.logMessages).take(20)
                            )
                        }
                    }
                    is TvConnectionEvent.Error -> {
                        _uiState.update {
                            it.copy(
                                connectionStatus = ConnectionStatus.ERROR,
                                logMessages = (listOf("Connection error: ${event.message}") + it.logMessages).take(20)
                            )
                        }
                    }
                    is TvConnectionEvent.KeySent -> {
                        // Key successfully acknowledged
                    }
                }
            }
        }
    }

    private fun triggerHaptic(durationMs: Long = 35) {
        if (!_uiState.value.isHapticEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun showVolumeOverlayBriefly() {
        volumeOverlayJob?.cancel()
        _uiState.update { it.copy(volumeOverlayVisible = true) }
        volumeOverlayJob = viewModelScope.launch {
            delay(2200)
            _uiState.update { it.copy(volumeOverlayVisible = false) }
        }
    }

    // --- Volume Controls ---
    fun onVolumeUp() {
        triggerHaptic(30)
        _uiState.update { current ->
            val newVol = (current.volume + 1).coerceAtMost(100)
            current.copy(
                volume = newVol,
                isMuted = false,
                lastActionTimestamp = System.currentTimeMillis(),
                logMessages = (listOf("Volume Up: $newVol") + current.logMessages).take(20)
            )
        }
        showVolumeOverlayBriefly()
        viewModelScope.launch {
            client.sendKey(SamsungKey.VOL_UP)
        }
    }

    fun onVolumeDown() {
        triggerHaptic(30)
        _uiState.update { current ->
            val newVol = (current.volume - 1).coerceAtLeast(0)
            current.copy(
                volume = newVol,
                isMuted = false,
                lastActionTimestamp = System.currentTimeMillis(),
                logMessages = (listOf("Volume Down: $newVol") + current.logMessages).take(20)
            )
        }
        showVolumeOverlayBriefly()
        viewModelScope.launch {
            client.sendKey(SamsungKey.VOL_DOWN)
        }
    }

    fun onVolumeSet(level: Int) {
        triggerHaptic(15)
        val clamped = level.coerceIn(0, 100)
        val prev = _uiState.value.volume
        _uiState.update {
            it.copy(
                volume = clamped,
                isMuted = false,
                lastActionTimestamp = System.currentTimeMillis()
            )
        }
        showVolumeOverlayBriefly()
        viewModelScope.launch {
            if (clamped > prev) {
                client.sendKey(SamsungKey.VOL_UP)
            } else if (clamped < prev) {
                client.sendKey(SamsungKey.VOL_DOWN)
            }
        }
    }

    fun onToggleMute() {
        triggerHaptic(50)
        _uiState.update { current ->
            val muted = !current.isMuted
            current.copy(
                isMuted = muted,
                lastActionTimestamp = System.currentTimeMillis(),
                logMessages = (listOf(if (muted) "Muted" else "Unmuted") + current.logMessages).take(20)
            )
        }
        showVolumeOverlayBriefly()
        viewModelScope.launch {
            client.sendKey(SamsungKey.MUTE)
        }
    }

    // --- Channel Controls ---
    fun onChannelUp() {
        triggerHaptic(40)
        viewModelScope.launch {
            val next = repository.getNextChannel(_uiState.value.currentChannelNumber)
            _uiState.update { current ->
                val prevNum = current.currentChannelNumber
                val newNum = next?.number ?: (current.currentChannelNumber + 1)
                val newName = next?.name ?: "Channel $newNum"
                val newShow = next?.currentShow ?: "Live TV Broadcast"
                current.copy(
                    previousChannelNumber = prevNum,
                    currentChannelNumber = newNum,
                    currentChannelName = newName,
                    currentShow = newShow,
                    lastActionTimestamp = System.currentTimeMillis(),
                    logMessages = (listOf("Channel Up: CH $newNum ($newName)") + current.logMessages).take(20)
                )
            }
            client.sendKey(SamsungKey.CH_UP)
        }
    }

    fun onChannelDown() {
        triggerHaptic(40)
        viewModelScope.launch {
            val prev = repository.getPreviousChannel(_uiState.value.currentChannelNumber)
            _uiState.update { current ->
                val prevNum = current.currentChannelNumber
                val newNum = prev?.number ?: (current.currentChannelNumber - 1).coerceAtLeast(1)
                val newName = prev?.name ?: "Channel $newNum"
                val newShow = prev?.currentShow ?: "Live TV Broadcast"
                current.copy(
                    previousChannelNumber = prevNum,
                    currentChannelNumber = newNum,
                    currentChannelName = newName,
                    currentShow = newShow,
                    lastActionTimestamp = System.currentTimeMillis(),
                    logMessages = (listOf("Channel Down: CH $newNum ($newName)") + current.logMessages).take(20)
                )
            }
            client.sendKey(SamsungKey.CH_DOWN)
        }
    }

    fun onPreviousChannel() {
        triggerHaptic(35)
        viewModelScope.launch {
            val prevNum = _uiState.value.previousChannelNumber
            val target = repository.getChannelByNumber(prevNum)
            _uiState.update { current ->
                val newPrev = current.currentChannelNumber
                current.copy(
                    previousChannelNumber = newPrev,
                    currentChannelNumber = prevNum,
                    currentChannelName = target?.name ?: "Channel $prevNum",
                    currentShow = target?.currentShow ?: "Live TV",
                    lastActionTimestamp = System.currentTimeMillis(),
                    logMessages = (listOf("Prev Channel: CH $prevNum") + current.logMessages).take(20)
                )
            }
            client.sendKey(SamsungKey.PRE_CH)
        }
    }

    fun onSelectChannel(channel: TvChannel) {
        triggerHaptic(45)
        _uiState.update { current ->
            current.copy(
                previousChannelNumber = current.currentChannelNumber,
                currentChannelNumber = channel.number,
                currentChannelName = channel.name,
                currentShow = channel.currentShow,
                lastActionTimestamp = System.currentTimeMillis(),
                logMessages = (listOf("Tuned to CH ${channel.number} (${channel.name})") + current.logMessages).take(20)
            )
        }
        viewModelScope.launch {
            // Send digits for channel
            val digits = channel.number.toString()
            for (char in digits) {
                val key = when (char) {
                    '0' -> SamsungKey.NUM_0
                    '1' -> SamsungKey.NUM_1
                    '2' -> SamsungKey.NUM_2
                    '3' -> SamsungKey.NUM_3
                    '4' -> SamsungKey.NUM_4
                    '5' -> SamsungKey.NUM_5
                    '6' -> SamsungKey.NUM_6
                    '7' -> SamsungKey.NUM_7
                    '8' -> SamsungKey.NUM_8
                    '9' -> SamsungKey.NUM_9
                    else -> null
                }
                if (key != null) {
                    client.sendKey(key)
                    delay(120)
                }
            }
            client.sendKey(SamsungKey.ENTER)
        }
    }

    fun onKeypadDigit(digit: Int) {
        triggerHaptic(25)
        val key = when (digit) {
            0 -> SamsungKey.NUM_0
            1 -> SamsungKey.NUM_1
            2 -> SamsungKey.NUM_2
            3 -> SamsungKey.NUM_3
            4 -> SamsungKey.NUM_4
            5 -> SamsungKey.NUM_5
            6 -> SamsungKey.NUM_6
            7 -> SamsungKey.NUM_7
            8 -> SamsungKey.NUM_8
            9 -> SamsungKey.NUM_9
            else -> SamsungKey.NUM_0
        }
        viewModelScope.launch {
            client.sendKey(key)
        }
        _uiState.update {
            it.copy(logMessages = (listOf("Sent digit $digit") + it.logMessages).take(20))
        }
    }

    // --- Power & Sources ---
    fun onTogglePower() {
        triggerHaptic(60)
        val nextPower = !_uiState.value.isPoweredOn
        _uiState.update {
            it.copy(
                isPoweredOn = nextPower,
                lastActionTimestamp = System.currentTimeMillis(),
                logMessages = (listOf(if (nextPower) "Power ON command sent" else "Power OFF command sent") + it.logMessages).take(20)
            )
        }
        viewModelScope.launch {
            val dev = _uiState.value.connectedDevice
            if (nextPower && dev != null && dev.macAddress.isNotBlank()) {
                client.sendWakeOnLan(dev.macAddress)
            }
            client.sendKey(SamsungKey.POWER)
        }
    }

    fun onCycleSource() {
        triggerHaptic(35)
        val sources = listOf("TV Antenna", "HDMI 1 (PS5)", "HDMI 2 (Apple TV)", "Smart Hub", "Netflix", "YouTube")
        val currentIndex = sources.indexOf(_uiState.value.currentInputSource)
        val nextIndex = (currentIndex + 1) % sources.size
        val nextSource = sources[nextIndex]
        _uiState.update {
            it.copy(
                currentInputSource = nextSource,
                lastActionTimestamp = System.currentTimeMillis(),
                logMessages = (listOf("Input switched to $nextSource") + it.logMessages).take(20)
            )
        }
        viewModelScope.launch {
            client.sendKey(SamsungKey.SOURCE)
        }
    }

    fun onSendKey(key: SamsungKey) {
        triggerHaptic(30)
        _uiState.update {
            it.copy(
                lastActionTimestamp = System.currentTimeMillis(),
                logMessages = (listOf("Pressed ${key.label}") + it.logMessages).take(20)
            )
        }
        viewModelScope.launch {
            client.sendKey(key)
        }
    }

    // --- Settings & Modes ---
    fun setControlMode(mode: RemoteControlMode) {
        triggerHaptic(20)
        _uiState.update { it.copy(controlMode = mode) }
    }

    fun toggleVirtualTvScreen() {
        triggerHaptic(20)
        _uiState.update { it.copy(showVirtualTvScreen = !it.showVirtualTvScreen) }
    }

    fun toggleHaptic(enabled: Boolean) {
        _uiState.update { it.copy(isHapticEnabled = enabled) }
    }

    fun toggleSound(enabled: Boolean) {
        _uiState.update { it.copy(isSoundEnabled = enabled) }
    }

    // --- Device Management ---
    fun connectToDevice(device: SamsungTVDevice) {
        viewModelScope.launch {
            repository.selectDevice(device.id)
            _uiState.update {
                it.copy(
                    connectedDevice = device,
                    connectionStatus = if (device.isVirtual) ConnectionStatus.VIRTUAL_SIMULATOR else ConnectionStatus.CONNECTING,
                    logMessages = (listOf("Connecting to ${device.name} (${device.ipAddress})") + it.logMessages).take(20)
                )
            }
            client.connect(device)
        }
    }

    fun startDiscovery() {
        startNetworkScan(showPromptIfFound = false)
    }

    fun startNetworkScan(showPromptIfFound: Boolean = false) {
        discoveryJob?.cancel()
        _uiState.update {
            it.copy(
                isNetworkScanning = true,
                connectionStatus = if (it.connectedDevice == null) ConnectionStatus.DISCOVERING else it.connectionStatus,
                logMessages = (listOf("Scanning local Wi-Fi for Samsung TVs...") + it.logMessages).take(20)
            )
        }
        discoveryJob = viewModelScope.launch {
            val found = networkScanner.scanLocalNetwork { tv: SamsungTVDevice ->
                onTvDiscovered(tv, showPromptIfFound)
            }
            _discoveredDevices.value = (_discoveredDevices.value + found).distinctBy { dev -> dev.ipAddress }
            _uiState.update {
                it.copy(
                    isNetworkScanning = false,
                    logMessages = (listOf("Wi-Fi Scan completed. Found ${found.size} Samsung TV(s)") + it.logMessages).take(20)
                )
            }
        }
    }

    fun onTvDiscovered(device: SamsungTVDevice, showPrompt: Boolean) {
        _discoveredDevices.update { list ->
            if (list.none { it.ipAddress == device.ipAddress }) list + device else list
        }
        val currentActive = _uiState.value.connectedDevice
        // If not already connected to this IP and not previously prompted in this session
        if (showPrompt && (currentActive == null || currentActive.ipAddress != device.ipAddress)) {
            if (!networkScanner.hasBeenPrompted(device.ipAddress)) {
                networkScanner.markPrompted(device.ipAddress)
                _uiState.update { it.copy(promptTvToConnect = device) }
            }
        }
    }

    fun acceptPromptAndConnect(device: SamsungTVDevice) {
        triggerHaptic(50)
        _uiState.update {
            it.copy(
                promptTvToConnect = null,
                logMessages = (listOf("Connecting to detected TV: ${device.name}") + it.logMessages).take(20)
            )
        }
        viewModelScope.launch {
            val id = repository.saveDevice(device)
            connectToDevice(device.copy(id = id))
        }
    }

    fun dismissPrompt() {
        triggerHaptic(20)
        _uiState.update { it.copy(promptTvToConnect = null) }
    }

    fun simulateDiscoveredTv() {
        val sampleTv = SamsungTVDevice(
            id = System.currentTimeMillis(),
            name = "Samsung The Frame 55\" 4K",
            ipAddress = "192.168.1.188",
            macAddress = "E4:7C:F9:AA:BB:CC",
            modelName = "The Frame QLED",
            isVirtual = false
        )
        networkScanner.clearPrompted(sampleTv.ipAddress)
        onTvDiscovered(sampleTv, showPrompt = true)
    }

    fun addManualDevice(name: String, ip: String, mac: String) {
        viewModelScope.launch {
            val newDevice = SamsungTVDevice(
                name = name.ifBlank { "Samsung Smart TV" },
                ipAddress = ip.trim(),
                macAddress = mac.trim(),
                modelName = "Samsung Smart TV",
                isVirtual = false
            )
            val id = repository.saveDevice(newDevice)
            connectToDevice(newDevice.copy(id = id))
        }
    }

    fun deleteDevice(device: SamsungTVDevice) {
        viewModelScope.launch {
            repository.deleteDevice(device.id)
            if (_uiState.value.connectedDevice?.id == device.id) {
                // Fall back to virtual device
                val devices = repository.getLastConnectedDevice()
                if (devices != null) {
                    connectToDevice(devices)
                }
            }
        }
    }

    // --- Channels management ---
    fun toggleFavoriteChannel(channel: TvChannel) {
        triggerHaptic(25)
        viewModelScope.launch {
            repository.toggleFavoriteChannel(channel.id, channel.isFavorite)
        }
    }

    fun addCustomChannel(number: Int, name: String, category: String) {
        viewModelScope.launch {
            val newChan = TvChannel(
                number = number,
                name = name.ifBlank { "Channel $number" },
                category = category.ifBlank { "Custom" },
                currentShow = "Live Broadcast",
                isFavorite = true
            )
            repository.addChannel(newChan)
        }
    }

    fun deleteChannel(channel: TvChannel) {
        viewModelScope.launch {
            repository.deleteChannel(channel.id)
        }
    }

    // --- Sleep Timer ---
    fun startSleepTimer(minutes: Int) {
        triggerHaptic(40)
        sleepTimerJob?.cancel()
        val totalSeconds = minutes * 60
        _uiState.update {
            it.copy(
                sleepTimerInitialSeconds = totalSeconds,
                sleepTimerRemainingSeconds = totalSeconds,
                logMessages = (listOf("Sleep Timer started for $minutes minutes") + it.logMessages).take(20)
            )
        }
        sleepTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _uiState.value.sleepTimerRemainingSeconds ?: break
                if (current <= 1) {
                    _uiState.update {
                        it.copy(
                            isPoweredOn = false,
                            sleepTimerRemainingSeconds = null,
                            sleepTimerInitialSeconds = null,
                            logMessages = (listOf("Sleep Timer expired: TV powered off") + it.logMessages).take(20)
                        )
                    }
                    triggerHaptic(100)
                    client.sendKey(SamsungKey.POWER)
                    break
                } else {
                    _uiState.update { it.copy(sleepTimerRemainingSeconds = current - 1) }
                }
            }
        }
    }

    fun cancelSleepTimer() {
        triggerHaptic(25)
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _uiState.update {
            it.copy(
                sleepTimerRemainingSeconds = null,
                sleepTimerInitialSeconds = null,
                logMessages = (listOf("Sleep Timer canceled") + it.logMessages).take(20)
            )
        }
    }

    // --- Favorite Channels Surfing ---
    fun surfNextFavorite() {
        val favs = favoriteChannels.value
        if (favs.isEmpty()) {
            onChannelUp()
            return
        }
        val currentNum = _uiState.value.currentChannelNumber
        val next = favs.firstOrNull { it.number > currentNum } ?: favs.first()
        onSelectChannel(next)
    }

    fun surfPrevFavorite() {
        val favs = favoriteChannels.value
        if (favs.isEmpty()) {
            onChannelDown()
            return
        }
        val currentNum = _uiState.value.currentChannelNumber
        val prev = favs.lastOrNull { it.number < currentNum } ?: favs.last()
        onSelectChannel(prev)
    }

    fun toggleFavoriteSurfingOnly() {
        triggerHaptic(20)
        _uiState.update {
            val nextState = !it.isFavoriteSurfingOnly
            it.copy(
                isFavoriteSurfingOnly = nextState,
                logMessages = (listOf(if (nextState) "Favorite Surfing ON" else "Favorite Surfing OFF") + it.logMessages).take(20)
            )
        }
    }

    // --- Voice Control ---
    fun startVoiceListening() {
        triggerHaptic(30)
        _uiState.update {
            it.copy(
                isVoiceListening = true,
                voiceRecognizedText = "",
                voiceFeedbackMessage = "Listening... speak TV command"
            )
        }
        voiceController.startListening(
            onListeningStarted = {
                _uiState.update { it.copy(voiceFeedbackMessage = "Listening... say 'Open Netflix' or 'Change channel to 5'") }
            },
            onRmsChanged = { rms ->
                _uiState.update { it.copy(voiceRmsDb = rms) }
            },
            onPartialResult = { partial ->
                _uiState.update { it.copy(voiceRecognizedText = partial) }
            },
            onFinalResult = { final ->
                _uiState.update { it.copy(voiceRecognizedText = final, isVoiceListening = false) }
                executeVoiceCommand(final)
            },
            onError = { error ->
                _uiState.update {
                    it.copy(
                        isVoiceListening = false,
                        voiceFeedbackMessage = error
                    )
                }
            }
        )
    }

    fun stopVoiceListening() {
        voiceController.stopListening()
        _uiState.update { it.copy(isVoiceListening = false) }
    }

    fun executeVoiceCommand(commandText: String) {
        if (commandText.isBlank()) return
        triggerHaptic(40)
        _uiState.update { it.copy(voiceRecognizedText = commandText) }
        val action = voiceController.parseCommand(commandText, allChannels.value)
        when (action) {
            is VoiceAction.VolumeUp -> {
                onVolumeUp()
                setVoiceFeedback("Action: Volume Up (+1)")
            }
            is VoiceAction.VolumeDown -> {
                onVolumeDown()
                setVoiceFeedback("Action: Volume Down (-1)")
            }
            is VoiceAction.SetVolume -> {
                onVolumeSet(action.level)
                setVoiceFeedback("Action: Set Volume to ${action.level}")
            }
            is VoiceAction.Mute -> {
                if (!_uiState.value.isMuted) onToggleMute()
                setVoiceFeedback("Action: TV Muted")
            }
            is VoiceAction.Unmute -> {
                if (_uiState.value.isMuted) onToggleMute()
                setVoiceFeedback("Action: TV Unmuted")
            }
            is VoiceAction.ChannelUp -> {
                onChannelUp()
                setVoiceFeedback("Action: Channel Up")
            }
            is VoiceAction.ChannelDown -> {
                onChannelDown()
                setVoiceFeedback("Action: Channel Down")
            }
            is VoiceAction.TuneChannelNumber -> {
                val target = allChannels.value.find { it.number == action.number }
                    ?: TvChannel(number = action.number, name = "Channel ${action.number}")
                onSelectChannel(target)
                setVoiceFeedback("Action: Tuned to Channel ${action.number} (${target.name})")
            }
            is VoiceAction.TuneChannelName -> {
                val target = allChannels.value.find { it.name.contains(action.name, ignoreCase = true) }
                    ?: allChannels.value.find { action.name.contains(it.name, ignoreCase = true) }
                if (target != null) {
                    onSelectChannel(target)
                    setVoiceFeedback("Action: Tuned to ${target.name} (CH ${target.number})")
                } else {
                    setVoiceFeedback("Channel '${action.name}' not found")
                }
            }
            is VoiceAction.LaunchApp -> {
                launchStreamingApp(action.app)
                setVoiceFeedback("Action: Opened ${action.app.displayName}")
            }
            is VoiceAction.PowerOff -> {
                if (_uiState.value.isPoweredOn) onTogglePower()
                setVoiceFeedback("Action: Power Off")
            }
            is VoiceAction.PowerOn -> {
                if (!_uiState.value.isPoweredOn) onTogglePower()
                setVoiceFeedback("Action: Power On")
            }
            is VoiceAction.Home -> {
                onSendKey(SamsungKey.HOME)
                setVoiceFeedback("Action: Opened Smart Hub")
            }
            is VoiceAction.Back -> {
                onSendKey(SamsungKey.RETURN)
                setVoiceFeedback("Action: Back")
            }
            is VoiceAction.Source -> {
                onCycleSource()
                setVoiceFeedback("Action: Switched Source")
            }
            is VoiceAction.PlayPause -> {
                onSendKey(SamsungKey.PLAY)
                setVoiceFeedback("Action: Play/Pause")
            }
            is VoiceAction.Unknown -> {
                setVoiceFeedback("Unrecognized: '${action.rawText}'. Try 'Volume up' or 'Channel 24'")
            }
        }
    }

    private fun setVoiceFeedback(msg: String) {
        _uiState.update {
            it.copy(
                voiceFeedbackMessage = msg,
                logMessages = (listOf("Voice: $msg") + it.logMessages).take(20)
            )
        }
    }

    fun launchStreamingApp(app: StreamingApp) {
        triggerHaptic(45)
        _uiState.update { current ->
            current.copy(
                currentInputSource = "Smart Hub: ${app.displayName}",
                currentShow = app.subtitle,
                activeStreamingAppId = app.id,
                lastActionTimestamp = System.currentTimeMillis(),
                logMessages = (listOf("Launching ${app.displayName} on Samsung TV...") + current.logMessages).take(20)
            )
        }
        viewModelScope.launch {
            val ok = client.launchApp(app.samsungAppId, app.displayName)
            _uiState.update {
                it.copy(
                    logMessages = (listOf(if (ok) "${app.displayName} launched on TV" else "Sent launch command for ${app.displayName}") + it.logMessages).take(20)
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.disconnect()
        discoveryJob?.cancel()
        sleepTimerJob?.cancel()
        voiceController.stopListening()
        networkScanner.stopPeriodicAutoScan()
    }
}
