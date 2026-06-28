package pt.paulinoo.dbotkt.commands.settings

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.config.GuildSettingsStore
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import java.util.concurrent.TimeUnit

class PrefixCommand(
    private val settings: GuildSettingsStore,
) : Command {
    override val name: String = "prefix"

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
        val member = event.member ?: return
        if (!member.hasPermission(Permission.MANAGE_SERVER) && event.author.id !in admins) {
            reply(event, EmbedLevel.ERROR, "You need **Manage Server** or be a bot admin to change the prefix.")
            return
        }

        val arg = args.firstOrNull()
        if (arg == null) {
            val current = settings.get(event.guild.idLong).prefix
            reply(
                event,
                EmbedLevel.INFO,
                "Current prefix: ${current?.let { "`$it`" } ?: "global default"}.\n" +
                    "Usage: `prefix <new prefix>` or `prefix clear`.",
            )
            return
        }

        if (arg.lowercase() in setOf("clear", "reset", "none")) {
            settings.update(event.guild.idLong) { it.copy(prefix = null) }
            reply(event, EmbedLevel.SUCCESS, "Prefix reset to the global default.")
        } else {
            settings.update(event.guild.idLong) { it.copy(prefix = arg) }
            reply(event, EmbedLevel.SUCCESS, "Prefix set to `$arg`.")
        }
    }

    private fun reply(
        event: MessageReceivedEvent,
        level: EmbedLevel,
        message: String,
    ) {
        event.channel.sendMessageEmbeds(Embed.create(level, message).build()).queue { sent ->
            sent.delete().queueAfter(15, TimeUnit.SECONDS)
        }
    }
}
