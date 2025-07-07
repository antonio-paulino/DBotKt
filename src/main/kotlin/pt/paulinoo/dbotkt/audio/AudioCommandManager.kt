package pt.paulinoo.dbotkt.audio

import net.dv8tion.jda.api.entities.Guild

interface AudioCommandManager {
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
}
