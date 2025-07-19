package pt.paulinoo.dbotkt.player.embed

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import pt.paulinoo.dbotkt.player.audio.AudioManager
import java.util.concurrent.ConcurrentHashMap

object PlayerMessageManager {
    private val playerMessages = ConcurrentHashMap<Long, Message>()

    fun sendOrUpdatePlayerMessage(
        channel: MessageChannel,
        guild: Guild,
        audioManager: AudioManager,
    ) {
        val embed = PlayerEmbed.createPlayerEmbed(guild, audioManager)
        val buttons = PlayerEmbed.createPlayerButtons()

        val existingMessage = playerMessages[guild.idLong]

        if (existingMessage != null) {
            existingMessage.editMessageEmbeds(embed)
                .setComponents(*buttons)
                .queue()
        } else {
            val message =
                MessageCreateBuilder()
                    .setEmbeds(embed)
                    .setComponents(*buttons)
                    .build()

            channel.sendMessage(message).queue {
                playerMessages[guild.idLong] = it
            }
        }
    }

    fun removePlayerMessage(guild: Guild) {
        val message = playerMessages.remove(guild.idLong)
        message?.delete()?.queue()
    }
}
