package pt.paulinoo.dbotkt.player.buttons

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import java.util.concurrent.TimeUnit

class SkipButton(private val audioManager: AudioManager) : CustomButton {
    override val customId: String = "skip_button"

    override suspend fun handle(event: ButtonInteractionEvent) {
        val guild = event.guild
        if (guild == null) {
            event.reply("This command can only be used in a guild.").setEphemeral(true).queue()
            return
        }
        audioManager.skip(event.channel, guild)
        event.deferEdit().queue()

        val embed =
            Embed.create(
                EmbedLevel.INFO,
                "Skipped to the next track.",
            ).build()

        event.channel.sendMessageEmbeds(embed).queue { message ->
            message.delete().queueAfter(10, TimeUnit.SECONDS)
        }
    }
}
