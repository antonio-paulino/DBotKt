package pt.paulinoo.dbotkt.player.commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import java.util.concurrent.TimeUnit

class SkipToCommand(
    private val audioCommandManager: AudioManager,
) : Command {
    override val name: String = "skipto"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        if (args.isEmpty()) {
            val embed =
                Embed.create(
                    level = EmbedLevel.WARNING,
                    title = "Invalid Usage",
                    description = "Usage: `${event.guild.selfMember.effectiveName} skipto <track number>`",
                ).build()
            event.channel.sendMessageEmbeds(embed).queue { message ->
                message.delete().queueAfter(10, TimeUnit.SECONDS)
            }
            return
        }

        val trackNumber = args.firstOrNull()?.toIntOrNull()
        if (trackNumber == null || trackNumber < 1) {
            val embed =
                Embed.create(
                    level = EmbedLevel.ERROR,
                    title = "Invalid Track Number",
                    description = "Please provide a valid track number.",
                )
            event.channel.sendMessageEmbeds(embed.build()).queue { message ->
                message.delete().queueAfter(10, TimeUnit.SECONDS)
            }
            return
        }

        val guild = event.guild
        val textChannel = event.channel
        audioCommandManager.skipTo(textChannel, guild, trackNumber - 1) // Convert to zero-based index
        val embed =
            Embed.create(
                level = EmbedLevel.INFO,
                title = "Skipped to Track",
                description = "Skipped to track number $trackNumber.",
            )
        event.channel.sendMessageEmbeds(embed.build()).queue { message ->
            message.delete().queueAfter(10, TimeUnit.SECONDS)
        }
    }
}
