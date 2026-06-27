package pt.paulinoo.dbotkt.player.lyrics

import net.dv8tion.jda.api.components.selections.SelectOption
import net.dv8tion.jda.api.components.selections.StringSelectMenu
import net.dv8tion.jda.api.entities.MessageEmbed
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel

/** Renders the lyrics chooser (select menu) and the lyrics embed shared by the command, button and listener. */
object LyricsView {
    const val SELECT_ID = "lyrics_select"

    private const val MENU_LIMIT = 25
    private val timestampRegex = Regex("""^\[\d{1,2}:\d{2}(?:\.\d{1,3})?]\s*""")

    /** Builds a select menu of lyric candidates, optionally pre-selecting [selectedId]. */
    fun buildMenu(
        results: List<LrcLibResult>,
        selectedId: Long?,
    ): StringSelectMenu {
        val options =
            results.take(MENU_LIMIT).map { result ->
                val kind = if (result.hasSynced) "synced" else "plain"
                val album = result.albumName?.takeIf { it.isNotBlank() }?.let { " • $it" }.orEmpty()
                SelectOption.of(truncate("${result.trackName} — ${result.artistName}", SelectOption.LABEL_MAX_LENGTH), result.id.toString())
                    .withDescription(
                        truncate("${formatDuration(result.durationSeconds)} • $kind$album", SelectOption.DESCRIPTION_MAX_LENGTH),
                    )
                    .withDefault(result.id == selectedId)
            }

        return StringSelectMenu.create(SELECT_ID)
            .setPlaceholder("Choose a lyrics version")
            .addOptions(options)
            .build()
    }

    /** Builds the embed showing a chosen result's lyrics (plain, or synced with timestamps stripped). */
    fun lyricsEmbed(result: LrcLibResult): MessageEmbed {
        val body =
            result.plainLyrics?.takeIf { it.isNotBlank() }
                ?: result.syncedLyrics?.takeIf { it.isNotBlank() }?.let { stripTimestamps(it) }
                ?: "No lyrics text available for this version."

        val truncated =
            if (body.length > MessageEmbed.DESCRIPTION_MAX_LENGTH) {
                body.take(MessageEmbed.DESCRIPTION_MAX_LENGTH - 1) + "…"
            } else {
                body
            }

        return Embed.create(
            level = EmbedLevel.INFO,
            title = truncate("🎤 ${result.trackName} — ${result.artistName}", MessageEmbed.TITLE_MAX_LENGTH),
            description = truncated,
            footer = "LRCLIB • ${formatDuration(result.durationSeconds)}${if (result.hasSynced) " • synced" else ""}",
        ).build()
    }

    private fun stripTimestamps(synced: String): String =
        synced.lineSequence()
            .map { it.replace(timestampRegex, "") }
            .joinToString("\n")
            .trim()

    private fun formatDuration(seconds: Double): String {
        val total = seconds.toLong()
        return "%d:%02d".format(total / 60, total % 60)
    }

    private fun truncate(
        text: String,
        max: Int,
    ): String = if (text.length <= max) text else text.take(max - 1) + "…"
}
