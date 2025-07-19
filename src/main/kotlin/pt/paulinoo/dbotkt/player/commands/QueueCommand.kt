package pt.paulinoo.dbotkt.player.commands

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import pt.paulinoo.dbotkt.player.embed.PlayerEmbed
import java.util.concurrent.TimeUnit

class QueueCommand(
    private val audioCommandManager: AudioManager,
) : Command {
    override val name: String = "queue"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        val page = 1
        val channel = event.channel
        val guild = event.guild
        val guildId = guild.id

        val audioPlayer = audioCommandManager.getGuildPlayer(guild)
        if (audioPlayer == null) {
            channel.sendMessageEmbeds(
                Embed.create(
                    EmbedLevel.ERROR,
                    "No audio player found for this guild. Please join a voice channel and play something first.",
                ).build(),
            ).queue { it.delete().queueAfter(5, TimeUnit.SECONDS) }
            return
        }

        val queue = audioPlayer.queue
        val totalPages = (queue.size + 9) / 10
        val currentPage = page.coerceAtMost(totalPages.coerceAtLeast(1))
        val embed = PlayerEmbed.showQueuePaginated(channel, guild, currentPage, audioCommandManager)

        val rightEmoji = Emoji.fromUnicode("U+27A1")
        val leftEmoji = Emoji.fromUnicode("U+2B05")
        val deleteEmoji = Emoji.fromUnicode("U+274C")

        val components =
            buildList {
                if (currentPage > 1) add(Button.secondary("queue_prev:$guildId:$currentPage", leftEmoji))
                if (currentPage < totalPages) add(Button.secondary("queue_next:$guildId:$currentPage", rightEmoji))
                add(Button.secondary("queue_delete:$guildId:$currentPage", deleteEmoji))
            }

        channel.sendMessageEmbeds(embed)
            .setActionRow(components)
            .queue()
    }
}
