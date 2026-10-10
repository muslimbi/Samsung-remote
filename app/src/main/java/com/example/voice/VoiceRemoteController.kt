package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.model.StreamingApp
import com.example.model.TvChannel
import java.util.Locale

sealed class VoiceAction {
    object VolumeUp : VoiceAction()
    object VolumeDown : VoiceAction()
    data class SetVolume(val level: Int) : VoiceAction()
    object Mute : VoiceAction()
    object Unmute : VoiceAction()
    object ChannelUp : VoiceAction()
    object ChannelDown : VoiceAction()
    data class TuneChannelNumber(val number: Int) : VoiceAction()
    data class TuneChannelName(val name: String) : VoiceAction()
    data class LaunchApp(val app: StreamingApp) : VoiceAction()
    object PowerOn : VoiceAction()
    object PowerOff : VoiceAction()
    object Home : VoiceAction()
    object Back : VoiceAction()
    object Source : VoiceAction()
    object PlayPause : VoiceAction()
    data class Unknown(val rawText: String) : VoiceAction()
}

class VoiceRemoteController(private val context: Context? = null) {

    private val tag = "VoiceRemoteController"
    private var speechRecognizer: SpeechRecognizer? = null

    fun isSpeechRecognitionAvailable(): Boolean {
        val ctx = context ?: return false
        return try {
            SpeechRecognizer.isRecognitionAvailable(ctx)
        } catch (_: Exception) {
            false
        }
    }

