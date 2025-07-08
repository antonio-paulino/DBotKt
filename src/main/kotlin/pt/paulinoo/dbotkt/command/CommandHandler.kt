package pt.paulinoo.dbotkt.command

import io.github.cdimascio.dotenv.dotenv
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class CommandHandler(private val commands: List<Command>) {
    private val commandMap = commands.associateBy { it.name }
    private val dotenv = dotenv()

    suspend fun handle(event: MessageReceivedEvent) {
        val content = event.message.contentRaw
        if (dotenv["PREFIXES"].split(" ").none { content.startsWith(it) }) return

        val parts = content.substring(1).split("\\s+".toRegex())
        if (parts.isEmpty() || parts[0].isBlank()) return

        val name = parts[0]
        val args = if (parts.size > 1) parts.subList(1, parts.size) else emptyList()

        val command = commandMap[name] ?: return
        command.execute(event, args)
    }
}
