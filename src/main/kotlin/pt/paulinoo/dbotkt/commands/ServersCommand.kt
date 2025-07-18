package pt.paulinoo.dbotkt.commands

import io.github.cdimascio.dotenv.dotenv
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import java.util.concurrent.TimeUnit
import kotlin.text.split

class ServersCommand : Command {
    override val name: String = "servers"

    private val admins: Set<String> =
        dotenv()["ADMIN_IDS"]
            ?.split(",")
            ?.map { it.trim() }
            ?.toSet()
            ?: emptySet()

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        val authorId = event.author.id
        if (authorId !in admins) return

        val guilds = event.jda.guilds
        val serverList = guilds.joinToString("\n") { it.name }
        val embed =
            Embed.create(
                title = "Connected Servers",
                description = "Here is the list of servers I am connected to:\n$serverList",
                level = EmbedLevel.INFO,
            )
        event.channel.sendMessageEmbeds(embed.build()).queue { message ->
            message.delete().queueAfter(20, TimeUnit.SECONDS)
        }
    }
}
