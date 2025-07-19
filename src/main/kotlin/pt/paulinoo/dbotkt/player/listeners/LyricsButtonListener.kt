package pt.paulinoo.dbotkt.player.listeners

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import pt.paulinoo.dbotkt.player.audio.AudioManager

class LyricsButtonListener(
    private val audioManager: AudioManager,
) : ListenerAdapter() {
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val id = event.componentId

        if (id == "lyrics_button_delete") {
            val guild = event.guild
            if (guild == null) {
                event.reply("This command can only be used in a guild.").setEphemeral(true).queue()
                return
            }
            event.deferEdit().queue { event.message.delete().queue() }
        }
    }
}
