package pt.paulinoo.dbotkt.config

import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GuildSettingsStoreTest {
    private val tempFiles = mutableListOf<Path>()

    private fun freshFile(): Path {
        val file = Files.createTempFile("guild-settings", ".json")
        Files.deleteIfExists(file) // start with no file; the store creates it on first write
        tempFiles.add(file)
        return file
    }

    @AfterTest
    fun cleanup() {
        tempFiles.forEach { Files.deleteIfExists(it) }
    }

    @Test
    fun persistsAndReloadsAllFields() {
        val file = freshFile()
        GuildSettingsStore(file).update(123L) {
            it.copy(prefix = "?", volume = 80, equalizer = "rock")
        }

        val reloaded = GuildSettingsStore(file).get(123L)

        assertEquals("?", reloaded.prefix)
        assertEquals(80, reloaded.volume)
        assertEquals("rock", reloaded.equalizer)
    }

    @Test
    fun returnsDefaultsForUnknownGuild() {
        val settings = GuildSettingsStore(freshFile()).get(999L)
        assertNull(settings.prefix)
        assertNull(settings.volume)
        assertEquals("flat", settings.equalizer)
    }

    @Test
    fun serializesValidJsonWithEscapedPrefix() {
        val json = GuildSettingsStore.serializeAll(mapOf(1L to GuildSettings(prefix = "a\"b\\c")))

        // Must be valid JSON and round-trip the special characters intact.
        val parsed = JsonBrowser.parse(json)
        assertEquals("a\"b\\c", parsed.get("1").get("prefix").text())
    }
}
