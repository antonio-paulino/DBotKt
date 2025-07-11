package pt.paulinoo.dbotkt.commands.player

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.audio.AudioManager
import pt.paulinoo.dbotkt.commands.Command

class VolumeCommand(
    private val audioCommandManager: AudioManager,
) : Command {
    override val name: String = "volume"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        if (args.isEmpty()) {
            event.channel.sendMessage("Please provide a volume level (0-100).").queue()
            return
        }

        val volume = args[0].toIntOrNull()
        if (volume == null || volume < 0 || volume > 200) {
            event.channel.sendMessage("Invalid volume level. Please provide a number between 0 and 200.").queue()
            return
        }
        val textChannel = event.channel

        audioCommandManager.setVolume(textChannel, event.guild, volume)
        event.channel.sendMessage("Volume set to $volume.").queue()
    }
}
