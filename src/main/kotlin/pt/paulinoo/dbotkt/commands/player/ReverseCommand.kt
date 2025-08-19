package pt.paulinoo.dbotkt.commands.player

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import java.util.concurrent.TimeUnit

class ReverseCommand(
    private val audioCommandManager: AudioManager,
) : Command {
    override val name: String = "reverse"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        val guild = event.guild
        val textChannel = event.channel
        audioCommandManager.reverse(textChannel, guild)
        val embed =
            Embed.create(
                EmbedLevel.INFO,
                "Queue reversed.",
            ).build()
        event.channel.sendMessageEmbeds(embed).queue { message ->
            message.delete().queueAfter(10, TimeUnit.SECONDS)
        }
    }
}
