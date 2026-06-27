package pt.paulinoo.dbotkt.player.buttons

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import pt.paulinoo.dbotkt.player.lyrics.LyricsView

class LyricsButton(private val audioManager: AudioManager) : CustomButton {
    override val customId: String = "lyrics_button"

    override suspend fun handle(event: ButtonInteractionEvent) {
        val guild =
            event.guild ?: run {
                event.reply("This component can only be used in a guild.").setEphemeral(true).queue()
                return
            }

        // Lyrics require blocking HTTP; acknowledge with an ephemeral reply, then fetch off-thread.
        event.deferReply(true).queue()

        val search = withContext(Dispatchers.IO) { audioManager.searchLyrics(guild) }

        if (search.results.isEmpty()) {
            event.hook.sendMessageEmbeds(
                Embed.create(EmbedLevel.ERROR, "No lyrics found for the current track.").build(),
            ).queue()
            return
        }

        val best = search.best ?: search.results.first()

        event.hook.sendMessageEmbeds(LyricsView.lyricsEmbed(best))
            .setComponents(ActionRow.of(LyricsView.buildMenu(search.results, best.id)))
            .queue()
    }
}
