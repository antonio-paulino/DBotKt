package pt.paulinoo.dbotkt.commands.slash

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

interface SlashCommand {
    val name: String

    fun getCommandData(): SlashCommandData

    suspend fun execute(event: SlashCommandInteractionEvent)
}