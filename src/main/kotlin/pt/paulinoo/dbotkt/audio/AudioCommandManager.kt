package pt.paulinoo.dbotkt.audio

import net.dv8tion.jda.api.entities.Guild

interface AudioCommandManager {
    fun getLavaPlayerStats(): String

    fun loadAndPlaySpotifyPlaylist(
        guild: Guild,
        songsMetadata: List<String>,
    )

    fun loadAndPlayPlaylist(
        guild: Guild,
        trackUrl: String,
    )

    fun loadAndPlaySong(
        guild: Guild,
        trackUrl: String,
    )

    fun pause(guild: Guild)

    fun resume(guild: Guild)

    fun stop(guild: Guild)

    fun skip(guild: Guild)

    fun skipTo(
        guild: Guild,
        trackNumber: Int,
    )

    fun setVolume(
        guild: Guild,
        volume: Int,
    )

    fun swap(
        guild: Guild,
        first: Int,
        second: Int,
    )

    fun remove(
        guild: Guild,
        trackNumber: Int,
    )

    fun shuffle(guild: Guild)

    fun reverse(guild: Guild)
}
