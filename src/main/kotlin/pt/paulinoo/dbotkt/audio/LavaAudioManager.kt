package pt.paulinoo.dbotkt.audio

import com.github.topi314.lavasearch.SearchManager
import com.github.topi314.lavasrc.mirror.DefaultMirroringAudioTrackResolver
import com.github.topi314.lavasrc.spotify.SpotifySourceManager
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.lavalink.youtube.YoutubeAudioSourceManager
import dev.lavalink.youtube.clients.MWebWithThumbnail
import dev.lavalink.youtube.clients.TvHtml5Embedded
import dev.lavalink.youtube.clients.WebWithThumbnail
import io.github.cdimascio.dotenv.dotenv
import net.dv8tion.jda.api.entities.Guild
import org.slf4j.LoggerFactory

class LavaAudioManager : AudioCommandManager {
    private val logger = LoggerFactory.getLogger(LavaAudioManager::class.java)

    private val players = mutableMapOf<Long, AudioPlayer>()

    val playerManager = DefaultAudioPlayerManager()

    val searchManager = SearchManager()

    private val queues = mutableMapOf<Long, ArrayDeque<AudioTrack>>()

    private val dotenv =
        dotenv {
        }

    init {
        val mirroringResolver =
            DefaultMirroringAudioTrackResolver(
                null,
            )

        val spotifySourceManager =
            SpotifySourceManager(
                dotenv["SPOTIFY_CLIENT_ID"]
                    ?: throw IllegalArgumentException("Missing SPOTIFY_CLIENT_ID"),
                dotenv["SPOTIFY_CLIENT_SECRET"]
                    ?: throw IllegalArgumentException("Missing SPOTIFY_CLIENT_SECRET"),
                "US",
                playerManager,
                mirroringResolver,
            )
        playerManager.registerSourceManager(spotifySourceManager)

        val youtubeSourceManager =
            YoutubeAudioSourceManager(
                TvHtml5Embedded(),
                WebWithThumbnail(),
                MWebWithThumbnail(),
            )
        youtubeSourceManager.useOauth2(dotenv["YT_REFRESH_TOKEN"], true)
        playerManager.registerSourceManager(youtubeSourceManager)
        AudioSourceManagers.registerLocalSource(playerManager)

        playerManager.configuration.apply {
            resamplingQuality = AudioConfiguration.ResamplingQuality.HIGH
            opusEncodingQuality = 10
            outputFormat = StandardAudioDataFormats.DISCORD_OPUS
        }

        searchManager.registerSearchManager(spotifySourceManager)
    }

    private fun getOrCreatePlayer(guild: Guild): AudioPlayer {
        return players.computeIfAbsent(guild.idLong) {
            val player = playerManager.createPlayer()
            val queue = queues.computeIfAbsent(guild.idLong) { ArrayDeque() }
            player.addListener(TrackScheduler(player, queue))
            guild.audioManager.sendingHandler = LavaPlayerAudioSendHandler(player)
            player
        }
    }

    private fun loadAndPlay(
        guild: Guild,
        trackUrl: String,
        queueAll: Boolean,
    ) {
        val player = getOrCreatePlayer(guild)
        val queue = queues.computeIfAbsent(guild.idLong) { ArrayDeque() }

        playerManager.loadItemOrdered(
            player,
            trackUrl,
            object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    if (!player.startTrack(track, true)) {
                        queue.add(track)
                        logger.info("Added track to queue: ${track.info.title}")
                    } else {
                        logger.info("Playing track immediately: ${track.info.title}")
                    }
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    val firstTrack = playlist.selectedTrack ?: playlist.tracks.firstOrNull()
                    if (firstTrack != null) {
                        if (!player.startTrack(firstTrack, true)) {
                            queue.add(firstTrack)
                        }
                        if (queueAll) {
                            playlist.tracks.drop(1).forEach { queue.add(it) }
                            logger.info("Playlist loaded: ${playlist.name}, queue size is now ${queue.size + 1}")
                        } else {
                            logger.info("Loaded only the first track from playlist: ${firstTrack.info.title}")
                        }
                    }
                }

                override fun noMatches() {
                    logger.warn("No matches found for $trackUrl")
                }

                override fun loadFailed(exception: FriendlyException) {
                    logger.error("Track load failed", exception)
                }
            },
        )
    }

    override fun loadAndPlayPlaylist(
        guild: Guild,
        trackUrl: String,
    ) {
        loadAndPlay(guild, trackUrl, queueAll = true)
    }

    override fun loadAndPlaySong(
        guild: Guild,
        trackUrl: String,
    ) {
        loadAndPlay(guild, trackUrl, queueAll = false)
    }

    override fun loadAndPlaySpotifyPlaylist(
        guild: Guild,
        songsMetadata: List<String>,
    ) {
        songsMetadata.forEach { song ->
            val trackUrl = "ytsearch:$song"
            loadAndPlay(guild, trackUrl, queueAll = false)
        }
    }

    override fun pause(guild: Guild) {
        getOrCreatePlayer(guild).isPaused = true
        logger.info("Paused playback in guild ${guild.name}")
    }

    override fun resume(guild: Guild) {
        getOrCreatePlayer(guild).isPaused = false
        logger.info("Resumed playback in guild ${guild.name}")
    }

    override fun stop(guild: Guild) {
        val player = getOrCreatePlayer(guild)
        player.stopTrack()
        queues.remove(guild.idLong)
        players.remove(guild.idLong)
        guild.audioManager.closeAudioConnection()
        logger.info("Stopped playback and cleared queue in guild ${guild.name}")
    }

    override fun skip(guild: Guild) {
        getOrCreatePlayer(guild).playTrack(null)
        logger.info("Skipped current track in guild ${guild.name}")
    }
}
