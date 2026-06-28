package pt.paulinoo.dbotkt.commands.slash

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel

class PingSlashCommand : SlashCommand {
    override val name: String = "ping"

    private val admins: Set<String> =
        System.getenv("ADMIN_IDS")
            ?.split(",")
            ?.map { it.trim() }
            ?.toSet()
            ?: emptySet()

    override fun getCommandData(): SlashCommandData = Commands.slash(name, "Show the bot's gateway and REST latency (admin only)")

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        if (event.user.id !in admins) {
            event.reply("You are not allowed to use this command.").setEphemeral(true).queue()
            return
        }

        val gatewayPing = event.jda.gatewayPing
        event.jda.restPing.queue { restPing ->
            val embed =
                Embed.create(
                    level = EmbedLevel.INFO,
                    title = "🏓 Pong!",
                    description = "**Gateway:** $gatewayPing ms\n**REST:** $restPing ms",
                ).build()
            event.replyEmbeds(embed).setEphemeral(true).queue()
        }
    }
}
