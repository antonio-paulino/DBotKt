package pt.paulinoo.dbotkt.player.audio

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.DISCORD_OPUS
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.ByteBuffer

class LavaPlayerAudioSendHandler(private val player: AudioPlayer) : AudioSendHandler {
    private val frameBuffer = ByteBuffer.allocate(DISCORD_OPUS.maximumChunkSize())
    private val frame = MutableAudioFrame().apply { setBuffer(frameBuffer) }

    private var lastFrame: ByteArray? = null

    override fun canProvide(): Boolean {
        val didProvide = player.provide(frame)
        if (didProvide) {
            frameBuffer.flip()
            val data = ByteArray(frame.dataLength)
            frameBuffer.get(data)
            frameBuffer.clear()
            lastFrame = data
        }
        return didProvide
    }

    override fun provide20MsAudio(): ByteBuffer? {
        return lastFrame?.let { ByteBuffer.wrap(it) }
    }

    override fun isOpus(): Boolean = true
}
