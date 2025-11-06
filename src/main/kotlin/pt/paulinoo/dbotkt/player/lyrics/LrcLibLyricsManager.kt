package pt.paulinoo.dbotkt.player.lyrics

import com.github.topi314.lavalyrics.AudioLyricsManager
import com.github.topi314.lavalyrics.lyrics.AudioLyrics
import com.github.topi314.lavalyrics.lyrics.BasicAudioLyrics
import com.github.topi314.lavalyrics.lyrics.BasicAudioLyrics.BasicLine
import com.github.topi314.lavasrc.LavaSrcTools
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.text.Normalizer
import java.time.Duration
import java.util.Locale

class LrcLibLyricsManager : AudioLyricsManager {
    private val httpInterfaceManager: HttpInterfaceManager = HttpClientTools.createCookielessThreadLocalManager()

    override fun getSourceName(): String {
        return "LRCLIB"
    }

    override fun loadLyrics(audioTrack: AudioTrack): AudioLyrics? {
        try {
            return getLyrics(audioTrack)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @Throws(IOException::class)
    private fun getLyrics(track: AudioTrack): AudioLyrics? {
        val info = track.info

        val trackName = normalize(cleanTitle(info.title))

        val uri: URI?
        try {
            val uriBuilder = URIBuilder(API_BASE + "search")
            uriBuilder.addParameter("q", trackName)
            uri = uriBuilder.build()
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }

        println(uri)

        val json: JsonBrowser?
        this.httpInterfaceManager.getInterface().use { httpInterface ->
            val request = HttpGet(uri)
            json = LavaSrcTools.fetchResponseAsJson(httpInterface, request)
        }
        if (json == null) return null

        return parseLyrics(json)
    }

    private fun parseLyrics(json: JsonBrowser): AudioLyrics {
        println(
            "JSON - TEXT : ${json.text()}",
        )
        val lyricsText = json.get("plainLyrics").text()
        val lyrics: MutableList<AudioLyrics.Line?> = ArrayList()

        for (line in json.get("syncedLyrics").safeText().split("\\n".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()) {
            val parts: Array<String?> = line.split(" ".toRegex(), limit = 2).toTypedArray()
            if (parts.size < 2) continue

            val timePart = parts[0]!!.substring(1, parts[0]!!.length - 1)
            val timeParts: Array<String?> = timePart.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (timeParts.size != 2) continue

            try {
                val timestamp =
                    Duration.ofMinutes(timeParts[0]!!.toInt().toLong())
                        .plusMillis((timeParts[1]!!.toDouble() * 1000).toLong())
                lyrics.add(BasicLine(timestamp, null, parts[1]))
            } catch (ignored: NumberFormatException) {
            }
        }

        println("LYRICS FOUND: $lyricsText")

        return BasicAudioLyrics("LRCLIB", "LRCLIB", lyricsText, lyrics)
    }

    private fun cleanTitle(title: String?): String? {
        if (title == null) return null
        return title.replace("\\s*\\([^)]*\\)".toRegex(), "") // remove parênteses
            .replace("\\s*\\[[^]]*]".toRegex(), "") // remove colchetes
            .trim { it <= ' ' }
    }

    private fun normalize(text: String?): String {
        if (text == null) return ""
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace("\\p{M}".toRegex(), "") // remove acentos
            .replace("[^a-zA-Z0-9 ]".toRegex(), "") // remove caracteres especiais
            .lowercase(Locale.getDefault())
            .trim { it <= ' ' }
    }

    override fun shutdown() {
        try {
            this.httpInterfaceManager.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private const val API_BASE = "https://lrclib.net/api/"
    }
}
