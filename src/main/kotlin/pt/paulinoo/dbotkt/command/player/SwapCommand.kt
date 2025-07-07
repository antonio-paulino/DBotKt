package pt.paulinoo.dbotkt.command.player

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.audio.AudioCommandManager
import pt.paulinoo.dbotkt.command.Command

class SwapCommand(
    private val audioCommandManager: AudioCommandManager
): Command{
    override val name: String = "swap"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>
    ) {
        val guild = event.guild
        val audioManager = guild.audioManager

        if (audioManager.isConnected) {
            if (args.size >= 2) {
                val first = args[0].toIntOrNull()
                val second = args[1].toIntOrNull()
                if (first != null && second != null) {
                    audioCommandManager.swap(guild, first, second)
                    event.channel.sendMessage("Swapped track $first with track $second.").queue()
                } else {
                    event.channel.sendMessage("Invalid arguments. Please provide two valid track numbers.").queue()
                }
            } else {
                event.channel.sendMessage("Please provide two track numbers to swap.").queue()
            }
        }
    }
}