package pt.paulinoo.dbotkt.command

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.audio.AudioCommandManager

class StopCommand(
    private val audioCommandManager: AudioCommandManager,
) : Command {
    override val name: String = "stop"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        val guild = event.guild
        audioCommandManager.stop(guild)
        event.channel.sendMessage("Playback stopped.").queue()
    }
}
