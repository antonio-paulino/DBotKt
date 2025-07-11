package pt.paulinoo.dbotkt.commands.player

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.audio.AudioManager
import pt.paulinoo.dbotkt.commands.Command

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
        event.channel.sendMessage("Queue shuffled.").queue()
    }
}
