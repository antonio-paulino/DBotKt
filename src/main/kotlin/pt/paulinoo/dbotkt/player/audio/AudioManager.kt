package pt.paulinoo.dbotkt.player.audio

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import pt.paulinoo.dbotkt.player.lyrics.LrcLibResult
import pt.paulinoo.dbotkt.player.lyrics.LyricsSearchResult

interface AudioManager {
    fun getLavaPlayerStats(): String

    fun loadAndPlayPlaylist(
        channel: MessageChannel,
        guild: Guild,
        trackUrl: String,
        requesterId: Long,
    )

    fun loadAndPlaySong(
        channel: MessageChannel,
        guild: Guild,
        trackUrl: String,
        requesterId: Long,
    )

    fun pause(
        channel: MessageChannel,
        guild: Guild,
    )

    fun togglePause(
        channel: MessageChannel,
        guild: Guild,
    )

    fun resume(
        channel: MessageChannel,
        guild: Guild,
    )

    fun stop(
        channel: MessageChannel,
        guild: Guild,
    )

    fun skip(
        channel: MessageChannel,
        guild: Guild,
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
        guild: Guild,
    )

    fun reverse(
        channel: MessageChannel,
        guild: Guild,
    )

    fun toggleLoop(
        channel: MessageChannel,
        guild: Guild,
    )

    fun setEqualizer(
        channel: MessageChannel,
        guild: Guild,
        preset: EqualizerPreset,
    )

    fun getGuildPlayer(guild: Guild): GuildAudioPlayer?

    fun clearQueue(
        channel: MessageChannel,
        guild: Guild,
    )

    /** Searches LRCLIB for the currently playing track and returns the candidates to choose from. */
    fun searchLyrics(guild: Guild): LyricsSearchResult

    /** Fetches a specific lyrics candidate by its LRCLIB id. */
    fun getLyricsResult(id: Long): LrcLibResult?

    fun isPaused(guild: Guild): Boolean
}
