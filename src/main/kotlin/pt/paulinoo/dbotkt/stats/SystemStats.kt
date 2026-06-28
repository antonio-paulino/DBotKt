package pt.paulinoo.dbotkt.stats

import java.lang.management.ManagementFactory
import java.util.Locale

/** A point-in-time snapshot of JVM/OS health metrics. */
data class SystemSnapshot(
    val uptimeMillis: Long,
    val usedHeapBytes: Long,
    val maxHeapBytes: Long,
    val committedHeapBytes: Long,
    val threadCount: Int,
    val availableProcessors: Int,
    /** Process CPU load in 0.0..1.0, or -1.0 when unavailable. */
    val processCpuLoad: Double,
    /** System-wide CPU load in 0.0..1.0, or -1.0 when unavailable. */
    val systemCpuLoad: Double,
    val jvmVersion: String,
    val osName: String,
    val osArch: String,
)

/** Reads JVM and operating-system health metrics for the stats command. */
object SystemStats {
    private val runtimeMx = ManagementFactory.getRuntimeMXBean()
    private val threadMx = ManagementFactory.getThreadMXBean()
    private val memoryMx = ManagementFactory.getMemoryMXBean()
    private val osMx = ManagementFactory.getOperatingSystemMXBean()

    fun snapshot(): SystemSnapshot {
        val heap = memoryMx.heapMemoryUsage
        val sunOs = osMx as? com.sun.management.OperatingSystemMXBean

        return SystemSnapshot(
            uptimeMillis = runtimeMx.uptime,
            usedHeapBytes = heap.used,
            maxHeapBytes = heap.max,
            committedHeapBytes = heap.committed,
            threadCount = threadMx.threadCount,
            availableProcessors = osMx.availableProcessors,
            processCpuLoad = sunOs?.processCpuLoad ?: -1.0,
            systemCpuLoad = sunOs?.cpuLoad ?: -1.0,
            jvmVersion = System.getProperty("java.version") ?: "unknown",
            osName = osMx.name,
            osArch = osMx.arch,
        )
    }

    fun formatUptime(millis: Long): String {
        val totalSeconds = millis / 1000
        val days = totalSeconds / 86_400
        val hours = (totalSeconds % 86_400) / 3_600
        val minutes = (totalSeconds % 3_600) / 60
        val seconds = totalSeconds % 60

        return buildString {
            if (days > 0) append("${days}d ")
            if (days > 0 || hours > 0) append("${hours}h ")
            if (days > 0 || hours > 0 || minutes > 0) append("${minutes}m ")
            append("${seconds}s")
        }.trim()
    }

    fun formatBytes(bytes: Long): String {
        if (bytes < 0) return "n/a"
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb >= 1024) {
            String.format(Locale.US, "%.2f GB", mb / 1024)
        } else {
            String.format(Locale.US, "%.0f MB", mb)
        }
    }

    /** Formats a 0.0..1.0 load as a percentage, or "n/a" when the value is negative. */
    fun formatLoad(load: Double): String = if (load < 0) "n/a" else String.format(Locale.US, "%.1f%%", load * 100)
}
