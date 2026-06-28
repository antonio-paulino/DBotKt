package pt.paulinoo.dbotkt.config

import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Lightweight, dependency-free JSON store for per-guild settings. Reading reuses
 * Lavaplayer's [JsonBrowser]; writing rewrites the whole file atomically (settings change
 * rarely, so this is cheap and avoids partial writes). All access is thread-safe.
 */
class GuildSettingsStore(private val path: Path) {
    private val logger = LoggerFactory.getLogger(GuildSettingsStore::class.java)
    private val settings = java.util.concurrent.ConcurrentHashMap<Long, GuildSettings>()
    private val writeLock = Any()

    init {
        load()
    }

    fun get(guildId: Long): GuildSettings = settings[guildId] ?: GuildSettings()

    /** Applies [transform] to the guild's settings, persists, and returns the new value. */
    fun update(
        guildId: Long,
        transform: (GuildSettings) -> GuildSettings,
    ): GuildSettings {
        val updated = settings.compute(guildId) { _, current -> transform(current ?: GuildSettings()) }!!
        persist()
        return updated
    }

    private fun load() {
        if (!Files.exists(path)) return
        try {
            val json = JsonBrowser.parse(Files.readString(path)) ?: return
            for (key in json.keys()) {
                val id = key.toLongOrNull() ?: continue
                val entry = json.get(key)
                settings[id] =
                    GuildSettings(
                        prefix = entry.get("prefix").text(),
                        volume = entry.get("volume").text()?.toIntOrNull(),
                        equalizer = entry.get("equalizer").text() ?: "flat",
                    )
            }
            logger.info("Loaded settings for {} guild(s) from {}", settings.size, path)
        } catch (e: Exception) {
            logger.error("Failed to load guild settings from {}", path, e)
        }
    }

    private fun persist() {
        synchronized(writeLock) {
            try {
                val json = serializeAll(settings.toMap())
                path.toAbsolutePath().parent?.let { Files.createDirectories(it) }
                val tmp = path.resolveSibling("${path.fileName}.tmp")
                Files.writeString(tmp, json)
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception) {
                logger.error("Failed to persist guild settings to {}", path, e)
            }
        }
    }

    companion object {
        internal fun serializeAll(all: Map<Long, GuildSettings>): String =
            buildString {
                append("{")
                all.entries.forEachIndexed { index, (id, settings) ->
                    if (index > 0) append(",")
                    append("\"").append(id).append("\":").append(serialize(settings))
                }
                append("}")
            }

        private fun serialize(settings: GuildSettings): String {
            val parts = mutableListOf<String>()
            settings.prefix?.let { parts.add("\"prefix\":\"${escape(it)}\"") }
            settings.volume?.let { parts.add("\"volume\":$it") }
            parts.add("\"equalizer\":\"${escape(settings.equalizer)}\"")
            return "{${parts.joinToString(",")}}"
        }

        private fun escape(text: String): String =
            buildString {
                for (c in text) {
                    when (c) {
                        '"' -> append("\\\"")
                        '\\' -> append("\\\\")
                        '\n' -> append("\\n")
                        '\r' -> append("\\r")
                        '\t' -> append("\\t")
                        else -> if (c < ' ') append("\\u%04x".format(c.code)) else append(c)
                    }
                }
            }
    }
}
