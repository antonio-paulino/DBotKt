package pt.paulinoo.dbotkt.commands.help

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class HelpPaginationListener : ListenerAdapter() {
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val id = event.componentId
        if (!id.startsWith(HelpPages.BUTTON_PREFIX)) return

        // Format: help_page:<index>:<a|u>
        val parts = id.removePrefix(HelpPages.BUTTON_PREFIX).split(":")
        val pageIndex = parts.getOrNull(0)?.toIntOrNull() ?: return
        val isAdmin = parts.getOrNull(1) == "a"

        val prefix = HelpPages.resolvePrefix(event.guild?.idLong)
        val (embed, buttons) = HelpPages.page(pageIndex, prefix, isAdmin)

        event.editMessageEmbeds(embed)
            .setComponents(buttons)
            .queue()
    }
}
