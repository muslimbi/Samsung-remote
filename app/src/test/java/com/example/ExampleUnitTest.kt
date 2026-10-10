package com.example

import com.example.model.SamsungKey
import com.example.model.SamsungTVDevice
import com.example.model.StreamingApp
import com.example.model.TvChannel
import com.example.model.TvState
import com.example.voice.VoiceAction
import com.example.voice.VoiceRemoteController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testInitialVolumeAndChannel() {
        val state = TvState(volume = 25, currentChannelNumber = 24)
        assertEquals(25, state.volume)
        assertEquals(24, state.currentChannelNumber)
        assertEquals(false, state.isMuted)
    }

    @Test
    fun testVolumeAdjustments() {
        val state = TvState(volume = 99)
        val volUp = (state.volume + 1).coerceAtMost(100)
        assertEquals(100, volUp)

        val capped = (volUp + 1).coerceAtMost(100)
        assertEquals(100, capped)

        val volDown = (capped - 5).coerceAtLeast(0)
        assertEquals(95, volDown)
    }

    @Test
    fun testSamsungKeyCodes() {
        assertEquals("KEY_VOLUP", SamsungKey.VOL_UP.keyCode)
        assertEquals("KEY_VOLDOWN", SamsungKey.VOL_DOWN.keyCode)
        assertEquals("KEY_MUTE", SamsungKey.MUTE.keyCode)
        assertEquals("KEY_CHUP", SamsungKey.CH_UP.keyCode)
        assertEquals("KEY_CHDOWN", SamsungKey.CH_DOWN.keyCode)
    }

    @Test
    fun testSleepTimerState() {
        val initialSeconds = 30 * 60
        val state = TvState(
            sleepTimerInitialSeconds = initialSeconds,
            sleepTimerRemainingSeconds = initialSeconds - 10
        )
        assertEquals(1800, state.sleepTimerInitialSeconds)
        assertEquals(1790, state.sleepTimerRemainingSeconds)
    }

    @Test
    fun testFavoriteSurfingCalculation() {
        val favs = listOf(
            TvChannel(number = 7, name = "BBC"),
            TvChannel(number = 15, name = "HBO"),
            TvChannel(number = 24, name = "ESPN")
        )
        val currentNum = 15
        val next = favs.firstOrNull { it.number > currentNum } ?: favs.first()
        assertEquals(24, next.number)

        val nextAfterLast = favs.firstOrNull { it.number > 24 } ?: favs.first()
        assertEquals(7, nextAfterLast.number)
    }

    @Test
    fun testDiscoveredTvPromptState() {
        val stateWithoutPrompt = TvState(promptTvToConnect = null, isNetworkScanning = false)
        assertNull(stateWithoutPrompt.promptTvToConnect)

        val discoveredTv = SamsungTVDevice(
            name = "Samsung QLED 65\"",
            ipAddress = "192.168.1.120",
            modelName = "QN90B Neo QLED"
        )
        val stateWithPrompt = stateWithoutPrompt.copy(
            promptTvToConnect = discoveredTv,
            isNetworkScanning = false
        )
        assertNotNull(stateWithPrompt.promptTvToConnect)
        assertEquals("192.168.1.120", stateWithPrompt.promptTvToConnect?.ipAddress)
        assertEquals("Samsung QLED 65\"", stateWithPrompt.promptTvToConnect?.name)
    }

    @Test
    fun testStreamingAppsMetadata() {
        assertEquals("3201907018807", StreamingApp.NETFLIX.samsungAppId)
        assertEquals("111299001912", StreamingApp.YOUTUBE.samsungAppId)
        assertEquals("3201901017640", StreamingApp.DISNEY_PLUS.samsungAppId)
        assertEquals("3201512006785", StreamingApp.PRIME_VIDEO.samsungAppId)
        assertEquals("org.tizen.tv-plus", StreamingApp.SAMSUNG_TV_PLUS.samsungAppId)
        assertEquals("3201606009684", StreamingApp.SPOTIFY.samsungAppId)

        val state = TvState(activeStreamingAppId = StreamingApp.NETFLIX.id)
        assertEquals("netflix", state.activeStreamingAppId)
    }

    @Test
    fun testVoiceCommandOpenNetflix() {
        val controller = VoiceRemoteController()
        val channels = listOf(
            TvChannel(number = 5, name = "ABC HD"),
            TvChannel(number = 24, name = "ESPN Live")
        )

        val actionOpenNetflix = controller.parseCommand("Open Netflix", channels)
        assertTrue(actionOpenNetflix is VoiceAction.LaunchApp)
        assertEquals(StreamingApp.NETFLIX, (actionOpenNetflix as VoiceAction.LaunchApp).app)

        val actionNetflixDirect = controller.parseCommand("netflix", channels)
        assertTrue(actionNetflixDirect is VoiceAction.LaunchApp)
        assertEquals(StreamingApp.NETFLIX, (actionNetflixDirect as VoiceAction.LaunchApp).app)

        val actionOpenYouTube = controller.parseCommand("Open YouTube", channels)
        assertTrue(actionOpenYouTube is VoiceAction.LaunchApp)
        assertEquals(StreamingApp.YOUTUBE, (actionOpenYouTube as VoiceAction.LaunchApp).app)

        val actionOpenDisney = controller.parseCommand("Open Disney Plus", channels)
        assertTrue(actionOpenDisney is VoiceAction.LaunchApp)
        assertEquals(StreamingApp.DISNEY_PLUS, (actionOpenDisney as VoiceAction.LaunchApp).app)
    }

    @Test
    fun testVoiceCommandChangeChannelTo5() {
        val controller = VoiceRemoteController()
        val channels = listOf(
            TvChannel(number = 5, name = "ABC HD"),
            TvChannel(number = 24, name = "ESPN Live")
        )

        // Digit command
        val action1 = controller.parseCommand("Change channel to 5", channels)
        assertTrue(action1 is VoiceAction.TuneChannelNumber)
        assertEquals(5, (action1 as VoiceAction.TuneChannelNumber).number)

        // Word number command: "five"
        val action2 = controller.parseCommand("change channel to five", channels)
        assertTrue(action2 is VoiceAction.TuneChannelNumber)
        assertEquals(5, (action2 as VoiceAction.TuneChannelNumber).number)

        // Alternate phrasing
        val action3 = controller.parseCommand("switch to channel 5", channels)
        assertTrue(action3 is VoiceAction.TuneChannelNumber)
        assertEquals(5, (action3 as VoiceAction.TuneChannelNumber).number)

        val action4 = controller.parseCommand("channel 5", channels)
        assertTrue(action4 is VoiceAction.TuneChannelNumber)
        assertEquals(5, (action4 as VoiceAction.TuneChannelNumber).number)
    }

    @Test
    fun testVoiceCommandVolumeAndPower() {
        val controller = VoiceRemoteController()
        val channels = emptyList<TvChannel>()

        val volUp = controller.parseCommand("volume up", channels)
        assertTrue(volUp is VoiceAction.VolumeUp)

        val setVol = controller.parseCommand("set volume to 35", channels)
        assertTrue(setVol is VoiceAction.SetVolume)
        assertEquals(35, (setVol as VoiceAction.SetVolume).level)

        val mute = controller.parseCommand("mute", channels)
        assertTrue(mute is VoiceAction.Mute)

        val powerOff = controller.parseCommand("power off", channels)
        assertTrue(powerOff is VoiceAction.PowerOff)
    }
}
