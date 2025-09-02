package pt.paulinoo.dbotkt.player.buttons

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import java.util.concurrent.TimeUnit

class LyricsButton(private val audioManager: AudioManager) : CustomButton {
    override val customId: String = "lyrics_button"

    override suspend fun handle(event: ButtonInteractionEvent) {
        val guild = event.guild
        if (guild == null) {
            event.reply("This command can only be used in a guild.").setEphemeral(true).queue()
            return
        }

        val lyrics = audioManager.getLyrics(event.channel, guild)
        event.deferEdit().queue()

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

        event.channel.sendMessageEmbeds(embed).setActionRow(
            Button.secondary(
                "button_delete",
                deleteEmoji,
            ),
        ).queue()
    }
}
