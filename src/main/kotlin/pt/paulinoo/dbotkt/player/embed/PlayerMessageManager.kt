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
                .queue(
                    { updated -> playerMessages[guild.idLong] = updated },
                    { error ->
                        println("Falha ao editar mensagem de player: ${error.message}")
                        playerMessages.remove(guild.idLong)
                    },
                )
        } else {
            val message =
                MessageCreateBuilder()
                    .setEmbeds(embed)
                    .setComponents(*buttons)
                    .build()

            channel.sendMessage(message).queue(
                { sentMessage -> playerMessages[guild.idLong] = sentMessage },
                { error -> println("Falha ao enviar mensagem de player: ${error.message}") },
            )
        }
    }

    fun removePlayerMessage(guild: Guild) {
        playerMessages.remove(guild.idLong)?.delete()?.queue()
    }
}
