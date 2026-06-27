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
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.text.Normalizer
import java.time.Duration
import java.util.Locale
import kotlin.math.abs

/** The outcome of a lyrics search: every candidate plus the best automatic pick. */
data class LyricsSearchResult(
    val results: List<LrcLibResult>,
    val best: LrcLibResult?,
)

/** A single entry returned by LRCLIB's `/api/search` endpoint. */
data class LrcLibResult(
    val id: Long,
    val trackName: String,
    val artistName: String,
    val albumName: String?,
    val durationSeconds: Double,
    val instrumental: Boolean,
    val plainLyrics: String?,
    val syncedLyrics: String?,
) {
    val hasSynced: Boolean get() = !syncedLyrics.isNullOrBlank()
    val hasLyrics: Boolean get() = hasSynced || !plainLyrics.isNullOrBlank()

    fun toAudioLyrics(): AudioLyrics =
        BasicAudioLyrics(
            "LRCLIB",
            "LRCLIB",
            plainLyrics.orEmpty(),
            LrcLibLyricsManager.parseSyncedLyrics(syncedLyrics),
        )
}

class LrcLibLyricsManager : AudioLyricsManager {
    private val logger = LoggerFactory.getLogger(LrcLibLyricsManager::class.java)
    private val httpInterfaceManager: HttpInterfaceManager = HttpClientTools.createCookielessThreadLocalManager()

    init {
        // LRCLIB asks API consumers to send an identifying User-Agent; its default-UA path
        // can be slow/throttled. Set our own UA and sane timeouts so requests don't hang.
        httpInterfaceManager.configureRequests { config ->
            RequestConfig.copy(config)
                .setConnectTimeout(REQUEST_TIMEOUT_MS)
                .setSocketTimeout(REQUEST_TIMEOUT_MS)
                .setConnectionRequestTimeout(REQUEST_TIMEOUT_MS)
                .build()
        }
        httpInterfaceManager.configureBuilder { builder ->
            builder.setUserAgent(USER_AGENT)
        }
    }

    override fun getSourceName(): String = "LRCLIB"

    override fun loadLyrics(audioTrack: AudioTrack): AudioLyrics? = searchForTrack(audioTrack).best?.toAudioLyrics()

    /**
     * Searches LRCLIB for [track] and returns all candidates plus the best pick.
     *
     * It first runs a precise structured search (`track_name` + `artist_name`), which keeps
     * results tied to the actual artist, and only falls back to a looser free-text title
     * search when the structured one finds nothing.
     */
    fun searchForTrack(track: AudioTrack): LyricsSearchResult {
        val title =
            cleanTitle(track.info.title)?.takeIf { it.isNotBlank() }
                ?: return LyricsSearchResult(emptyList(), null)
        val artist = cleanArtist(track.info.author)

        var results = if (artist.isNullOrBlank()) emptyList() else searchStructured(title, artist)
        if (results.isEmpty()) {
            results = search(normalize(title))
        }

        return LyricsSearchResult(results, pickBest(results, track.duration))
    }

    /** Fetches a single record by its LRCLIB id (`/api/get/{id}`), or null if unavailable. */
    fun getById(id: Long): LrcLibResult? {
        val uri =
            try {
                URIBuilder(API_BASE + "get/" + id).build()
            } catch (e: URISyntaxException) {
                throw RuntimeException(e)
            }

        return try {
            val json =
                httpInterfaceManager.getInterface().use { httpInterface ->
                    LavaSrcTools.fetchResponseAsJson(httpInterface, HttpGet(uri))
                }
            if (json == null || !json.isMap) null else json.toResult()
        } catch (e: IOException) {
            logger.warn("Failed to fetch LRCLIB lyrics by id {}: {}", id, e.message)
            null
        }
    }

    /**
     * Searches LRCLIB for [query] and returns the best matching lyrics, picking the
     * candidate whose duration is closest to [durationMs] (when provided) and
     * preferring synced lyrics. Returns null when nothing usable is found.
     */
    fun loadLyrics(
        query: String,
        durationMs: Long? = null,
    ): AudioLyrics? = pickBest(search(query), durationMs)?.toAudioLyrics()

    /**
     * Calls LRCLIB's partial-search endpoint (`/api/search?q=`) and returns every
     * match it reports (up to ~20). The list lets callers present the options and
     * let the user pick the right one. May be empty; never null.
     */
    fun search(query: String): List<LrcLibResult> = fetchResults(buildSearchUri { it.addParameter("q", query) })

