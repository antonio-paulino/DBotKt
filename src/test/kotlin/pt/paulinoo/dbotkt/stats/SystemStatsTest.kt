package pt.paulinoo.dbotkt.stats

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SystemStatsTest {
    @Test
    fun formatUptimeBreaksDownUnits() {
        assertEquals("0s", SystemStats.formatUptime(0))
        assertEquals("5s", SystemStats.formatUptime(5_000))
        assertEquals("1m 5s", SystemStats.formatUptime(65_000))
        assertEquals("1h 1m 1s", SystemStats.formatUptime(3_661_000))
        assertEquals("1d 1h 1m 1s", SystemStats.formatUptime(90_061_000))
    }

    @Test
    fun formatBytesUsesMegabytesAndGigabytes() {
        assertEquals("n/a", SystemStats.formatBytes(-1))
        assertEquals("0 MB", SystemStats.formatBytes(0))
        assertEquals("256 MB", SystemStats.formatBytes(256L * 1024 * 1024))
        assertEquals("2.00 GB", SystemStats.formatBytes(2L * 1024 * 1024 * 1024))
    }

    @Test
    fun formatLoadUsesDotDecimalAndHandlesUnavailable() {
        assertEquals("n/a", SystemStats.formatLoad(-1.0))
        assertEquals("0.0%", SystemStats.formatLoad(0.0))
        assertEquals("12.3%", SystemStats.formatLoad(0.123))
        assertEquals("100.0%", SystemStats.formatLoad(1.0))
    }

    @Test
    fun snapshotReturnsSaneValues() {
        val snapshot = SystemStats.snapshot()
        assertTrue(snapshot.uptimeMillis >= 0)
        assertTrue(snapshot.availableProcessors > 0)
        assertTrue(snapshot.threadCount > 0)
        assertTrue(snapshot.maxHeapBytes > 0)
        assertTrue(snapshot.usedHeapBytes > 0)
    }
}
