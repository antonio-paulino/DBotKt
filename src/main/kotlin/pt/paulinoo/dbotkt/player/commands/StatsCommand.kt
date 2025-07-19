package pt.paulinoo.dbotkt.player.commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
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

        val usageMessage = audioCommandManager.getLavaPlayerStats()
        val embed =
            Embed.create(
                title = "LavaPlayer Statistics",
                description = usageMessage,
                level = EmbedLevel.INFO,
            ).build()

        event.channel.sendMessageEmbeds(embed).queue { message ->
            message.delete().queueAfter(20, TimeUnit.SECONDS)
        }
    }
}