    /**
     * Precise search using LRCLIB's structured parameters. Keeps matches tied to the given
     * [artistName] instead of fuzzy-matching the whole free-text query.
     */
    fun searchStructured(
        trackName: String,
        artistName: String,
    ): List<LrcLibResult> =
        fetchResults(
            buildSearchUri {
                it.addParameter("track_name", trackName)
                it.addParameter("artist_name", artistName)
            },
        )

    private fun buildSearchUri(configure: (URIBuilder) -> Unit): URI =
        try {
            URIBuilder(API_BASE + "search").also(configure).build()
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }

    private fun fetchResults(uri: URI): List<LrcLibResult> {
        logger.debug("LRCLIB search: {}", uri)

        val json: JsonBrowser? =
            httpInterfaceManager.getInterface().use { httpInterface ->
                LavaSrcTools.fetchResponseAsJson(httpInterface, HttpGet(uri))
            }

        if (json == null || !json.isList) return emptyList()

        return json.values().mapNotNull { it.toResult() }
    }

    private fun JsonBrowser.toResult(): LrcLibResult? {
        val trackName = get("trackName").text() ?: return null
        return LrcLibResult(
            id = get("id").asLong(0),
            trackName = trackName,
            artistName = get("artistName").text().orEmpty(),
            albumName = get("albumName").text(),
            durationSeconds = get("duration").text()?.toDoubleOrNull() ?: 0.0,
            instrumental = get("instrumental").asBoolean(false),
            plainLyrics = get("plainLyrics").text(),
            syncedLyrics = get("syncedLyrics").text(),
        )
    }

    /** Picks the most relevant result: non-instrumental, closest duration, synced first. */
    internal fun pickBest(
        results: List<LrcLibResult>,
        durationMs: Long?,
    ): LrcLibResult? {
        val usable = results.filter { it.hasLyrics && !it.instrumental }.ifEmpty { results }
        if (usable.isEmpty()) return null

        val targetSeconds = durationMs?.let { it / 1000.0 }
        return usable.minWithOrNull(
            compareBy(
                { result -> targetSeconds?.let { abs(result.durationSeconds - it) } ?: 0.0 },
                { result -> if (result.hasSynced) 0 else 1 },
            ),
        )
    }

    private fun cleanTitle(title: String?): String? {
        if (title == null) return null
        return title.replace("\\s*\\([^)]*\\)".toRegex(), "") // remove parentheses
            .replace("\\s*\\[[^]]*]".toRegex(), "") // remove brackets
            .trim { it <= ' ' }
    }

    /**
     * Normalises a track author into an artist name for structured search by dropping common
     * YouTube channel noise ("- Topic", "VEVO", "Official"), so e.g. "Queen - Topic" -> "Queen".
     */
    internal fun cleanArtist(author: String?): String? {
        if (author == null) return null
        return author.replace("\\s*-\\s*Topic$".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("VEVO$".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("\\s*-\\s*Official.*$".toRegex(RegexOption.IGNORE_CASE), "")
            .trim()
            .takeIf { it.isNotBlank() }
    }

    private fun normalize(text: String?): String {
        if (text == null) return ""
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace("\\p{M}".toRegex(), "") // strip accents
            .replace("[^a-zA-Z0-9 ]".toRegex(), "") // strip special characters
            .lowercase(Locale.getDefault())
            .trim { it <= ' ' }
    }

    override fun shutdown() {
        try {
            httpInterfaceManager.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private const val API_BASE = "https://lrclib.net/api/"
        private const val REQUEST_TIMEOUT_MS = 10_000
        private const val USER_AGENT = "DBotKt (https://github.com/paulinoo/dbotkt)"

        /** Parses LRC-style `[mm:ss.xx] text` lines into timestamped lyric lines. */
        internal fun parseSyncedLyrics(syncedLyrics: String?): List<AudioLyrics.Line> {
            if (syncedLyrics.isNullOrBlank()) return emptyList()

            val lines = mutableListOf<AudioLyrics.Line>()
            for (line in syncedLyrics.split("\n")) {
                val parts = line.split(" ", limit = 2)
                if (parts.size < 2) continue

                val timePart = parts[0]
                if (timePart.length < 2 || !timePart.startsWith("[") || !timePart.endsWith("]")) continue

                val timeParts = timePart.substring(1, timePart.length - 1).split(":")
                if (timeParts.size != 2) continue

                try {
                    val timestamp =
                        Duration.ofMinutes(timeParts[0].toInt().toLong())
                            .plusMillis((timeParts[1].toDouble() * 1000).toLong())
                    lines.add(BasicLine(timestamp, null, parts[1]))
                } catch (_: NumberFormatException) {
                    // Skip malformed timestamps.
                }
            }
            return lines
        }
    }
}
