package pt.paulinoo.dbotkt.commands

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CooldownManagerTest {
    @Test
    fun zeroCooldownAlwaysAllows() {
        val manager = CooldownManager(0)
        assertEquals(0, manager.check("user", "skip"))
        assertEquals(0, manager.check("user", "skip"))
    }

    @Test
    fun blocksWithinWindow() {
        val manager = CooldownManager(10_000)
        assertEquals(0, manager.check("user", "skip"), "first use is allowed")
        assertTrue(manager.check("user", "skip") > 0, "second use within the window is blocked")
    }

    @Test
    fun cooldownIsPerUserAndPerCommand() {
        val manager = CooldownManager(10_000)
        assertEquals(0, manager.check("user", "skip"))
        assertEquals(0, manager.check("user", "stop"), "different command is independent")
        assertEquals(0, manager.check("other", "skip"), "different user is independent")
    }
}
