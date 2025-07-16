package pt.paulinoo.dbotkt.player.buttons

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import java.util.concurrent.TimeUnit

class VolumeDownButton(private val audioManager: AudioManager) : Button {
    override val customId: String = "volume_down_button"

    override suspend fun handle(event: ButtonInteractionEvent) {
        val guild = event.guild
        if (guild == null) {
            event.reply("This command can only be used in a guild.").setEphemeral(true).queue()
            return
        }
        val textChannel = event.channel
        val player = audioManager.getGuildPlayer(guild)

        if (player.player.volume > 0) {
            val newVolume = (player.player.volume - 10).coerceAtLeast(0)
            audioManager.setVolume(textChannel, guild, newVolume)
        }

        event.deferEdit().queue()

        val embed =
            Embed.create(
                EmbedLevel.INFO,
                "Volume decreased.",
            ).build()

        event.channel.sendMessageEmbeds(embed).queue { message ->
            message.delete().queueAfter(10, TimeUnit.SECONDS)
        }
    }
}
