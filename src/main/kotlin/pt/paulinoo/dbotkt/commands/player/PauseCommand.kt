package pt.paulinoo.dbotkt.commands.player

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import java.util.concurrent.TimeUnit

class PauseCommand(
    private val audioCommandManager: AudioManager,
) : Command {
    override val name: String = "pause"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        val guild = event.guild
        val audioManager = guild.audioManager
        val textChannel = event.channel

        if (audioManager.isConnected) {
            audioCommandManager.pause(textChannel, guild)
            val embed =
                Embed.create(
                    EmbedLevel.INFO,
                    "Playback paused.",
                ).build()
            event.channel.sendMessageEmbeds(embed).queue { message ->
                message.delete().queueAfter(10, TimeUnit.SECONDS)
            }
        } else {
            val embed =
                Embed.create(
                    EmbedLevel.ERROR,
                    "Not connected to a voice channel.",
                ).build()
            event.channel.sendMessageEmbeds(embed).queue { message ->
                message.delete().queueAfter(10, TimeUnit.SECONDS)
            }
        }
    }
}
