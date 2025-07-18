package pt.paulinoo.dbotkt.player.embed

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.buttons.Button
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import java.util.concurrent.TimeUnit

class QueueButtonListener(
    private val audioManager: AudioManager,
) : ListenerAdapter() {
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val id = event.componentId

        if (!id.startsWith("queue_")) return

        val parts = id.split(":")
        if (parts.size != 3) return

        val direction = parts[0].removePrefix("queue_")
        val guildId = parts[1]
        val currentPage = parts[2].toIntOrNull() ?: return

        if (direction == "delete") {
            event.deferEdit().queue { event.message.delete().queue() }
            return
        }

        val guild = event.jda.getGuildById(guildId) ?: return
        val newPage =
            when (direction) {
                "prev" -> (currentPage - 1).coerceAtLeast(1)
                "next" -> currentPage + 1
                else -> return
            }

        val audioPlayer = audioManager.getGuildPlayer(guild)
        if (audioPlayer == null) {
            val embed =
                Embed.create(
                    EmbedLevel.ERROR,
                    "No audio player found for this guild. Please join a voice channel and play something first.",
                ).build()
            event.channel.sendMessageEmbeds(embed).queue {
                it.delete().queueAfter(5, TimeUnit.SECONDS)
            }
            return
        }
        val queue = audioPlayer.queue
        val totalPages = (queue.size + 9) / 10
        if (newPage > totalPages) return

        val newEmbed =
            PlayerEmbed.showQueuePaginated(
                event.channel,
                guild,
                newPage,
                audioManager,
            )
        val newComponents = mutableListOf<Button>()

        val rightEmoji = Emoji.fromUnicode("U+27A1")
        val leftEmoji = Emoji.fromUnicode("U+2B05")
        val deleteEmoji = Emoji.fromUnicode("U+274C")

        if (newPage > 1) {
            newComponents.add(Button.secondary("queue_prev:$guildId:$newPage", leftEmoji))
        }
        if (newPage < totalPages) {
            newComponents.add(Button.secondary("queue_next:$guildId:$newPage", rightEmoji))
        }

        newComponents.add(Button.secondary("queue_delete:$guildId:$newPage", deleteEmoji))

        event.editMessageEmbeds(newEmbed)
            .setActionRow(newComponents)
            .queue()
    }
}
