package pt.paulinoo.dbotkt.embed

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import pt.paulinoo.dbotkt.audio.AudioManager
import java.awt.Color

object PlayerEmbed {

    fun createPlayerEmbed(guild: Guild, audioManager: AudioManager): MessageEmbed {
        val player = audioManager.getGuildPlayer(guild)
        val track = player.playingTrack?.info ?: return EmbedBuilder()
            .setColor(Color.RED)
            .setTitle("Nothing is playing")
            .build()

        return EmbedBuilder()
            .setColor(Color.RED)
            .setTitle("Now Playing")
            .setDescription("[${track.title}](${track.uri})")
            .setThumbnail("https://img.youtube.com/vi/${extractYouTubeId(track.uri)}/hqdefault.jpg")
            .addField("Duration", formatTime(track.length), true)
            .addField("Volume", "${player.volume}%", true)
            .build()
    }

    fun createPlayerButtons(): Array<ActionRow> {
        return arrayOf(
            ActionRow.of(
                Button.secondary("pause_button", Emoji.fromUnicode("⏸")),
                Button.secondary("stop_button", Emoji.fromUnicode("⏹")),
                Button.secondary("skip_button", Emoji.fromUnicode("⏭")),
                Button.secondary("volume_up_button", Emoji.fromUnicode("🔊")),
                Button.secondary("volume_down_button", Emoji.fromUnicode("🔉"))
            ),
            ActionRow.of(
                Button.secondary("shuffle_button", Emoji.fromUnicode("🔀")),
                Button.secondary("loop_button", Emoji.fromUnicode("🔁")),
                Button.secondary("queue_button", Emoji.fromUnicode("📋")),
                Button.secondary("repeating_button", Emoji.fromUnicode("⏺")),
                Button.secondary("disconnect_button", Emoji.fromUnicode("🔌"))
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