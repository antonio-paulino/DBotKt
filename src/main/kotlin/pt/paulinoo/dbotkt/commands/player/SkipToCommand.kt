package pt.paulinoo.dbotkt.commands.player

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.audio.AudioManager
import pt.paulinoo.dbotkt.commands.Command

class SkipToCommand(
    private val audioCommandManager: AudioManager,
) : Command {
    override val name: String = "skipto"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        if (args.isEmpty()) {
            event.channel.sendMessage("Please provide a valid track number to skip to.").queue()
            return
        }

        val trackNumber = args.firstOrNull()?.toIntOrNull()
        if (trackNumber == null || trackNumber < 1) {
            event.channel.sendMessage("Invalid track number provided.").queue()
            return
        }

        val guild = event.guild
        val textChannel = event.channel
        audioCommandManager.skipTo(textChannel, guild, trackNumber - 1) // Convert to zero-based index
        event.channel.sendMessage("Skipped to track number $trackNumber.").queue()
    }
}
