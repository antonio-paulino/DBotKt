package pt.paulinoo.dbotkt.player.lyrics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LyricsViewTest {
    private fun result(
        id: Long,
        trackName: String = "Track",
        artistName: String = "Artist",
        plain: String? = "plain lyrics",
        synced: String? = "[00:00.00] line",
    ) = LrcLibResult(
        id = id,
        trackName = trackName,
        artistName = artistName,
        albumName = "Album",
        durationSeconds = 200.0,
        instrumental = false,
        plainLyrics = plain,
        syncedLyrics = synced,
    )

    @Test
    fun buildMenuEncodesIdsAndPreselectsBest() {
        val results = listOf(result(id = 11), result(id = 22), result(id = 33))

        val menu = LyricsView.buildMenu(results, selectedId = 22)

        assertEquals(LyricsView.SELECT_ID, menu.customId)
        assertEquals(listOf("11", "22", "33"), menu.options.map { it.value })
        assertEquals(setOf("22"), menu.options.filter { it.isDefault }.map { it.value }.toSet())
    }

    @Test
    fun buildMenuRespectsDiscordOptionLimit() {
        val many = (1..40L).map { result(id = it) }

        val menu = LyricsView.buildMenu(many, selectedId = null)

        assertTrue(menu.options.size <= 25, "Discord allows at most 25 select options")
    }

    @Test
    fun lyricsEmbedStripsTimestampsWhenOnlySynced() {
        val synced =
            """
            [00:00.15] Is this the real life?
            [00:07.13] Caught in a landslide
            """.trimIndent()

        val embed = LyricsView.lyricsEmbed(result(id = 1, plain = null, synced = synced))

        val description = embed.description ?: ""
        assertFalse(description.contains("["), "Timestamps should be stripped from synced-only lyrics")
        assertTrue(description.contains("Is this the real life?"))
        assertTrue(description.contains("Caught in a landslide"))
    }

    @Test
    fun lyricsEmbedTitleIncludesTrackAndArtist() {
        val embed = LyricsView.lyricsEmbed(result(id = 1, trackName = "Bohemian Rhapsody", artistName = "Queen"))

        val title = embed.title ?: ""
        assertTrue(title.contains("Bohemian Rhapsody"))
        assertTrue(title.contains("Queen"))
    }
}
