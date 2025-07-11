package pt.paulinoo.dbotkt.buttons

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import pt.paulinoo.dbotkt.audio.AudioManager

class PauseResumeButton (private val audioManager: AudioManager): Button {
    override val customId: String = "pause_button"

    override suspend fun handle(event: ButtonInteractionEvent) {
        audioManager.pause(event.channel, event.guild!!)
        event.reply("Playback paused.").setEphemeral(true).queue()
    }
}