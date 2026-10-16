package com.sonza.app.ai.orbit

import com.sonza.app.core.model.Song
import com.sonza.app.core.model.SongSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * End-to-end voice pipeline unit test suite for Sonza Orbit Assistant.
 * Validates conversational context memory, intent parsing, semantic variations,
 * mandatory spoken responses, corrections, and disambiguation.
 */
class OrbitAssistantVoiceTest {

    private lateinit var contextMemory: OrbitContextMemory

    private val sampleSong = Song(
        id = "test_song_1",
        title = "Tumhein Mohabbat Hai",
        artist = "Arijit Singh",
        album = "Atrangi Re",
        duration = 240000L,
        thumbnailUrl = "https://example.com/art.jpg",
        source = SongSource.YOUTUBE
    )

    @Before
    fun setUp() {
        contextMemory = OrbitContextMemory()
        contextMemory.updatePlayback(sampleSong)
    }

    @Test
    fun testCoreConversationExperience_IntentParsing() {
        // "Hey Orbit, play Tumhein Mohabbat Hai"
        val playIntent = OrbitIntentParser.parse(
            rawInput = "Hey Orbit, play Tumhein Mohabbat Hai",
            currentSong = sampleSong,
            contextMemory = contextMemory
        )
        assertTrue(playIntent is OrbitIntent.PlayTrackIntent)
        assertEquals("tumhein mohabbat hai", (playIntent as OrbitIntent.PlayTrackIntent).trackName.lowercase())

        // "make it louder" -> Volume UP
        val louderIntent = OrbitIntentParser.parse("make it louder", sampleSong, contextMemory = contextMemory)
        assertTrue(louderIntent is OrbitIntent.VolumeIntent)
        assertEquals(VolumeAction.UP, (louderIntent as OrbitIntent.VolumeIntent).action)

        // "what's the next song?" -> Next track query
        val nextSongIntent = OrbitIntentParser.parse("what's the next song?", sampleSong, contextMemory = contextMemory)
        assertTrue(nextSongIntent is OrbitIntent.QueueNextInfoIntent)

        // "skip it" -> Next track
        val skipIntent = OrbitIntentParser.parse("skip it", sampleSong, contextMemory = contextMemory)
        assertTrue(skipIntent is OrbitIntent.NextTrackIntent)

        // "pause" -> Pause
        val pauseIntent = OrbitIntentParser.parse("pause", sampleSong, contextMemory = contextMemory)
        assertTrue(pauseIntent is OrbitIntent.PauseIntent)

        // "resume" -> Resume
        val resumeIntent = OrbitIntentParser.parse("resume", sampleSong, contextMemory = contextMemory)
        assertTrue(resumeIntent is OrbitIntent.ResumeIntent)
    }

    @Test
    fun testMultiTurnFollowUpContext() {
        // Turn 1: "Play Arijit Singh"
        val turn1Intent = OrbitIntentParser.parse("Play Arijit Singh", sampleSong, contextMemory = contextMemory)
        assertTrue(turn1Intent is OrbitIntent.PlayArtistIntent)
        assertEquals("Arijit Singh", (turn1Intent as OrbitIntent.PlayArtistIntent).artistName)

        contextMemory.recordTurn(
            userUtterance = "Play Arijit Singh",
            intent = turn1Intent,
            spokenResponse = "Playing top songs by Arijit Singh."
        )

        // Turn 2: Follow-up "Something romantic" -> resolves artist from contextMemory
        val turn2Intent = OrbitIntentParser.parse("Something romantic", sampleSong, contextMemory = contextMemory)
        assertTrue(turn2Intent is OrbitIntent.PlayArtistIntent)
        val playArtist = turn2Intent as OrbitIntent.PlayArtistIntent
        assertEquals("Arijit Singh", playArtist.artistName)
        assertEquals("romantic", playArtist.moodOrGenre)

        // Turn 3: "Another one" -> resolves artist
        val turn3Intent = OrbitIntentParser.parse("Another one", sampleSong, contextMemory = contextMemory)
        assertTrue(turn3Intent is OrbitIntent.PlayArtistIntent)
        assertEquals("Arijit Singh", (turn3Intent as OrbitIntent.PlayArtistIntent).artistName)

        // Turn 4: "Who wrote this?" -> resolves to currentSong
        val turn4Intent = OrbitIntentParser.parse("Who wrote this?", sampleSong, contextMemory = contextMemory)
        assertTrue(turn4Intent is OrbitIntent.SongInfoIntent)
        assertEquals(SongInfoType.WRITER, (turn4Intent as OrbitIntent.SongInfoIntent).queryType)

        // Turn 5: "Add it to my favorites"
        val turn5Intent = OrbitIntentParser.parse("Add it to my favorites", sampleSong, contextMemory = contextMemory)
        assertTrue(turn5Intent is OrbitIntent.FavoriteIntent)
        assertTrue((turn5Intent as OrbitIntent.FavoriteIntent).isFavorite)
    }

