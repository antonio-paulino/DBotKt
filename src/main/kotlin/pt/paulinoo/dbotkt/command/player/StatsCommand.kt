package pt.paulinoo.dbotkt.command.player

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.audio.AudioCommandManager
import pt.paulinoo.dbotkt.command.Command

class StatsCommand(
    private val audioCommandManager: AudioCommandManager,
) : Command {
    override val name: String = "stats"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        val usageMessage = audioCommandManager.getLavaPlayerStats()
        event.channel.sendMessage(usageMessage).queue()
    }
}
