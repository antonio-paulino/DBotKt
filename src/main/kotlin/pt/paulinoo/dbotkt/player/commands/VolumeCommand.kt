package pt.paulinoo.dbotkt.player.commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import java.util.concurrent.TimeUnit

class VolumeCommand(
    private val audioCommandManager: AudioManager,
) : Command {
    override val name: String = "volume"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        if (args.isEmpty()) {
            val embed =
                Embed.create(
                    title = "Volume Command",
                    description =
                        "Usage: `${event.guild.selfMember.effectiveName} volume <level>`\n" +
                            "Sets the volume level of the audio player.\n" +
                            "Valid range is 0 to 200.",
                    level = EmbedLevel.INFO,
                ).build()
            event.channel.sendMessageEmbeds(embed).queue { message ->
                message.delete().queueAfter(10, TimeUnit.SECONDS)
            }
            return
        }

        val volume = args[0].toIntOrNull()
        if (volume == null || volume < 0 || volume > 200) {
            val embed =
                Embed.create(
                    title = "Invalid Volume Level",
                    description = "Please provide a valid volume level between 0 and 200.",
                    level = EmbedLevel.ERROR,
                ).build()
            event.channel.sendMessageEmbeds(embed).queue { message ->
                message.delete().queueAfter(10, TimeUnit.SECONDS)
            }
            return
        }
        val textChannel = event.channel

        audioCommandManager.setVolume(textChannel, event.guild, volume)
        val embed =
            Embed.create(
                title = "Volume Set",
                description = "Volume has been set to $volume.",
                level = EmbedLevel.INFO,
            ).build()
        event.channel.sendMessageEmbeds(embed).queue { message ->
            message.delete().queueAfter(10, TimeUnit.SECONDS)
        }
    }
}