    fun startListening(
        onListeningStarted: () -> Unit,
        onRmsChanged: (Float) -> Unit = {},
        onPartialResult: (String) -> Unit,
        onFinalResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        stopListening()

        val ctx = context
        if (ctx == null || !isSpeechRecognitionAvailable()) {
            onError("Voice recognition service not available. You can tap quick voice commands or type below.")
            return
        }

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(ctx).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        onListeningStarted()
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {
                        onRmsChanged(rmsdB)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {}

                    override fun onError(error: Int) {
                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Microphone recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Speech recognizer client error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required"
                            SpeechRecognizer.ERROR_NETWORK -> "Network connection required for voice search"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No command detected. Please try again."
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice recognizer is busy"
                            SpeechRecognizer.ERROR_SERVER -> "Voice server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                            else -> "Recognition error ($error)"
                        }
                        Log.w(tag, "Speech recognition error: $message")
                        onError(message)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        onFinalResult(text)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.firstOrNull()?.let { onPartialResult(it) }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(tag, "Failed to start speech recognizer", e)
            onError(e.message ?: "Failed to start microphone voice recognition")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }

    /**
     * Converts spoken number words (e.g., "five", "twenty four") into standard digit representations.
     */
    fun normalizeSpokenNumbers(input: String): String {
        var text = input
        val numberMap = listOf(
            "zero" to "0",
            "one" to "1",
            "two" to "2",
            "three" to "3",
            "four" to "4",
            "five" to "5",
            "six" to "6",
            "seven" to "7",
            "eight" to "8",
            "nine" to "9",
            "ten" to "10",
            "eleven" to "11",
            "twelve" to "12",
            "fifteen" to "15",
            "twenty four" to "24",
            "twenty-four" to "24",
            "twenty" to "20",
            "thirty" to "30",
            "forty" to "40",
            "fifty" to "50"
        )
        for ((word, digit) in numberMap) {
            text = text.replace(Regex("""\b$word\b""", RegexOption.IGNORE_CASE), digit)
        }
        return text
    }

    fun parseCommand(rawText: String, existingChannels: List<TvChannel>): VoiceAction {
        val normalized = normalizeSpokenNumbers(rawText)
        val clean = normalized.trim().lowercase(Locale.ROOT)

        // 1. Streaming App commands (e.g. "Open Netflix", "Launch YouTube", "Watch Disney Plus")
        if (clean.contains("netflix")) return VoiceAction.LaunchApp(StreamingApp.NETFLIX)
        if (clean.contains("youtube")) return VoiceAction.LaunchApp(StreamingApp.YOUTUBE)
        if (clean.contains("disney")) return VoiceAction.LaunchApp(StreamingApp.DISNEY_PLUS)
        if (clean.contains("prime") || clean.contains("amazon video")) return VoiceAction.LaunchApp(StreamingApp.PRIME_VIDEO)
        if (clean.contains("spotify")) return VoiceAction.LaunchApp(StreamingApp.SPOTIFY)
        if (clean.contains("tv plus") || clean.contains("samsung plus") || clean.contains("samsung tv")) {
            return VoiceAction.LaunchApp(StreamingApp.SAMSUNG_TV_PLUS)
        }

        // 2. Volume Commands
        if (clean.contains("unmute") || clean.contains("sound on")) {
            return VoiceAction.Unmute
        }
        if (clean.contains("mute") || clean.contains("silence") || clean.contains("quiet")) {
            return VoiceAction.Mute
        }
        if (clean.contains("volume up") || clean.contains("turn it up") || clean.contains("louder") ||
            clean.contains("raise volume") || clean.contains("increase volume") || clean == "up"
        ) {
            return VoiceAction.VolumeUp
        }
        if (clean.contains("volume down") || clean.contains("turn it down") || clean.contains("quieter") ||
            clean.contains("lower volume") || clean.contains("decrease volume") || clean == "down"
        ) {
            return VoiceAction.VolumeDown
        }

        // Set volume to specific level: e.g. "set volume to 40", "volume 35", "change volume to 20"
        val volRegex = Regex("""(?:volume|level)\s+(?:to\s+)?(\d{1,3})""")
        val volMatch = volRegex.find(clean)
        if (volMatch != null) {
            val level = volMatch.groupValues[1].toIntOrNull()
            if (level != null && level in 0..100) {
                return VoiceAction.SetVolume(level)
            }
        }

        // 3. Channel commands by number:
        // Handles: "change channel to 5", "change to channel 5", "switch channel to 5", "tune channel to 5",
        // "channel to 5", "channel 5", "switch to 5", "tune to 5", "go to channel 5", "ch 5", etc.
        val channelNumRegex = Regex(
            """(?:change\s+(?:the\s+)?channel\s+to|change\s+to\s+channel|switch\s+(?:the\s+)?channel\s+to|switch\s+to\s+channel|tune\s+(?:the\s+)?channel\s+to|tune\s+to\s+channel|go\s+to\s+channel|set\s+channel\s+to|channel\s+to|channel\s+number|channel|ch|tune\s+to|switch\s+to|go\s+to)\s+(\d{1,4})"""
        )
        val chanMatch = channelNumRegex.find(clean)
        if (chanMatch != null) {
            val num = chanMatch.groupValues[1].toIntOrNull()
            if (num != null) {
                return VoiceAction.TuneChannelNumber(num)
            }
        }

        // Secondary fallback for "number 5" or "to 5"
        val altNumRegex = Regex("""(?:number|no\.?|to)\s+(\d{1,4})""")
        val altMatch = altNumRegex.find(clean)
        if (altMatch != null && (clean.contains("channel") || clean.contains("tune") || clean.contains("switch") || clean.contains("change"))) {
            val num = altMatch.groupValues[1].toIntOrNull()
            if (num != null) {
                return VoiceAction.TuneChannelNumber(num)
            }
        }

        // Direct number alone: "5", "24", "7"
        val directNum = clean.toIntOrNull()
        if (directNum != null && directNum in 1..9999) {
            return VoiceAction.TuneChannelNumber(directNum)
        }

        if (clean.contains("channel up") || clean.contains("next channel")) {
            return VoiceAction.ChannelUp
        }
        if (clean.contains("channel down") || clean.contains("previous channel")) {
            return VoiceAction.ChannelDown
        }

        // 4. Channel commands by name / query
        for (chan in existingChannels) {
            val nameClean = chan.name.lowercase(Locale.ROOT)
            if (clean.contains(nameClean) || nameClean.contains(clean)) {
                return VoiceAction.TuneChannelName(chan.name)
            }
        }

        // Common Network Aliases
        if (clean.contains("abc")) return VoiceAction.TuneChannelName("ABC HD")
        if (clean.contains("espn")) return VoiceAction.TuneChannelName("ESPN Live")
        if (clean.contains("bbc")) return VoiceAction.TuneChannelName("BBC World News")
        if (clean.contains("discovery")) return VoiceAction.TuneChannelName("Discovery")
        if (clean.contains("hbo")) return VoiceAction.TuneChannelName("HBO Max")
        if (clean.contains("cnn")) return VoiceAction.TuneChannelName("CNN International")
        if (clean.contains("cartoon")) return VoiceAction.TuneChannelName("Cartoon Network")
        if (clean.contains("f1") || clean.contains("formula 1")) return VoiceAction.TuneChannelName("F1 Racing TV")

        // 5. Power & System commands
        if (clean.contains("turn off") || clean.contains("power off") || clean.contains("shutdown") || clean.contains("sleep")) {
            return VoiceAction.PowerOff
        }
        if (clean.contains("turn on") || clean.contains("power on")) {
            return VoiceAction.PowerOn
        }
        if (clean.contains("home") || clean.contains("smart hub") || clean.contains("menu")) {
            return VoiceAction.Home
        }
        if (clean.contains("back") || clean.contains("return") || clean.contains("exit")) {
            return VoiceAction.Back
        }
        if (clean.contains("source") || clean.contains("input") || clean.contains("hdmi")) {
            return VoiceAction.Source
        }
        if (clean.contains("play") || clean.contains("pause") || clean.contains("resume")) {
            return VoiceAction.PlayPause
        }

        return VoiceAction.Unknown(rawText)
    }

    companion object {
        val SAMPLE_COMMANDS = listOf(
            "Open Netflix",
            "Change channel to 5",
            "Open YouTube",
            "Open Disney Plus",
            "Volume up",
            "Set volume to 35",
            "Tune to ESPN",
            "Mute",
            "Unmute",
            "Next channel",
            "Power off"
        )
    }
}
