package pt.paulinoo.dbotkt.player.commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.player.audio.AudioManager

class LyricsCommand(
    private val audioCommandManager: AudioManager,
) : Command {
    override val name: String = "lyrics"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        TODO("Implement lyrics command functionality")
    }
}
