package pt.paulinoo.dbotkt.stats

import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StatsServiceTest {
    private fun sampleSnapshot() =
        StatsSnapshot(
            status = "CONNECTED",
            gatewayPing = 42,
            healthy = true,
            guilds = 3,
            members = 100,
            voiceConnections = 1,
            players = 2,
            playing = 1,
            paused = 1,
            queuedTracks = 5,
            queuedDurationMs = 60_000,
            tracksPlayed = 10,
            commandsExecuted = 20,
            errors = 1,
            system =
                SystemSnapshot(
                    uptimeMillis = 1_000,
                    usedHeapBytes = 1_024,
                    maxHeapBytes = 2_048,
                    committedHeapBytes = 1_500,
                    threadCount = 8,
                    availableProcessors = 4,
                    processCpuLoad = 0.1,
                    systemCpuLoad = 0.2,
                    jvmVersion = "25",
                    osName = "Linux",
                    osArch = "amd64",
                ),
        )

    @Test
    fun toJsonProducesWellFormedJsonWithExpectedValues() {
        val json = StatsService.toJson(sampleSnapshot())

        val parsed = JsonBrowser.parse(json)
        assertTrue(parsed.get("healthy").asBoolean(false))
        assertEquals(42L, parsed.get("gatewayPingMs").asLong(0))
        assertEquals(3, parsed.get("guilds").asInt(0))
        assertEquals(1, parsed.get("players").get("playing").asInt(-1))
        assertEquals(10L, parsed.get("totals").get("tracksPlayed").asLong(0))
        assertEquals(20L, parsed.get("totals").get("commands").asLong(0))
        assertEquals(2048L, parsed.get("memory").get("maxHeapBytes").asLong(0))
        assertEquals(4, parsed.get("cores").asInt(0))
    }
}
