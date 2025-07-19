package pt.paulinoo.dbotkt.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class SlashCommandHandler(val commands: List<SlashCommand>) {
    private val commandMap = commands.associateBy { it.name }

    suspend fun handle(event: SlashCommandInteractionEvent) {
        val command = commandMap[event.name] ?: return
        command.execute(event)
    }
}
