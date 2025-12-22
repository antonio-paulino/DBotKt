package pt.paulinoo.dbotkt.commands.player

import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import java.util.concurrent.TimeUnit

class LyricsCommand(
    private val audioCommandManager: AudioManager,
) : Command {
    override val name: String = "lyrics"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        val guild = event.guild

        val audioPlayer = audioCommandManager.getGuildPlayer(guild)
        if (audioPlayer == null) {
            val embed =
                Embed.create(
                    EmbedLevel.ERROR,
                    "Not connected to a voice channel.",
                ).build()
            event.channel.sendMessageEmbeds(embed).queue { message ->
                message.delete().queueAfter(10, TimeUnit.SECONDS)
            }
            return
        }

        val lyrics = audioCommandManager.getLyrics(channel = event.channel, guild = guild)

        if (lyrics == null) {
            val embed =
                Embed.create(
                    EmbedLevel.ERROR,
                    "No lyrics found for the current track.",
                ).build()
            event.channel.sendMessageEmbeds(embed).queue { message ->
                message.delete().queueAfter(10, TimeUnit.SECONDS)
            }
            return
        }
        val embed =
            Embed.create(
                EmbedLevel.INFO,
                "Lyrics for current track:",
                lyrics.text,
            ).build()

        val deleteEmoji = Emoji.fromUnicode("U+274C")

        event.channel.sendMessageEmbeds(embed)
            .setComponents(
                ActionRow.of(
                    Button.secondary(
                        "button_delete",
                        deleteEmoji,
                    ),
                ),
            ).queue()
    }
}
