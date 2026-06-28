package pt.paulinoo.dbotkt.commands

import java.util.concurrent.ConcurrentHashMap

/** Per-user, per-command cooldown to throttle command spam. */
class CooldownManager(private val cooldownMillis: Long) {
    private val lastUse = ConcurrentHashMap<String, Long>()

    /**
     * Returns the remaining cooldown in milliseconds (0 when the command may run). When it
     * returns 0 the use is recorded, starting a fresh cooldown window.
     */
    fun check(
        userId: String,
        command: String,
    ): Long {
        if (cooldownMillis <= 0) return 0

        val key = "$userId:$command"
        val now = System.currentTimeMillis()
        val last = lastUse[key]
        if (last != null && now - last < cooldownMillis) {
            return cooldownMillis - (now - last)
        }
        lastUse[key] = now
        return 0
    }
}
