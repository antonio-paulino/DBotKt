package pt.paulinoo.dbotkt.player.audio

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

enum class LoopMode {
    NONE,
    SINGLE,
    QUEUE,
}

class GuildAudioPlayer(
    val player: AudioPlayer,
) {
    var queue: ArrayDeque<AudioTrack> = ArrayDeque()

    val isPaused: Boolean
        get() = player.isPaused

    var loopMode: LoopMode = LoopMode.NONE

    /** Shared, mutable 15-band equalizer applied to the live filter chain. */
    val equalizerFactory: EqualizerFactory = EqualizerFactory()

    var equalizerPreset: EqualizerPreset = EqualizerPreset.FLAT

    /** Set when the player is created so its resources can be released on stop. */
    var scheduler: TrackScheduler? = null

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
