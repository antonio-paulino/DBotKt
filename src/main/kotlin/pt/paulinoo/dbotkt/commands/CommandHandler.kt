package pt.paulinoo.dbotkt.commands

import io.github.cdimascio.dotenv.dotenv
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.LoggerFactory
import java.time.LocalTime

class CommandHandler(private val commands: List<Command>) {
    private val commandMap = commands.associateBy { it.name }
    private val dotenv = dotenv()

    private val logger = LoggerFactory.getLogger(CommandHandler::class.java)

    suspend fun handle(event: MessageReceivedEvent) {
        val content = event.message.contentRaw
        if (dotenv["PREFIXES"].split(" ").none { content.startsWith(it) }) return

        val parts = content.substring(1).split("\\s+".toRegex())
        if (parts.isEmpty() || parts[0].isBlank()) return

        val name = parts[0]
        val args = if (parts.size > 1) parts.subList(1, parts.size) else emptyList()

        val command = commandMap[name] ?: return

        logger.info("[${LocalTime.now()}] Command executed: ${command.name} by ${event.author.name} in ${event.guild.name}")
        command.execute(event, args)
    }
}
