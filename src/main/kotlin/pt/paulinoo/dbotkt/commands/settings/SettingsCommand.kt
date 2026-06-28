package pt.paulinoo.dbotkt.commands.settings

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.config.GuildSettingsStore
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import java.util.concurrent.TimeUnit

class SettingsCommand(
    private val settings: GuildSettingsStore,
) : Command {
    override val name: String = "settings"

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
            event.channel.sendMessageEmbeds(
                Embed.create(EmbedLevel.ERROR, "You need **Manage Server** or be a bot admin to view settings.").build(),
            ).queue { it.delete().queueAfter(10, TimeUnit.SECONDS) }
            return
        }

        val current = settings.get(event.guild.idLong)

        val embed =
            Embed.create(
                level = EmbedLevel.INFO,
                title = "⚙️ Server Settings",
            ).apply {
                addField("Prefix", current.prefix?.let { "`$it`" } ?: "global default", true)
                addField("Volume", current.volume?.let { "$it%" } ?: "default", true)
                addField("Equalizer", current.equalizer, true)
            }.build()

        event.channel.sendMessageEmbeds(embed).queue { message ->
            message.delete().queueAfter(30, TimeUnit.SECONDS)
        }
    }
}
