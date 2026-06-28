package pt.paulinoo.dbotkt.commands.slash

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import pt.paulinoo.dbotkt.commands.help.HelpPages

class HelpSlashCommand : SlashCommand {
    override val name: String = "help"

    override fun getCommandData(): SlashCommandData = Commands.slash(name, "Shows the help menu")

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val prefix = HelpPages.resolvePrefix(event.guild?.idLong)
        val isAdmin = event.user.id in HelpPages.admins
        val page = HelpPages.page(0, prefix, isAdmin)
        event.replyEmbeds(page.first)
            .setComponents(page.second)
            .queue()
    }
}
