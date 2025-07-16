package pt.paulinoo.dbotkt.player.buttons

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import java.util.concurrent.TimeUnit

class ButtonHandler(private val buttons: List<Button>) {
    private val handlers = buttons.associateBy { it.customId }

    suspend fun handle(event: ButtonInteractionEvent) {
        val handler = handlers[event.componentId]
        if (handler != null) {
            handler.handle(event)
        } else {
            event.deferEdit().queue()

            val embed =
                Embed.create(
                    EmbedLevel.ERROR,
                    "Unknown button interaction.",
                ).build()

            event.channel.sendMessageEmbeds(embed).queue { message ->
                message.delete().queueAfter(5, TimeUnit.SECONDS)
            }
        }
    }
}
