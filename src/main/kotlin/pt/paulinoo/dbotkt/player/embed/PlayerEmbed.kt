package pt.paulinoo.dbotkt.player.embed

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
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
        val isSpotify = trackInfo.uri.contains("spotify")

        val embedBuilder =
            EmbedBuilder()
                .setColor(Color.RED)
                .setTitle(if (audioManager.isPaused(guild)) "Paused" else "Now Playing")
                .setDescription("[${trackInfo.title} - ${track.info.author}](${trackInfo.uri})")
                .setThumbnail(
                    if (isSpotify) {
                        "https://i.scdn.co/image/${trackInfo.artworkUrl.replace("https://i.scdn.co/image/", "")}"
                    } else {
                        "https://img.youtube.com/vi/${extractYouTubeId(trackInfo.uri)}/hqdefault.jpg"
                    },
                )
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
            false,
        )

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

    fun createPlayerButtons(): Array<ActionRow> {
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

    private fun extractYouTubeId(uri: String): String {
        return uri.substringAfter("v=").substringBefore("&")
    }

    private fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        val minutes = totalSec / 60
        val seconds = totalSec % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
