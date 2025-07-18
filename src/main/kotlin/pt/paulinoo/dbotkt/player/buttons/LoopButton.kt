package pt.paulinoo.dbotkt.player.buttons

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import java.util.concurrent.TimeUnit

class LoopButton(private val audioManager: AudioManager) : CustomButton {
    override val customId: String = "loop_button"

    override suspend fun handle(event: ButtonInteractionEvent) {
        val guild =
            event.guild ?: run {
                event.reply("This command can only be used in a guild.").setEphemeral(true).queue()
                return
            }
        audioManager.toggleLoop(event.channel, guild)
        event.deferEdit().queue()

        val player = audioManager.getGuildPlayer(guild)
        if (player == null) {
            val embed =
                Embed.create(
                    EmbedLevel.ERROR,
                    "No player found for this guild.",
                ).build()
            event.channel.sendMessageEmbeds(embed).queue { message ->
                message.delete().queueAfter(10, TimeUnit.SECONDS)
            }
            return
        }
        val embed =
            Embed.create(
                EmbedLevel.INFO,
                if (player.isLooping) "Looping enabled." else "Looping disabled.",
            ).build()

        event.channel.sendMessageEmbeds(embed).queue { message ->
            message.delete().queueAfter(10, TimeUnit.SECONDS)
        }
    }
}
