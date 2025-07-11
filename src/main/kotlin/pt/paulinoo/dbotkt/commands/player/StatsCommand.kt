package pt.paulinoo.dbotkt.commands.player

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.audio.AudioManager
import pt.paulinoo.dbotkt.commands.Command

class StatsCommand(
    private val audioCommandManager: AudioManager,
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
