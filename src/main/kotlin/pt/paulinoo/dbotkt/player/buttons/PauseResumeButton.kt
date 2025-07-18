package pt.paulinoo.dbotkt.player.buttons

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import java.util.concurrent.TimeUnit

class PauseResumeButton(private val audioManager: AudioManager) : CustomButton {
    override val customId: String = "pause_button"

    override suspend fun handle(event: ButtonInteractionEvent) {
        val guild = event.guild
        if (guild == null) {
            val embed =
                Embed.create(
                    EmbedLevel.ERROR,
                    "This command can only be used in a guild.",
                ).build()

            event.channel.sendMessageEmbeds(embed).queue { message ->
                message.delete().queueAfter(5, TimeUnit.SECONDS)
            }
            return
        }

        audioManager.togglePause(event.channel, guild)

        event.deferEdit().queue()

        val embed =
            Embed.create(
                EmbedLevel.INFO,
                if (audioManager.isPaused(guild)) "Playback paused." else "Playback resumed.",
            ).build()

        event.channel.sendMessageEmbeds(embed).queue { message ->
            message.delete().queueAfter(10, TimeUnit.SECONDS)
        }
    }
}
