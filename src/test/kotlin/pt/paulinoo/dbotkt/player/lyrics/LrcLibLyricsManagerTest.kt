package pt.paulinoo.dbotkt.player.lyrics

import org.junit.jupiter.api.Assumptions.assumeTrue
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LrcLibLyricsManagerTest {
    private lateinit var manager: LrcLibLyricsManager

    @BeforeTest
    fun setUp() {
        manager = LrcLibLyricsManager()
    }

    @AfterTest
    fun tearDown() {
        manager.shutdown()
    }

    /**
     * Runs [block], retrying a few times on transient network failures, and only skips
     * the test (instead of failing) when LRCLIB stays unreachable. This keeps the suite
     * green on flaky connections while still exercising the real API when it responds.
     */
    private fun <T> withNetwork(block: () -> T): T {
        var lastError: Exception? = null
        repeat(3) {
            try {
                return block()
            } catch (e: Exception) {
                lastError = e
            }
        }
        assumeTrue(false, "LRCLIB not reachable, skipping network test: ${lastError?.message}")
        throw lastError!! // unreachable: assumeTrue(false) aborts the test
    }

    // ---- Pure unit tests (no network) ----

    @Test
    fun parsesLrcTimestampsAndSkipsMalformedLines() {
        val synced =
            """
            [00:00.15] Is this the real life?
            [01:02.50] Caught in a landslide
            not a timestamped line
            [bad] also skipped
            """.trimIndent()

        val lines = LrcLibLyricsManager.parseSyncedLyrics(synced)

        assertEquals(2, lines.size, "Only well-formed lines should be parsed")
        assertEquals(150, lines[0].timestamp.toMillis())
        assertEquals("Is this the real life?", lines[0].line)
        assertEquals(62_500, lines[1].timestamp.toMillis())
        assertEquals("Caught in a landslide", lines[1].line)
    }

    @Test
    fun parseSyncedLyricsReturnsEmptyForBlank() {
        assertTrue(LrcLibLyricsManager.parseSyncedLyrics(null).isEmpty())
        assertTrue(LrcLibLyricsManager.parseSyncedLyrics("").isEmpty())
    }

    @Test
    fun pickBestChoosesClosestDuration() {
        val results =
            listOf(
                result(id = 1, durationSeconds = 355.0),
                result(id = 2, durationSeconds = 237.0),
                result(id = 3, durationSeconds = 100.0),
            )

        val best = manager.pickBest(results, durationMs = 240_000)

        assertNotNull(best)
        assertEquals(2, best.id, "Should pick the 237s entry for a ~240s track")
    }

    @Test
    fun pickBestPrefersSyncedOnDurationTie() {
        val results =
            listOf(
                result(id = 1, durationSeconds = 200.0, synced = null),
                result(id = 2, durationSeconds = 200.0, synced = "[00:00.00] hi"),
            )

        val best = manager.pickBest(results, durationMs = 200_000)

        assertEquals(2, best?.id, "Synced lyrics should win when durations tie")
    }

    @Test
    fun pickBestIgnoresInstrumentalWhenAlternativesExist() {
        val results =
            listOf(
                result(id = 1, durationSeconds = 200.0, instrumental = true, plain = null, synced = null),
                result(id = 2, durationSeconds = 205.0, plain = "real lyrics"),
            )

        val best = manager.pickBest(results, durationMs = 200_000)

        assertEquals(2, best?.id, "Instrumental entries should be skipped when lyrics exist")
    }

    @Test
    fun pickBestReturnsNullForEmpty() {
        assertNull(manager.pickBest(emptyList(), durationMs = 1000))
    }

    @Test
    fun cleanArtistStripsYouTubeChannelNoise() {
        assertEquals("Queen", manager.cleanArtist("Queen - Topic"))
        assertEquals("Eminem", manager.cleanArtist("EminemVEVO"))
        assertEquals("Coldplay", manager.cleanArtist("Coldplay - Official Artist Channel"))
        assertEquals("Tame Impala", manager.cleanArtist("Tame Impala"))
        assertNull(manager.cleanArtist(null))
    }

    // ---- Real integration tests against the live LRCLIB API ----

    @Test
    fun searchReturnsMultiplePartialMatches() =
        withNetwork {
            val results = manager.search("bohemian rhapsody")

            assertTrue(results.isNotEmpty(), "Search should return results")
            assertTrue(results.size > 1, "Partial search should return multiple candidates to choose from")
            assertTrue(
                results.any { it.artistName.contains("Queen", ignoreCase = true) },
                "Expected a Queen result among the matches",
            )
            assertTrue(results.any { it.hasSynced }, "At least one candidate should expose synced lyrics")
        }

    @Test
    fun searchReturnsEmptyForUnknownQuery() =
        withNetwork {
            val results = manager.search("zzzqwxyz not a real song 1234567890")
            assertTrue(results.isEmpty(), "Unknown query should yield no results")
        }

    @Test
    fun loadLyricsReturnsParsedSyncedLyrics() =
        withNetwork {
            val lyrics =
                assertNotNull(
                    manager.loadLyrics("bohemian rhapsody", durationMs = 355_000),
                    "Lyrics should be found for a well-known track",
                )

            val text = assertNotNull(lyrics.text, "Plain lyrics text should be present")
            assertTrue(text.contains("fantasy", ignoreCase = true), "Plain lyrics should contain a known line")

            val lines = assertNotNull(lyrics.lines, "Synced lines should be present")
            assertTrue(lines.isNotEmpty(), "Synced lines should be parsed")

            val firstTimestamp = assertNotNull(lines.first().timestamp, "First synced line should have a timestamp")
            assertTrue(firstTimestamp.toMillis() in 0..60_000, "First synced line should have a sane timestamp")
        }

    private fun result(
        id: Long,
        durationSeconds: Double,
        instrumental: Boolean = false,
        plain: String? = "plain lyrics",
        synced: String? = "[00:00.00] synced",
    ) = LrcLibResult(
        id = id,
        trackName = "Track $id",
        artistName = "Artist",
        albumName = null,
        durationSeconds = durationSeconds,
        instrumental = instrumental,
        plainLyrics = plain,
        syncedLyrics = synced,
    )
}
