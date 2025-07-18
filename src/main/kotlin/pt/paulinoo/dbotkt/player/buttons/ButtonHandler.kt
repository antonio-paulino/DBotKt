package pt.paulinoo.dbotkt.player.buttons

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class ButtonHandler(private val buttons: List<CustomButton>) {
    private val handlers = buttons.associateBy { it.customId }

    suspend fun handle(event: ButtonInteractionEvent) {
        val handler = handlers[event.componentId]
        if (handler != null) {
            handler.handle(event)
        } else {
            return
        }
    }
}
