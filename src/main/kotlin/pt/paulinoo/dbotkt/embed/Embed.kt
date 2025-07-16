package pt.paulinoo.dbotkt.embed

import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color
import java.time.Instant

enum class EmbedLevel(val color: Color) {
    ERROR(Color(0xED4245)), // Red - for failures
    SUCCESS(Color(0x57F287)), // Green - confirmations
    INFO(Color(0x5865F2)), // Blue - general bot info
    WARNING(Color(0xFEE75C)), // Yellow - temporary or cautionary
    SYSTEM(Color(0x2C2F33)), // Dark gray - for things like !queue cleared
}

object Embed {
    fun create(
        level: EmbedLevel,
        title: String? = null,
        description: String? = null,
        footer: String? = null,
        footerIconUrl: String? = null,
        thumbnailUrl: String? = null,
        timestamp: Boolean = true,
    ): EmbedBuilder {
        return EmbedBuilder().apply {
            setColor(level.color)
            title?.let { setTitle(it) }
            description?.let { setDescription(it) }
            thumbnailUrl?.let { setThumbnail(it) }
            if (footer != null) setFooter(footer, footerIconUrl)
            if (timestamp) setTimestamp(Instant.now())
        }
    }
}
