package pt.paulinoo.dbotkt.buttons

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

interface Button {
    val customId: String
    suspend fun handle(event: ButtonInteractionEvent)
}