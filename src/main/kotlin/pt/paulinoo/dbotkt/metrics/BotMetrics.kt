package pt.paulinoo.dbotkt.metrics

import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

/** Cumulative, in-memory counters tracked since the process started. */
object BotMetrics {
    val startedAt: Instant = Instant.now()

    private val tracksPlayed = AtomicLong()
    private val commandsExecuted = AtomicLong()
    private val errors = AtomicLong()

    fun trackPlayed() {
        tracksPlayed.incrementAndGet()
    }

    fun commandExecuted() {
        commandsExecuted.incrementAndGet()
    }

    fun errorOccurred() {
        errors.incrementAndGet()
    }

    fun snapshot(): MetricsSnapshot =
        MetricsSnapshot(
            tracksPlayed = tracksPlayed.get(),
            commandsExecuted = commandsExecuted.get(),
            errors = errors.get(),
            uptimeMillis = Duration.between(startedAt, Instant.now()).toMillis(),
        )
}

data class MetricsSnapshot(
    val tracksPlayed: Long,
    val commandsExecuted: Long,
    val errors: Long,
    val uptimeMillis: Long,
)
