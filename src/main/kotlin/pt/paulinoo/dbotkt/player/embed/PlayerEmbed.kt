package pt.paulinoo.dbotkt.player.embed

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import pt.paulinoo.dbotkt.player.audio.AudioManager
import pt.paulinoo.dbotkt.player.audio.TrackMetadata
import java.awt.Color

object PlayerEmbed {
    fun createPlayerEmbed(
        guild: Guild,
        audioManager: AudioManager,
    ): MessageEmbed {
        val player = audioManager.getGuildPlayer(guild)
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
                .setDescription("[${trackInfo.title}](${trackInfo.uri})")
                .setThumbnail("https://img.youtube.com/vi/${extractYouTubeId(trackInfo.uri)}/hqdefault.jpg")
                .addField("Duration", formatTime(trackInfo.length), true)
                .addField("Volume", "${player.player.volume}%", true)

        metadata?.let {
            embedBuilder.addField("Requested by", "<@${it.requesterId}>", true)
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
