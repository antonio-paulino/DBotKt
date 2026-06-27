package pt.paulinoo.dbotkt.player.listeners

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import pt.paulinoo.dbotkt.player.lyrics.LyricsView

class LyricsSelectListener(
    private val audioManager: AudioManager,
) : ListenerAdapter() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        if (event.componentId != LyricsView.SELECT_ID) return

        val id = event.values.firstOrNull()?.toLongOrNull() ?: return

        // Acknowledge immediately; fetching the chosen version is a blocking HTTP call.
        event.deferEdit().queue()

        scope.launch {
            val result = audioManager.getLyricsResult(id)
            val embed =
                if (result == null) {
                    Embed.create(EmbedLevel.ERROR, "Could not load that lyrics version.").build()
                } else {
                    LyricsView.lyricsEmbed(result)
                }
            // Only the embed is replaced; the select menu stays so the user can switch versions.
            event.hook.editOriginalEmbeds(embed).queue()
        }
    }
}
