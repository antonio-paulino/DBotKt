package pt.paulinoo.dbotkt.player.buttons

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import java.util.concurrent.TimeUnit

class VolumeUpButton(private val audioManager: AudioManager) : Button {
    override val customId: String = "volume_up_button"

    override suspend fun handle(event: ButtonInteractionEvent) {
        val guild =
            event.guild ?: run {
                event.reply("This command can only be used in a guild.").setEphemeral(true).queue()
                return
            }
        val textChannel = event.channel
        val player = audioManager.getGuildPlayer(guild)

        if (player.player.volume < 200) {
            val newVolume = (player.player.volume + 10).coerceAtMost(200)
            audioManager.setVolume(textChannel, guild, newVolume)
        }

        event.deferEdit().queue()

        val embed =
            Embed.create(
                EmbedLevel.INFO,
                "Volume increased.",
            ).build()

        event.channel.sendMessageEmbeds(embed).queue { message ->
            message.delete().queueAfter(10, TimeUnit.SECONDS)
        }
    }
}
