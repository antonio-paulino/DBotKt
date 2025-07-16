package pt.paulinoo.dbotkt.player.buttons

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import pt.paulinoo.dbotkt.player.audio.AudioManager

class QueueButton(private val audioManager: AudioManager) : Button {
    override val customId: String = "queue_button"

    override suspend fun handle(event: ButtonInteractionEvent) {
        TODO("Implement queue button functionality")
    }
}