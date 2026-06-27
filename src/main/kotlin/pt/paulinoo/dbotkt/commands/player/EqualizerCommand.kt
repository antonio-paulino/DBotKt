package pt.paulinoo.dbotkt.commands.player

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import pt.paulinoo.dbotkt.player.audio.EqualizerPreset
import java.util.concurrent.TimeUnit

class EqualizerCommand(
    private val audioCommandManager: AudioManager,
) : Command {
    override val name: String = "eq"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        val presetList = EqualizerPreset.entries.joinToString("\n") { "- `${it.id}` — ${it.displayName}" }

        val requested = args.firstOrNull()
        if (requested == null) {
            val embed =
                Embed.create(
                    title = "Equalizer",
                    description =
                        "Usage: `${event.guild.selfMember.effectiveName} eq <preset>`\n\n" +
                            "Available presets:\n$presetList",
                    level = EmbedLevel.INFO,
                ).build()
            event.channel.sendMessageEmbeds(embed).queue { message ->
                message.delete().queueAfter(15, TimeUnit.SECONDS)
            }
            return
        }

        val preset = EqualizerPreset.fromId(requested)
        if (preset == null) {
            val embed =
                Embed.create(
                    title = "Unknown Preset",
                    description = "`$requested` is not a valid preset.\n\nAvailable presets:\n$presetList",
                    level = EmbedLevel.ERROR,
                ).build()
            event.channel.sendMessageEmbeds(embed).queue { message ->
                message.delete().queueAfter(15, TimeUnit.SECONDS)
            }
            return
        }

        audioCommandManager.setEqualizer(event.channel, event.guild, preset)
        val embed =
            Embed.create(
                title = "Equalizer Updated",
                description = "Equalizer preset set to **${preset.displayName}**.",
                level = EmbedLevel.INFO,
            ).build()
        event.channel.sendMessageEmbeds(embed).queue { message ->
            message.delete().queueAfter(10, TimeUnit.SECONDS)
        }
    }
}
