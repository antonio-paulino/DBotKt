package pt.paulinoo.dbotkt.player.lyrics

import com.google.gson.Gson
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder

data class LyricsResponse(
    val id: Int,
    val trackName: String?,
    val artistName: String?,
    val albumName: String?,
    val duration: Int,
    val instrumental: Boolean,
    val plainLyrics: String?,
    val syncedLyrics: String?,
)

class LyricsSource(
    private val endpoint: String = "https://lrclib.net/api/get",
    private val timeoutMillis: Int = 5000,
) {
    private val gson = Gson()

    fun fetchLyrics(
        trackName: String,
        artistName: String,
        duration: Long,
    ): String? {
        val durationSeconds = duration / 1000
        val queryParams =
            listOf(
                "track_name=${trackName.encode()}",
                "artist_name=${artistName.encode()}",
                "duration=$durationSeconds",
            ).joinToString("&")

        val url = "$endpoint?$queryParams"
        val connection = URI.create(url).toURL().openConnection() as HttpURLConnection

        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = timeoutMillis
            connection.readTimeout = timeoutMillis

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { reader ->
                    val body = reader.readText()
                    val response = gson.fromJson(body, LyricsResponse::class.java)
                    response.plainLyrics
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun String.encode(): String = URLEncoder.encode(this, Charsets.UTF_8)
}
