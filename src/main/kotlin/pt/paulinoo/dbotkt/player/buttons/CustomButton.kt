package pt.paulinoo.dbotkt.player.buttons

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

interface CustomButton {
    val customId: String

    suspend fun handle(event: ButtonInteractionEvent)
}
