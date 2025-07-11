package pt.paulinoo.dbotkt.commands.player

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.audio.AudioManager
import pt.paulinoo.dbotkt.commands.Command

class ResumeCommand(
    private val audioCommandManager: AudioManager,
) : Command {
    override val name: String = "resume"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        val guild = event.guild
        val audioManager = guild.audioManager
        val textChannel = event.channel

        if (audioManager.isConnected) {
            audioCommandManager.resume(textChannel, guild)
            event.channel.sendMessage("Playback resumed.").queue()
        } else {
            event.channel.sendMessage("Not connected to a voice channel.").queue()
        }
    }
}
