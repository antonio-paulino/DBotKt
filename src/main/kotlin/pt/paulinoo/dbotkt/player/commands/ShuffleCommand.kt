package pt.paulinoo.dbotkt.player.commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import java.util.concurrent.TimeUnit

class ShuffleCommand(
    private val audioCommandManager: AudioManager,
) : Command {
    override val name: String = "shuffle"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        val guild = event.guild
        val textChannel = event.channel
        audioCommandManager.shuffle(textChannel, guild)
        val embed =
            Embed.create(
                EmbedLevel.INFO,
                "Queue shuffled.",
            ).build()
        event.channel.sendMessageEmbeds(embed).queue { message ->
            message.delete().queueAfter(10, TimeUnit.SECONDS)
        }
    }
}
