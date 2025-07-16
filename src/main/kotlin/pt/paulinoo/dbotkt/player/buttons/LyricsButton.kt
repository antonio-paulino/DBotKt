package pt.paulinoo.dbotkt.player.buttons

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import pt.paulinoo.dbotkt.player.audio.AudioManager

class LyricsButton (private val audioManager: AudioManager) : Button {
    override val customId: String = "lyrics_button"

    override suspend fun handle(event: ButtonInteractionEvent) {
        TODO("Implement lyrics button functionality")
    }
}