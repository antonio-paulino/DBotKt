package pt.paulinoo.dbotkt.command.player

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.audio.AudioCommandManager
import pt.paulinoo.dbotkt.command.Command

class RemoveCommand(
    private val audioCommandManager: AudioCommandManager
): Command{
    override val name: String = "remove"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>
    ) {
        if (args.isEmpty()) {
            event.channel.sendMessage("Please provide a song index to remove.").queue()
            return
        }

        val index = args[0].toIntOrNull()
        if (index == null || index < 1) {
            event.channel.sendMessage("Invalid song index. Please provide a valid number.").queue()
            return
        }

        audioCommandManager.remove(event.guild, index - 1)
        event.channel.sendMessage("Song at index $index has been removed from the queue.").queue()
    }
}