package pt.paulinoo.dbotkt.commands.player

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.audio.AudioManager
import pt.paulinoo.dbotkt.commands.Command

class SkipCommand(
    private val audioCommandManager: AudioManager,
) : Command {
    override val name: String = "skip"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        val guild = event.guild
        val audioManager = guild.audioManager
        val textChannel = event.channel

        if (audioManager.isConnected) {
            audioCommandManager.skip(textChannel, guild)
            event.channel.sendMessage("Skipped to the next track.").queue()
        } else {
            event.channel.sendMessage("Not connected to a voice channel.").queue()
        }
    }
}
