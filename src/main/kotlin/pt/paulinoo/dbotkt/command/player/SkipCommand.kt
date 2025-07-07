package pt.paulinoo.dbotkt.command.player

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.audio.AudioCommandManager
import pt.paulinoo.dbotkt.command.Command

class SkipCommand (
    private val audioCommandManager: AudioCommandManager
): Command {
    override val name: String = "skip"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>
    ) {
        val guild = event.guild
        val audioManager = guild.audioManager

        if (audioManager.isConnected) {
            audioCommandManager.skip(guild)
            event.channel.sendMessage("Skipped to the next track.").queue()
        } else {
            event.channel.sendMessage("Not connected to a voice channel.").queue()
        }
    }
}