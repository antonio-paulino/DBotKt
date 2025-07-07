package pt.paulinoo.dbotkt.command.player

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.audio.AudioCommandManager
import pt.paulinoo.dbotkt.command.Command

class ShuffleCommand(
    private val audioCommandManager: AudioCommandManager,
) : Command {
    override val name: String = "shuffle"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        val guild = event.guild
        audioCommandManager.shuffle(guild)
        event.channel.sendMessage("Queue shuffled.").queue()
    }
}
