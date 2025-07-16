package pt.paulinoo.dbotkt.player.commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import java.util.concurrent.TimeUnit

class SkipCommand(
    private val audioCommandManager: AudioManager,
) : Command {
    override val name: String = "skip"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        val guild = event.guild
        val audioManager = guild.audioManager
        val textChannel = event.channel

        if (audioManager.isConnected) {
            audioCommandManager.skip(textChannel, guild)
            val embed =
                Embed.create(
                    description = "Skipped to the next track.",
                    level = EmbedLevel.INFO,
                ).build()
            event.channel.sendMessageEmbeds(embed).queue { message ->
                message.delete().queueAfter(10, TimeUnit.SECONDS)
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
