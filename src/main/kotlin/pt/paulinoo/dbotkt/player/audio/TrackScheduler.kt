package pt.paulinoo.dbotkt.player.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.embed.PlayerMessageManager
import java.util.concurrent.TimeUnit.MINUTES
import kotlin.time.Duration.Companion.minutes

class TrackScheduler(
    private val queue: ArrayDeque<AudioTrack>,
    private val guild: Guild,
    private val channel: MessageChannel,
    private val audioManager: AudioManager,
) : AudioEventAdapter() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var disconnectJob: Job? = null

    override fun onTrackEnd(
        player: AudioPlayer,
        track: AudioTrack,
        endReason: AudioTrackEndReason,
    ) {
        if (endReason.mayStartNext) {
            if (queue.isNotEmpty()) {
                val next = queue.removeFirst()
                player.startTrack(next, false)
            } else {
                PlayerMessageManager.removePlayerMessage(guild)

                disconnectJob =
                    scope.launch {
                        val embed =
                            Embed.create(
                                EmbedLevel.INFO,
                                "Queue Finished",
                                "The queue is empty. I will leave the channel in 5 minutes if no new songs are added.",
                            ).build()

                        channel.sendMessageEmbeds(embed).queue { message ->
                            message.delete().queueAfter(1, MINUTES)
                        }

                        delay(5.minutes)

                        val leaveEmbed =
                            Embed.create(
                                EmbedLevel.INFO,
                                "Leaving Voice Channel",
                                "I have been inactive for 5 minutes. Leaving the voice channel.",
                            ).build()
                        channel.sendMessageEmbeds(leaveEmbed).queue { message ->
                            message.delete().queueAfter(1, MINUTES)
                        }
                        audioManager.stop(channel, guild)
                    }
            }
        }
    }

    override fun onTrackStart(
        player: AudioPlayer,
        track: AudioTrack,
    ) {
        disconnectJob?.cancel()
        disconnectJob = null
        PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, audioManager)
    }

    override fun onPlayerPause(player: AudioPlayer?) {
        PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, audioManager)
    }

    override fun onPlayerResume(player: AudioPlayer?) {
        PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, audioManager)
    }
}
