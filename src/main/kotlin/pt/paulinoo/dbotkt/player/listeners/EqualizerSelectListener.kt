package pt.paulinoo.dbotkt.player.listeners

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import pt.paulinoo.dbotkt.player.audio.AudioManager
import pt.paulinoo.dbotkt.player.audio.EqualizerPreset

class EqualizerSelectListener(
    private val audioManager: AudioManager,
) : ListenerAdapter() {
    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        if (event.componentId != "equalizer_select") return

        val guild =
            event.guild ?: run {
                event.reply("This component can only be used in a guild.").setEphemeral(true).queue()
                return
            }

        val preset = event.values.firstOrNull()?.let { EqualizerPreset.fromId(it) } ?: return

        event.deferEdit().queue()
        audioManager.setEqualizer(event.channel, guild, preset)
    }
}
