package pt.paulinoo.dbotkt.player.audio

import se.michaelthelin.spotify.SpotifyApi
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest
import java.time.Instant

class SpotifyHandler(clientId: String, clientSecret: String) {
    private val spotifyApi =
        SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .build()

    private var tokenExpiration: Instant = Instant.MIN

    init {
        refreshToken()
    }

    private fun refreshToken() {
        val clientCredentialsRequest: ClientCredentialsRequest = spotifyApi.clientCredentials().build()
        val clientCredentials: ClientCredentials = clientCredentialsRequest.execute()

        spotifyApi.accessToken = clientCredentials.accessToken
        // Set expiration time based on current time + expiresIn seconds (usually 3600s = 1h)
        tokenExpiration = Instant.now().plusSeconds(clientCredentials.expiresIn.toLong())
    }

    private fun ensureTokenValid() {
        if (Instant.now().isAfter(tokenExpiration)) {
            refreshToken()
        }
    }

    fun extractTrackId(url: String): String? {
        val match = Regex("track/([a-zA-Z0-9]+)").find(url)
        return match?.groups?.get(1)?.value
    }

    fun getTrackMetadata(trackId: String): String {
        val trackRequest: GetTrackRequest = spotifyApi.getTrack(trackId).build()
        val track = trackRequest.execute()
        return "${track.name} - ${track.artists.joinToString(", ") { it.name }}"
    }

    fun extractPlaylistId(url: String): String? {
        val match = Regex("playlist/([a-zA-Z0-9]+)").find(url)
        return match?.groups?.get(1)?.value
    }

    fun getPlaylistMetadata(playlistId: String): List<String> {
        val playlistRequest: GetPlaylistRequest = spotifyApi.getPlaylist(playlistId).build()
        val playlist = playlistRequest.execute()
        return playlist.tracks.items.map {
            getTrackMetadata(it.track.id)
        }
    }
}
