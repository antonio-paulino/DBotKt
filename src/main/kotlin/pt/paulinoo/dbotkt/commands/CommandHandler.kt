package pt.paulinoo.dbotkt.commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.LoggerFactory
import pt.paulinoo.dbotkt.config.GuildSettingsStore
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.metrics.BotMetrics
import java.util.concurrent.TimeUnit

class CommandHandler(
    private val commands: List<Command>,
    private val settings: GuildSettingsStore,
    private val cooldowns: CooldownManager,
) {
    private val commandMap = commands.associateBy { it.name }
    private val logger = LoggerFactory.getLogger(CommandHandler::class.java)

    private val globalPrefixes: List<String> =
        System.getenv("PREFIXES")
            ?.split(" ")
            ?.filter { it.isNotBlank() }
            ?: emptyList()

    suspend fun handle(event: MessageReceivedEvent) {
        val content = event.message.contentRaw
        val guildSettings = settings.get(event.guild.idLong)

        // A guild-specific prefix overrides the global list when set.
        val prefixes = guildSettings.prefix?.let { listOf(it) } ?: globalPrefixes
        val prefix = prefixes.firstOrNull { content.startsWith(it) } ?: return

        val parts = content.substring(prefix.length).trim().split("\\s+".toRegex())
        if (parts.isEmpty() || parts[0].isBlank()) return

        val name = parts[0]
        val args = if (parts.size > 1) parts.subList(1, parts.size) else emptyList()

        val command = commandMap[name] ?: return

        val remaining = cooldowns.check(event.author.id, command.name)
        if (remaining > 0) {
            sendTransient(event, EmbedLevel.WARNING, "Slow down — try again in ${(remaining + 999) / 1000}s.")
            return
        }

        BotMetrics.commandExecuted()
        logger.info("Command executed: ${command.name} by ${event.author.name} in ${event.guild.name}")

        try {
            command.execute(event, args)
        } catch (e: Exception) {
            BotMetrics.errorOccurred()
            logger.error("Command '${command.name}' failed", e)
        }
    }

    private fun sendTransient(
        event: MessageReceivedEvent,
        level: EmbedLevel,
        message: String,
    ) {
        event.channel.sendMessageEmbeds(Embed.create(level, message).build()).queue { sent ->
            sent.delete().queueAfter(8, TimeUnit.SECONDS)
        }
    }
}
