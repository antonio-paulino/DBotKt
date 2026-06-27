package pt.paulinoo.dbotkt.player.embed

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlayerEmbedTest {
    @Test
    fun extractsYouTubeIdFromCommonUrlForms() {
        assertEquals("dQw4w9WgXcQ", PlayerEmbed.extractYouTubeId("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", PlayerEmbed.extractYouTubeId("https://www.youtube.com/watch?list=RD&v=dQw4w9WgXcQ&index=2"))
        assertEquals("dQw4w9WgXcQ", PlayerEmbed.extractYouTubeId("https://youtu.be/dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", PlayerEmbed.extractYouTubeId("https://www.youtube.com/shorts/dQw4w9WgXcQ"))
        assertNull(PlayerEmbed.extractYouTubeId("https://open.spotify.com/track/abc123"))
        assertNull(PlayerEmbed.extractYouTubeId(null))
    }

    @Test
    fun youTubeTracksUseReliableHqDefaultThumbnail() {
        val thumbnail =
            PlayerEmbed.resolveThumbnail(
                uri = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                artworkUrl = "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
            )
        assertEquals("https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg", thumbnail)
    }

    @Test
    fun nonYouTubeTracksFallBackToArtworkUrl() {
        val artwork = "https://i.scdn.co/image/ab67616d0000b273abcdef"
        assertEquals(artwork, PlayerEmbed.resolveThumbnail("https://open.spotify.com/track/xyz", artwork))
    }

    @Test
    fun returnsNullWhenNothingAvailable() {
        assertNull(PlayerEmbed.resolveThumbnail("https://example.com/audio.mp3", null))
        assertNull(PlayerEmbed.resolveThumbnail("https://example.com/audio.mp3", ""))
    }
}
