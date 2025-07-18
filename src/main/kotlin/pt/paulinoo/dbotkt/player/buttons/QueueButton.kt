package pt.paulinoo.dbotkt.player.buttons

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import pt.paulinoo.dbotkt.player.embed.PlayerEmbed
import java.util.concurrent.TimeUnit

class QueueButton(private val audioManager: AudioManager) : CustomButton {
    override val customId: String = "queue_button"

    override suspend fun handle(event: ButtonInteractionEvent) {
        val channel = event.channel
        val guild = event.guild

        if (guild == null) {
            val embed =
                Embed.create(
                    EmbedLevel.ERROR,
                    "This command can only be used in a guild context.",
                )
            channel.sendMessageEmbeds(embed.build()).queue {
                it.delete().queueAfter(10, TimeUnit.SECONDS)
            }
            return
        }

        val audioPlayer = audioManager.getGuildPlayer(guild)
        if (audioPlayer == null) {
            val embed =
                Embed.create(
                    EmbedLevel.ERROR,
                    "No audio player found for this guild. Please join a voice channel and play something first.",
                )
            channel.sendMessageEmbeds(embed.build()).queue {
                it.delete().queueAfter(10, TimeUnit.SECONDS)
            }
            return
        }

        val queue = audioPlayer.queue
        val totalPages = (queue.size + 9) / 10
        val page = 1
        val embed = PlayerEmbed.showQueuePaginated(channel, guild, page, audioManager)
        val guildId = guild.id

        val rightEmoji = Emoji.fromUnicode("U+27A1")
        val deleteEmoji = Emoji.fromUnicode("U+274C")

        val components =
            buildList {
                if (page < totalPages) add(Button.secondary("queue_next:$guildId:$page", rightEmoji))
                add(Button.secondary("queue_delete:$guildId:$page", deleteEmoji))
            }

        event.deferEdit().queue()
        channel.sendMessageEmbeds(embed)
            .setActionRow(components)
            .queue()
    }
}
