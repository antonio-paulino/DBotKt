package pt.paulinoo.dbotkt.commands.help

import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.emoji.Emoji
import pt.paulinoo.dbotkt.di.ServiceLocator
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel

object HelpPages {
    // Button id format: help_page:<index>:<a|u>  (a = admin, u = user)
    const val BUTTON_PREFIX = "help_page:"

    private val leftEmoji = Emoji.fromUnicode("U+2B05")
    private val rightEmoji = Emoji.fromUnicode("U+27A1")
    private val deleteEmoji = Emoji.fromUnicode("U+274C")

    val admins: Set<String> =
        System.getenv("ADMIN_IDS")
            ?.split(",")
            ?.map { it.trim() }
            ?.toSet()
            ?: emptySet()

    data class HelpEntry(
        val title: String,
        val adminOnly: Boolean = false,
        val body: (prefix: String) -> String,
    )

    private val allPages =
        listOf(
            HelpEntry("🎵 Playback") { p ->
                """
                `${p}play <query | YouTube/Spotify URL>`
                Play a song or playlist. For plain text, searches YouTube. Supports YouTube and Spotify URLs (tracks and playlists).

                `${p}pause` / `${p}resume`
                Pause or resume the current playback.

                `${p}skip`
                Skip to the next song in the queue.

                `${p}skipto <number>`
                Skip directly to a specific position in the queue.

                `${p}stop`
                Stop playback, clear the entire queue and leave the voice channel.
                """.trimIndent()
            },
            HelpEntry("📋 Queue Management") { p ->
                """
                `${p}queue`
                Show the current queue with pagination (10 tracks per page). Use the arrow buttons to navigate.

                `${p}clearqueue`
                Remove all tracks from the queue (keeps the current song playing).

                `${p}shuffle`
                Randomize the order of the queued tracks.

                `${p}reverse`
                Reverse the order of the queued tracks.

                `${p}swap <a> <b>`
                Swap two tracks in the queue by their position numbers.

                `${p}remove <index>`
                Remove a specific track from the queue by its position number.
                """.trimIndent()
            },
            HelpEntry("🎧 Audio & Equalizer") { p ->
                """
                `${p}volume <0-200>`
                Set the playback volume. The value is persisted per server.

                `${p}eq <preset>`
                Apply an equalizer preset. The preset is persisted per server and also selectable from the player's dropdown menu.

                **Available presets:**
                • `flat` — No effect (bypasses the equalizer, zero CPU cost)
                • `bass_boost` — Enhanced low frequencies
                • `treble_boost` — Enhanced high frequencies
                • `pop` — Optimized for pop/vocal music
                • `rock` — Boosted lows and highs for rock/metal
                • `jazz` — Warm, balanced tone for jazz
                • `classical` — Gentle low-end boost for orchestral music
                • `loudness` — V-shaped curve for maximum punch

                `${p}lyrics`
                Search for lyrics of the current track. Shows the best match and a dropdown menu to pick a different version (different artist, album, or synced/plain).
                """.trimIndent()
            },
            HelpEntry("⚙️ Server Settings & Prefixes") { p ->
                """
                **How prefixes work:**
                The bot listens for commands that start with a prefix. By default, it uses the global prefixes set via the `PREFIXES` environment variable (e.g. `!` and `.`).

                Each server can override the global prefixes with its own custom prefix. When a server prefix is set, only that prefix works in that server — the global ones are ignored.

                `${p}prefix <new prefix>`
                Set a custom prefix for this server. *Requires Manage Server or Bot Admin.*

                `${p}prefix clear`
                Remove the custom prefix and go back to the global defaults. *Requires Manage Server or Bot Admin.*

                `${p}settings`
                Show the current server settings (prefix, volume, equalizer). *Requires Manage Server or Bot Admin.*
                """.trimIndent()
            },
            HelpEntry("🛡️ Admin & Monitoring", adminOnly = true) { _ ->
                """
                These commands are restricted to **Bot Admins** (users listed in `ADMIN_IDS`).

                `!stats`
                Full health & statistics panel: uptime, gateway ping, servers, members, voice connections, players, queue, cumulative totals (tracks played, commands, errors), heap memory, CPU, threads, JVM and OS info.

                `!servers`
                List all servers the bot is connected to.

                `/stats`
                Same health panel as `!stats`, but as an ephemeral slash command (only you see the response).

                `/ping`
                Shows the bot's gateway and REST API latency.

                **Health endpoint:**
                The bot exposes `GET /health` on the configured port (default `8080`). Returns 200 + JSON when healthy, 503 when the Discord gateway is down. Used by Docker's `HEALTHCHECK` and external monitoring.
                """.trimIndent()
            },
        )

    private fun pagesFor(isAdmin: Boolean): List<HelpEntry> = if (isAdmin) allPages else allPages.filter { !it.adminOnly }

    fun resolvePrefix(guildId: Long?): String {
        if (guildId != null) {
            ServiceLocator.guildSettings.get(guildId).prefix?.let { return it }
        }
        return System.getenv("PREFIXES")
            ?.split(" ")
            ?.firstOrNull { it.isNotBlank() }
            ?: "!"
    }

    fun page(
        index: Int,
        prefix: String,
        isAdmin: Boolean,
    ): Pair<MessageEmbed, ActionRow> {
        val pages = pagesFor(isAdmin)
        val clamped = index.coerceIn(0, pages.lastIndex)
        val entry = pages[clamped]
        val total = pages.size
        val flag = if (isAdmin) "a" else "u"

        val embed =
            Embed.create(
                level = EmbedLevel.INFO,
                title = "${entry.title} — Help [${clamped + 1}/$total]",
                description = entry.body(prefix),
            ).build()

        val buttons =
            buildList {
                if (clamped > 0) add(Button.secondary("$BUTTON_PREFIX${clamped - 1}:$flag", leftEmoji))
                if (clamped < pages.lastIndex) add(Button.secondary("$BUTTON_PREFIX${clamped + 1}:$flag", rightEmoji))
                add(Button.secondary("button_delete", deleteEmoji))
            }

        return embed to ActionRow.of(buttons)
    }
}
