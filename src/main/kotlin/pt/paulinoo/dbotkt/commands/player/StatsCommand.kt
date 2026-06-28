package pt.paulinoo.dbotkt.commands.player

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.player.audio.AudioManager
import pt.paulinoo.dbotkt.stats.StatsService
import java.util.concurrent.TimeUnit

class StatsCommand(
    private val audioCommandManager: AudioManager,
) : Command {
    override val name: String = "stats"

    private val admins: Set<String> =
        System.getenv("ADMIN_IDS")
            ?.split(",")
            ?.map { it.trim() }
            ?.toSet()
            ?: emptySet()

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        if (event.author.id !in admins) return

        val snapshot = StatsService.gather(event.jda, audioCommandManager)
        val embed = StatsService.embed(snapshot, event.author.name)

        event.channel.sendMessageEmbeds(embed).queue { message ->
            message.delete().queueAfter(60, TimeUnit.SECONDS)
        }
    }
}
