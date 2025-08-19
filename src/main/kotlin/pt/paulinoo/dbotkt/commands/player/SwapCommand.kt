package pt.paulinoo.dbotkt.commands.player

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import java.util.concurrent.TimeUnit

class SwapCommand(
    private val audioCommandManager: AudioManager,
) : Command {
    override val name: String = "swap"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        val guild = event.guild
        val audioManager = guild.audioManager
        val textChannel = event.channel

        if (audioManager.isConnected) {
            if (args.size >= 2) {
                val first = args[0].toIntOrNull()
                val second = args[1].toIntOrNull()
                if (first != null && second != null) {
                    audioCommandManager.swap(textChannel, guild, first, second)
                    val embed =
                        Embed.create(
                            EmbedLevel.INFO,
                            "Swapped track $first with track $second.",
                        ).build()
                    event.channel.sendMessageEmbeds(embed).queue { message ->
                        message.delete().queueAfter(10, TimeUnit.SECONDS)
                    }
                } else {
                    val embed =
                        Embed.create(
                            EmbedLevel.ERROR,
                            "Invalid track numbers provided. Please provide two valid track numbers.",
                        ).build()
                    event.channel.sendMessageEmbeds(embed).queue { message ->
                        message.delete().queueAfter(10, TimeUnit.SECONDS)
                    }
                }
            } else {
                val embed =
                    Embed.create(
                        title = "Swap Command",
                        description = "Usage: `${event.guild.selfMember.effectiveName} swap <track number 1> <track number 2>`",
                        level = EmbedLevel.WARNING,
                    ).build()
                event.channel.sendMessageEmbeds(embed).queue { message ->
                    message.delete().queueAfter(10, TimeUnit.SECONDS)
                }
            }
        } else {
            val embed =
                Embed.create(
                    description = "Not connected to a voice channel.",
                    level = EmbedLevel.WARNING,
                ).build()
            textChannel.sendMessageEmbeds(embed).queue { message ->
                message.delete().queueAfter(10, TimeUnit.SECONDS)
            }
        }
    }
}
