package pt.paulinoo.dbotkt.player.commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import java.util.concurrent.TimeUnit

class RemoveCommand(
    private val audioCommandManager: AudioManager,
) : Command {
    override val name: String = "remove"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        if (args.isEmpty()) {
            val embed =
                Embed.create(
                    title = "Remove Command",
                    description = "Usage: `${event.guild.selfMember.effectiveName} remove <song index>`",
                    level = EmbedLevel.WARNING,
                ).build()
            event.channel.sendMessageEmbeds(embed).queue { message ->
                message.delete().queueAfter(10, TimeUnit.SECONDS)
            }
            return
        }

        val index = args[0].toIntOrNull()
        if (index == null || index < 1) {
            val embed =
                Embed.create(
                    title = "Invalid Index",
                    description = "Please provide a valid song index.",
                    level = EmbedLevel.ERROR,
                ).build()
            event.channel.sendMessageEmbeds(embed).queue { message ->
                message.delete().queueAfter(10, TimeUnit.SECONDS)
            }
            return
        }
        val textChannel = event.channel

        audioCommandManager.remove(textChannel, event.guild, index - 1)
        val embed =
            Embed.create(
                title = "Song Removed",
                description = "Song at index $index has been removed from the queue.",
                level = EmbedLevel.INFO,
            ).build()
        event.channel.sendMessageEmbeds(embed).queue { message ->
            message.delete().queueAfter(10, TimeUnit.SECONDS)
        }
    }
}