    @Test
    fun testCorrectionsAndDisambiguation() {
        // "No, not that one"
        val correction1 = OrbitIntentParser.parse("No, not that one", sampleSong, contextMemory = contextMemory)
        assertTrue(correction1 is OrbitIntent.CorrectionIntent)

        // "Actually, play the remix"
        val correction2 = OrbitIntentParser.parse("Actually, play the remix", sampleSong, contextMemory = contextMemory)
        assertTrue(correction2 is OrbitIntent.CorrectionIntent)

        // Disambiguation choice
        val choiceIntent = OrbitIntentParser.parse(
            rawInput = "The acoustic one",
            currentSong = sampleSong,
            dialogueContext = OrbitDialogueContext.AwaitingDisambiguation(
                options = listOf("Original", "Acoustic", "Remix")
            ),
            contextMemory = contextMemory
        )
        assertTrue(choiceIntent is OrbitIntent.DisambiguationChoiceIntent)
        assertEquals("the acoustic one", (choiceIntent as OrbitIntent.DisambiguationChoiceIntent).choice)
    }

    @Test
    fun testMandatorySpokenResponses_EveryCommandSpeaks() {
        // Test that OrbitResponseEngine returns non-empty, contextual spoken responses for all key commands
        val playRes = OrbitResponseEngine.playSong("Tumhein Mohabbat Hai", "Arijit Singh")
        assertEquals("Playing Tumhein Mohabbat Hai by Arijit Singh.", playRes.spokenResponse)
        assertNotNull(playRes.visualFeedback)

        val pauseRes = OrbitResponseEngine.pause()
        assertEquals("Paused.", pauseRes.spokenResponse)

        val resumeRes = OrbitResponseEngine.resume()
        assertEquals("Resuming playback.", resumeRes.spokenResponse)

        val nextRes = OrbitResponseEngine.next()
        assertEquals("Skipping to the next song.", nextRes.spokenResponse)

        val prevRes = OrbitResponseEngine.previous()
        assertEquals("Going back to the previous song.", prevRes.spokenResponse)

        val volRes = OrbitResponseEngine.volume(70, isUp = true)
        assertEquals("Volume is now 70 percent.", volRes.spokenResponse)

        val muteRes = OrbitResponseEngine.mute(true)
        assertEquals("Muted.", muteRes.spokenResponse)

        val unmuteRes = OrbitResponseEngine.mute(false)
        assertEquals("Unmuted.", unmuteRes.spokenResponse)

        val lyricsRes = OrbitResponseEngine.lyrics(true)
        assertEquals("Opening lyrics.", lyricsRes.spokenResponse)

        val helpRes = OrbitResponseEngine.help()
        assertTrue(helpRes.spokenResponse.contains("play and control music"))

        val unknownRes = OrbitResponseEngine.unknown("asdfghjk")
        assertTrue(unknownRes.spokenResponse.contains("didn't quite catch that"))

        val cancelRes = OrbitResponseEngine.cancel()
        assertEquals("Alright.", cancelRes.spokenResponse)
    }

    @Test
    fun testSemanticSynonyms() {
        val synonymsForNext = listOf("skip", "skip this", "next", "play another", "move on", "change the song")
        for (synonym in synonymsForNext) {
            val intent = OrbitIntentParser.parse(synonym, sampleSong, contextMemory = contextMemory)
            assertTrue("Expected NextTrackIntent for '$synonym'", intent is OrbitIntent.NextTrackIntent)
        }

        val synonymsForPause = listOf("stop", "pause", "hold it", "stop the music")
        for (synonym in synonymsForPause) {
            val intent = OrbitIntentParser.parse(synonym, sampleSong, contextMemory = contextMemory)
            assertTrue("Expected Pause/Stop intent for '$synonym'", intent is OrbitIntent.PauseIntent || intent is OrbitIntent.StopIntent)
        }
    }
}
