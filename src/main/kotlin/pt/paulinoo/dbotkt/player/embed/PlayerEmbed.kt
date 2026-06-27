package pt.paulinoo.dbotkt.player.embed

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.selections.StringSelectMenu
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import pt.paulinoo.dbotkt.player.audio.EqualizerPreset
import pt.paulinoo.dbotkt.player.audio.TrackMetadata
import java.awt.Color

object PlayerEmbed {
    fun createPlayerEmbed(
        guild: Guild,
        audioManager: AudioManager,
    ): MessageEmbed {
        val player =
            audioManager.getGuildPlayer(guild) ?: return Embed.create(
                EmbedLevel.ERROR,
                "No audio player found for this guild. Please join a voice channel and play something first.",
            ).build()

        val track =
            player.player.playingTrack ?: return EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("Nothing is playing")
                .build()

        val trackInfo = track.info
        val metadata = track.userData as? TrackMetadata

        val embedBuilder =
            EmbedBuilder()
                .setColor(Color.RED)
                .setTitle(if (audioManager.isPaused(guild)) "Paused" else "Now Playing")
                .setDescription("[${trackInfo.title} - ${track.info.author}](${trackInfo.uri})")
                .setThumbnail(resolveThumbnail(trackInfo.uri, trackInfo.artworkUrl))
                .addField("Duration", formatTime(trackInfo.length), true)
                .addField("Volume", "${player.player.volume}%", true)

        metadata?.let {
            embedBuilder.addField("Requested by", "<@${it.requesterId}>", true)
        }

        embedBuilder.addField(
            "Loop Mode",
            when (player.loopMode) {
                pt.paulinoo.dbotkt.player.audio.LoopMode.NONE -> "None"
                pt.paulinoo.dbotkt.player.audio.LoopMode.SINGLE -> "Single"
                pt.paulinoo.dbotkt.player.audio.LoopMode.QUEUE -> "Queue"
            },
            true,
        )

        embedBuilder.addField("Equalizer", player.equalizerPreset.displayName, true)

        if (player.queue.isNotEmpty()) {
            val totalQueueDuration = player.queue.sumOf { it.duration }
            embedBuilder
                .addField(
                    "Queue",
                    "${player.queue.size} tracks",
                    true,
                )
                .addField(
                    "Duration",
                    formatTime(totalQueueDuration),
                    true,
                )
                .addField(
                    "Next Track",
                    player.queue.first().info.title + " - " + player.queue.first().info.author,
                    false,
                )
        }
        return embedBuilder.build()
    }

    fun createPlayerComponents(
        guild: Guild,
        audioManager: AudioManager,
    ): Array<ActionRow> {
        val currentPreset = audioManager.getGuildPlayer(guild)?.equalizerPreset ?: EqualizerPreset.FLAT

        val equalizerMenu =
            StringSelectMenu.create("equalizer_select")
                .setPlaceholder("Equalizer: ${currentPreset.displayName}")
                .apply {
                    EqualizerPreset.entries.forEach { preset ->
                        addOption(preset.displayName, preset.id, Emoji.fromUnicode(preset.emoji))
                    }
                    setDefaultValues(currentPreset.id)
                }
                .build()

        return arrayOf(
            ActionRow.of(
                Button.secondary("pause_button", Emoji.fromUnicode("U+23EF")),
                Button.secondary("stop_button", Emoji.fromUnicode("U+23F9")),
                Button.secondary("skip_button", Emoji.fromUnicode("U+23ED")),
                Button.secondary("queue_button", Emoji.fromUnicode("U+1F4CB")),
                Button.secondary("lyrics_button", Emoji.fromUnicode("U+1F3A4")),
            ),
            ActionRow.of(
                Button.secondary("volume_up_button", Emoji.fromUnicode("U+1F50A")),
                Button.secondary("volume_down_button", Emoji.fromUnicode("U+1F509")),
                Button.secondary("shuffle_button", Emoji.fromUnicode("U+1F500")),
                Button.secondary("loop_button", Emoji.fromUnicode("U+1F501")),
                Button.secondary("clear_queue_button", Emoji.fromUnicode("U+1F5D1")),
            ),
            ActionRow.of(equalizerMenu),
        )
    }

    fun showQueuePaginated(
        channel: MessageChannel,
        guild: Guild,
        page: Int,
        audioManager: AudioManager,
    ): MessageEmbed {
        val player =
            audioManager.getGuildPlayer(guild) ?: return Embed.create(
                EmbedLevel.ERROR,
                "No audio player found for this guild. Please join a voice channel and play something first.",
            ).build()
        val queue = player.queue
        val tracksPerPage = 10
        val totalPages = (queue.size + tracksPerPage - 1) / tracksPerPage
        val currentPage = page.coerceIn(1, totalPages.coerceAtLeast(1))
        val start = (currentPage - 1) * tracksPerPage
        val end = (start + tracksPerPage).coerceAtMost(queue.size)

        val embed =
            EmbedBuilder()
                .setTitle("Queue [Page $currentPage/$totalPages]")
                .apply {
                    if (queue.isEmpty()) {
                        setDescription("The queue is empty.")
                    } else {
                        for ((i, track) in queue.slice(start until end).withIndex()) {
                            addField(
                                "${start + i + 1}. ${track.info.title}",
                                "by ${track.info.author} [${formatTime(track.duration)}]",
                                false,
                            )
                        }
                    }
                }
                .build()
        return embed
    }

    private fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        val minutes = totalSec / 60
        val seconds = totalSec % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    // Matches watch?v=ID, youtu.be/ID and shorts/embed/v/live/ID forms.
    private val youtubeIdRegexes =
        listOf(
            Regex("""[?&]v=([\w-]{11})"""),
            Regex("""youtu\.be/([\w-]{11})"""),
            Regex("""youtube\.com/(?:shorts|embed|v|live)/([\w-]{11})"""),
        )

    /** Extracts the 11-character YouTube video id from any common YouTube URL form. */
    internal fun extractYouTubeId(uri: String?): String? {
        if (uri.isNullOrBlank()) return null
        for (regex in youtubeIdRegexes) {
            regex.find(uri)?.let { return it.groupValues[1] }
        }
        return null
    }

    /**
     * Resolves a thumbnail for the track. YouTube tracks use `hqdefault.jpg` (always present),
     * avoiding the `maxresdefault` URLs in [artworkUrl] that 404 for many videos; everything
     * else (Spotify, SoundCloud, …) falls back to the source-provided [artworkUrl].
     */
    internal fun resolveThumbnail(
        uri: String?,
        artworkUrl: String?,
    ): String? {
        extractYouTubeId(uri)?.let { return "https://img.youtube.com/vi/$it/hqdefault.jpg" }
        return artworkUrl?.takeIf { it.isNotBlank() }
    }
}
