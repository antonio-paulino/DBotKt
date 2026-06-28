package pt.paulinoo.dbotkt.commands.slash

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import pt.paulinoo.dbotkt.player.audio.AudioManager
import pt.paulinoo.dbotkt.stats.StatsService

class StatsSlashCommand(
    private val audioManager: AudioManager,
) : SlashCommand {
    override val name: String = "stats"

    private val admins: Set<String> =
        System.getenv("ADMIN_IDS")
            ?.split(",")
            ?.map { it.trim() }
            ?.toSet()
            ?: emptySet()

    override fun getCommandData(): SlashCommandData = Commands.slash(name, "Show bot health & statistics (admin only)")

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        if (event.user.id !in admins) {
            event.reply("You are not allowed to use this command.").setEphemeral(true).queue()
            return
        }

        val snapshot = StatsService.gather(event.jda, audioManager)
        event.replyEmbeds(StatsService.embed(snapshot, event.user.name)).setEphemeral(true).queue()
    }
}
