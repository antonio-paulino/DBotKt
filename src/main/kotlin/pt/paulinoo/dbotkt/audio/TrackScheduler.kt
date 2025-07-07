package pt.paulinoo.dbotkt.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason

class TrackScheduler(
    private val player: AudioPlayer,
    private val queue: ArrayDeque<AudioTrack>,
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
            }
        }
    }

    override fun onEvent(event: AudioEvent?) {
        super.onEvent(event)
    }
}
