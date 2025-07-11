package pt.paulinoo.dbotkt.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import pt.paulinoo.dbotkt.embed.PlayerMessageManager

class TrackScheduler(
    private val queue: ArrayDeque<AudioTrack>,
    private val guild: Guild,
    private val channel: MessageChannel,
    private val audioManager: AudioManager
) : AudioEventAdapter() {
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
                channel.sendMessage("Queue finished.").queue()
            }
        }
    }
    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        super.onTrackStart(player, track)
        PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, audioManager)
    }

    override fun onPlayerPause(player: AudioPlayer?) {
        super.onPlayerPause(player)
        PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, audioManager)
    }

    override fun onPlayerResume(player: AudioPlayer?) {
        super.onPlayerResume(player)
        PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, audioManager)
    }
}
