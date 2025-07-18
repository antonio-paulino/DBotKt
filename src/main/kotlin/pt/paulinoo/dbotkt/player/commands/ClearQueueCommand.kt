package pt.paulinoo.dbotkt.player.commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.player.audio.AudioManager

class ClearQueueCommand(
    private val audioCommandManager: AudioManager,
) : Command {
    override val name: String = "clearqueue"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        val guild = event.guild
        val audioPlayer = audioCommandManager.getGuildPlayer(guild)

        if (audioPlayer != null) {
            audioPlayer.clearQueue()
            event.channel.sendMessage("Queue cleared.").queue()
        } else {
            event.channel.sendMessage("Not connected to a voice channel.").queue()
        }
    }
}
