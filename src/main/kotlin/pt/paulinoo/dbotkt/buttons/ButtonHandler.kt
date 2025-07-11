package pt.paulinoo.dbotkt.buttons

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class ButtonHandler(private val buttons: List<Button>) {
    private val handlers = buttons.associateBy { it.customId }

    suspend fun handle(event: ButtonInteractionEvent) {
        val handler = handlers[event.componentId]
        if (handler != null) {
            handler.handle(event)
        } else {
            event.reply("Unknown button.").setEphemeral(true).queue()
        }
    }
}
