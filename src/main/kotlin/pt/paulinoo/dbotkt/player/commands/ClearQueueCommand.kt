package pt.paulinoo.dbotkt.player.commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.player.audio.AudioManager

class ClearQueueCommand(
    private val audioManager: AudioManager,
) : Command {
    override val name: String = "clearqueue"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        TODO("Implement clear queue command")
    }
}
