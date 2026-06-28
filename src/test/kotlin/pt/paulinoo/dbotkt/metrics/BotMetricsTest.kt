package pt.paulinoo.dbotkt.metrics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BotMetricsTest {
    @Test
    fun countersIncrementRelativeToSnapshot() {
        val before = BotMetrics.snapshot()

        BotMetrics.commandExecuted()
        BotMetrics.trackPlayed()
        BotMetrics.errorOccurred()

        val after = BotMetrics.snapshot()
        assertEquals(before.commandsExecuted + 1, after.commandsExecuted)
        assertEquals(before.tracksPlayed + 1, after.tracksPlayed)
        assertEquals(before.errors + 1, after.errors)
        assertTrue(after.uptimeMillis >= 0)
    }
}
