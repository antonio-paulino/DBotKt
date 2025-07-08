package pt.paulinoo.dbotkt.audio

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.DISCORD_OPUS
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.ByteBuffer

class LavaPlayerAudioSendHandler(private val player: AudioPlayer) : AudioSendHandler {
    private val frameBuffer = ByteBuffer.allocate(DISCORD_OPUS.maximumChunkSize())
    private val frame = MutableAudioFrame().apply { setBuffer(frameBuffer) }

    override fun canProvide(): Boolean {
        return player.provide(frame)
    }

    override fun provide20MsAudio(): ByteBuffer {
        return (frameBuffer.flip() as ByteBuffer)
    }

    override fun isOpus(): Boolean = true
}
