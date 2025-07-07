package pt.paulinoo.dbotkt.command

import net.dv8tion.jda.api.events.message.MessageReceivedEvent

interface Command {
    val name: String

    suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    )
}
