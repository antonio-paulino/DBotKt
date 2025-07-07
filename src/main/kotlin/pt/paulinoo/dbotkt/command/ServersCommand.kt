package pt.paulinoo.dbotkt.command

import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class ServersCommand: Command {
    override val name: String = "servers"

    override suspend fun execute(event: MessageReceivedEvent, args: List<String>) {
        val guilds = event.jda.guilds
        val serverList = guilds.joinToString("\n") { it.name }
        event.channel.sendMessage("Connected to the following servers:\n$serverList").queue()
    }
}