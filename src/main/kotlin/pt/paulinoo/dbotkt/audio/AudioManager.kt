package pt.paulinoo.dbotkt.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

interface AudioManager {
    fun getLavaPlayerStats(): String

    fun loadAndPlaySpotifyPlaylist(
        channel: MessageChannel,
        guild: Guild,
        songsMetadata: List<String>,
    )

    fun loadAndPlayPlaylist(
        channel: MessageChannel,
        guild: Guild,
        trackUrl: String,
    )

    fun loadAndPlaySong(
        channel: MessageChannel,
        guild: Guild,
        trackUrl: String,
    )

    fun pause(
        channel: MessageChannel,
        guild: Guild
    )

    fun resume(
        channel: MessageChannel,
        guild: Guild
    )

    fun stop(
        channel: MessageChannel,
        guild: Guild
    )

    fun skip(
        channel: MessageChannel,
        guild: Guild
    )

    fun skipTo(
        channel: MessageChannel,
        guild: Guild,
        trackNumber: Int,
    )

    fun setVolume(
        channel: MessageChannel,
        guild: Guild,
        volume: Int,
    )

    fun swap(
        channel: MessageChannel,
        guild: Guild,
        first: Int,
        second: Int,
    )

    fun remove(
        channel: MessageChannel,
        guild: Guild,
        trackNumber: Int,
    )

    fun shuffle(
        channel: MessageChannel,
        guild: Guild
    )

    fun reverse(
        channel: MessageChannel,
        guild: Guild
    )

    fun getGuildPlayer(
        guild: Guild
    ): AudioPlayer
}
