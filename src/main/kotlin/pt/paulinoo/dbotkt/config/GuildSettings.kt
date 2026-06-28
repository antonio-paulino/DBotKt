package pt.paulinoo.dbotkt.config

/** Per-guild, persisted configuration. Null fields fall back to the global defaults. */
data class GuildSettings(
    val prefix: String? = null,
    val volume: Int? = null,
    val equalizer: String = "flat",
)
