package pt.paulinoo.dbotkt.commands.player

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import pt.paulinoo.dbotkt.player.lyrics.LyricsView
import java.util.concurrent.TimeUnit

class LyricsCommand(
    private val audioCommandManager: AudioManager,
) : Command {
    override val name: String = "lyrics"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        val guild = event.guild

        if (audioCommandManager.getGuildPlayer(guild)?.player?.playingTrack == null) {
            sendTransient(event, Embed.create(EmbedLevel.ERROR, "Nothing is playing right now.").build())
            return
        }

        val search = withContext(Dispatchers.IO) { audioCommandManager.searchLyrics(guild) }

        if (search.results.isEmpty()) {
            sendTransient(event, Embed.create(EmbedLevel.ERROR, "No lyrics found for the current track.").build())
            return
        }

        val best = search.best ?: search.results.first()

        event.channel.sendMessageEmbeds(LyricsView.lyricsEmbed(best))
            .setComponents(ActionRow.of(LyricsView.buildMenu(search.results, best.id)))
            .queue()
    }

    private fun sendTransient(
        event: MessageReceivedEvent,
        embed: net.dv8tion.jda.api.entities.MessageEmbed,
    ) {
        event.channel.sendMessageEmbeds(embed).queue { message ->
            message.delete().queueAfter(10, TimeUnit.SECONDS)
        }
    }
}
