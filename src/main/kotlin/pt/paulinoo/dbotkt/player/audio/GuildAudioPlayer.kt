package pt.paulinoo.dbotkt.player.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class GuildAudioPlayer(
    val player: AudioPlayer,
) {
    var queue: ArrayDeque<AudioTrack> = ArrayDeque()

    var isPaused: Boolean = false

    var isLooping: Boolean = false

    fun clearQueue() {
        queue.clear()
    }

    fun addTrack(track: AudioTrack) {
        queue.add(track)
    }

    fun removeTrack(track: AudioTrack) {
        queue.remove(track)
    }
}
