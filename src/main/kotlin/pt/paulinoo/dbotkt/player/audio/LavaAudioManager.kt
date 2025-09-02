package pt.paulinoo.dbotkt.player.audio

import com.github.topi314.lavalyrics.LyricsManager
import com.github.topi314.lavalyrics.lyrics.AudioLyrics
import com.github.topi314.lavasearch.SearchManager
import com.github.topi314.lavasrc.lrclib.LrcLibLyricsManager
import com.github.topi314.lavasrc.mirror.DefaultMirroringAudioTrackResolver
import com.github.topi314.lavasrc.spotify.SpotifySourceManager
import com.github.topi314.lavasrc.ytdlp.YtdlpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import org.slf4j.LoggerFactory
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.embed.PlayerMessageManager
import java.util.concurrent.TimeUnit

class LavaAudioManager : AudioManager {
    private val logger = LoggerFactory.getLogger(LavaAudioManager::class.java)

    private val players = mutableMapOf<Long, GuildAudioPlayer>()

    val playerManager = DefaultAudioPlayerManager()

    val searchManager = SearchManager()

    val lyricsManager = LyricsManager()

    init {
        val mirroringResolver = DefaultMirroringAudioTrackResolver(null)

        val spotifySourceManager =
            SpotifySourceManager(
                System.getenv("SPOTIFY_CLIENT_ID")
                    ?: throw IllegalArgumentException("Missing SPOTIFY_CLIENT_ID"),
                System.getenv("SPOTIFY_CLIENT_SECRET")
                    ?: throw IllegalArgumentException("Missing SPOTIFY_CLIENT_SECRET"),
                "US",
                playerManager,
                mirroringResolver,
            )
        playerManager.registerSourceManager(spotifySourceManager)

        val lyr = LrcLibLyricsManager()

        lyricsManager.registerLyricsManager(lyr)

        /*
        val youtubeSourceManager =
            YoutubeAudioSourceManager(
                TvHtml5Embedded(),
                WebWithThumbnail(),
                MWebWithThumbnail(
                    ClientOptions().apply {
                        playback = false
                    },
                ),
            )
        youtubeSourceManager.useOauth2(System.getenv("YT_REFRESH_TOKEN"), true)
        playerManager.registerSourceManager(youtubeSourceManager)
         */

        val ytdlManager =
            YtdlpAudioSourceManager(
                System.getenv("YTDLP_PATH")
                    ?: throw IllegalArgumentException("Missing YTDLP_PATH"),
            )
        playerManager.registerSourceManager(ytdlManager)

        AudioSourceManagers.registerLocalSource(playerManager)

        playerManager.configuration.apply {
            resamplingQuality = AudioConfiguration.ResamplingQuality.HIGH
            opusEncodingQuality = 10
            outputFormat = StandardAudioDataFormats.DISCORD_OPUS
        }
        searchManager.registerSearchManager(spotifySourceManager)
    }

    private fun getOrCreatePlayer(
        guild: Guild,
        channel: MessageChannel,
    ): GuildAudioPlayer =
        players.computeIfAbsent(guild.idLong) {
            val player = GuildAudioPlayer(playerManager.createPlayer())
            player.player.addListener(TrackScheduler(player.queue, guild, channel, this))
            guild.audioManager.sendingHandler = LavaPlayerAudioSendHandler(player.player)
            player
        }

    private fun loadAndPlay(
        channel: MessageChannel,
        guild: Guild,
        trackUrl: String,
        requesterId: Long,
        queueAll: Boolean,
    ) {
        val player = getOrCreatePlayer(guild, channel)

        playerManager.loadItemOrdered(
            player,
            trackUrl,
            object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    track.userData = TrackMetadata(requesterId = requesterId)
                    if (!player.player.startTrack(track, true)) {
                        player.queue.add(track)
                        logger.info("Added track to queue: ${track.info.title}")
                        val embed =
                            Embed.create(
                                EmbedLevel.INFO,
                                "Track Added",
                                "Added track: ${track.info.title} - ${track.info.author} to the queue.",
                            ).build()
                        channel.sendMessageEmbeds(embed).queue {
                            it.delete().queueAfter(10, TimeUnit.SECONDS)
                            PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, this@LavaAudioManager)
                        }
                    } else {
                        logger.info("Playing track immediately: ${track.info.title}")
                        val embed =
                            Embed.create(
                                EmbedLevel.INFO,
                                "Now Playing",
                                "Now playing: ${track.info.title} - ${track.info.author}",
                            ).build()
                        channel.sendMessageEmbeds(embed).queue {
                            it.delete().queueAfter(10, TimeUnit.SECONDS)
                        }
                    }
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    playlist.tracks.forEach { it.userData = TrackMetadata(requesterId) }
                    val firstTrack = playlist.selectedTrack ?: playlist.tracks.firstOrNull()
                    if (firstTrack != null) {
                        if (queueAll) {
                            playlist.tracks.drop(1).forEach { player.queue.add(it) }
                            logger.info("Playlist loaded: ${playlist.name}, queue size is now ${player.queue.size + 1}")
                            val embed =
                                Embed.create(
                                    EmbedLevel.INFO,
                                    "Playlist Loaded",
                                    "Added ${playlist.tracks.size} tracks from playlist: ${playlist.name} to the queue.",
                                ).build()
                            channel.sendMessageEmbeds(embed).queue {
                                it.delete().queueAfter(10, TimeUnit.SECONDS)
                                PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, this@LavaAudioManager)
                            }
                        } else {
                            if (player.player.playingTrack == null) {
                                logger.info("Now playing: ${firstTrack.info.title}")
                                val embed =
                                    Embed.create(
                                        EmbedLevel.INFO,
                                        "Now Playing",
                                        "Now playing: ${firstTrack.info.title} - ${firstTrack.info.author}",
                                    ).build()
                                channel.sendMessageEmbeds(embed).queue {
                                    it.delete().queueAfter(10, TimeUnit.SECONDS)
                                }
                            } else {
                                logger.info("Loaded only the first track from playlist: ${firstTrack.info.title}")
                                val embed =
                                    Embed.create(
                                        EmbedLevel.INFO,
                                        "Track Loaded",
                                        "Added track: ${firstTrack.info.title} - ${firstTrack.info.author} to the queue.",
                                    ).build()
                                channel.sendMessageEmbeds(embed).queue {
                                    it.delete().queueAfter(10, TimeUnit.SECONDS)
                                    PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, this@LavaAudioManager)
                                }
                            }
                        }
                        if (player.player.startTrack(firstTrack, true)) {
                            player.queue.remove(firstTrack)
                        } else {
                            player.queue.add(firstTrack)
                        }
                    }
                }

                override fun noMatches() {
                    logger.warn("No matches found for $trackUrl")
                    val embed =
                        Embed.create(
                            EmbedLevel.ERROR,
                            "No Matches Found",
                            "No matches found for the provided URL: $trackUrl",
                        ).build()
                    channel.sendMessageEmbeds(embed).queue {
                        it.delete().queueAfter(10, TimeUnit.SECONDS)
                        PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, this@LavaAudioManager)
                    }
                }

                override fun loadFailed(exception: FriendlyException) {
                    logger.error("Track load failed", exception)
                    val embed =
                        Embed.create(
                            EmbedLevel.ERROR,
                            "Track Load Failed",
                            "Failed to load track: ${exception.message}",
                        ).build()
                    channel.sendMessageEmbeds(embed).queue {
                        it.delete().queueAfter(10, TimeUnit.SECONDS)
                        PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, this@LavaAudioManager)
                    }
                }
            },
        )
    }

    override fun loadAndPlayPlaylist(
        channel: MessageChannel,
        guild: Guild,
        trackUrl: String,
        requesterId: Long,
    ) {
        loadAndPlay(channel, guild, trackUrl, requesterId, queueAll = true)
    }

    override fun loadAndPlaySong(
        channel: MessageChannel,
        guild: Guild,
        trackUrl: String,
        requesterId: Long,
    ) {
        loadAndPlay(channel, guild, trackUrl, requesterId, queueAll = false)
    }

    override fun loadAndPlaySpotifyPlaylist(
        channel: MessageChannel,
        guild: Guild,
        songsMetadata: List<String>,
        requesterId: Long,
    ) {
        songsMetadata.forEach { song ->
            val trackUrl = "ytsearch:$song"
            loadAndPlay(channel, guild, trackUrl, requesterId, queueAll = false)
        }
    }

    override fun pause(
        channel: MessageChannel,
        guild: Guild,
    ) {
        getOrCreatePlayer(guild, channel).player.isPaused = true
        logger.info("Paused playback in guild ${guild.name}")
        PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, this)
    }

    override fun togglePause(
        channel: MessageChannel,
        guild: Guild,
    ) {
        val player = getOrCreatePlayer(guild, channel).player
        player.isPaused = !player.isPaused
        logger.info(
            if (player.isPaused) {
                "Paused playback in guild ${guild.name}"
            } else {
                "Resumed playback in guild ${guild.name}"
            },
        )
        PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, this)
    }

    override fun isPaused(guild: Guild): Boolean = players[guild.idLong]?.isPaused ?: false

    override fun resume(
        channel: MessageChannel,
        guild: Guild,
    ) {
        getOrCreatePlayer(guild, channel).player.isPaused = false
        logger.info("Resumed playback in guild ${guild.name}")
        PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, this)
    }

    override fun stop(
        channel: MessageChannel,
        guild: Guild,
    ) {
        val player = getOrCreatePlayer(guild, channel)
        player.player.stopTrack()
        PlayerMessageManager.removePlayerMessage(guild)
        player.queue.clear()
        players.remove(guild.idLong)
        guild.audioManager.closeAudioConnection()
        logger.info("Stopped playback and cleared queue in guild ${guild.name}")
    }

    override fun skip(
        channel: MessageChannel,
        guild: Guild,
    ) {
        val player = getOrCreatePlayer(guild, channel)
        val queue = player.queue
        if (queue.isEmpty()) {
            logger.info("No more tracks to skip to in guild ${guild.name}")
            return
        }
        val nextTrack = queue.removeFirst()
        player.player.playTrack(nextTrack)
        logger.info("Skipped to next track: ${nextTrack.info.title} in guild ${guild.name}")
        PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, this)
    }

    override fun skipTo(
        channel: MessageChannel,
        guild: Guild,
        trackNumber: Int,
    ) {
        val player = getOrCreatePlayer(guild, channel)
        val queue = player.queue

        if (trackNumber !in 0 until queue.size) {
            logger.warn("Invalid track number: $trackNumber in guild ${guild.name}")
            return
        }

        val trackToSkipTo = (0..trackNumber).map { queue.removeFirst() }.last()
        player.player.playTrack(trackToSkipTo)
        logger.info("Skipped to track number $trackNumber in guild ${guild.name}")
        PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, this)
    }

    override fun setVolume(
        channel: MessageChannel,
        guild: Guild,
        volume: Int,
    ) {
        val player = getOrCreatePlayer(guild, channel)
        player.player.volume = volume
        logger.info("Set volume to $volume in guild ${guild.name}")
        PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, this)
    }

    override fun swap(
        channel: MessageChannel,
        guild: Guild,
        first: Int,
        second: Int,
    ) {
        val queue = players[guild.idLong]?.queue ?: return

        if (first !in 1 until queue.size || second !in 1 until queue.size) {
            logger.warn("Invalid track indices: $first, $second in guild ${guild.name}")
            return
        }

        val tmp = queue[first - 1]
        queue[first - 1] = queue[second - 1]
        queue[second] = tmp

        logger.info("Swapped tracks at indices $first and $second in guild ${guild.name}")
        PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, this)
    }

    override fun remove(
        channel: MessageChannel,
        guild: Guild,
        trackNumber: Int,
    ) {
        val queue = players[guild.idLong]?.queue ?: return

        if (trackNumber !in 0 until queue.size) {
            logger.warn("Invalid track number: $trackNumber in guild ${guild.name}")
            return
        }

        val removedTrack = queue.removeAt(trackNumber)
        logger.info("Removed track: ${removedTrack.info.title} at index $trackNumber in guild ${guild.name}")
        PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, this)
    }

    override fun shuffle(
        channel: MessageChannel,
        guild: Guild,
    ) {
        val player = players[guild.idLong] ?: return
        val queue = player.queue

        if (queue.isEmpty()) {
            logger.warn("Cannot shuffle an empty queue in guild ${guild.name}")
            return
        }

        player.queue = ArrayDeque(queue.shuffled())
        logger.info("Shuffled the queue in guild ${guild.name}, new size is ${player.queue.size}")
        PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, this)
    }

    override fun reverse(
        channel: MessageChannel,
        guild: Guild,
    ) {
        val player = players[guild.idLong] ?: return
        val queue = player.queue

        if (queue.isEmpty()) {
            logger.warn("Cannot reverse an empty queue in guild ${guild.name}")
            return
        }

        player.queue = ArrayDeque(queue.reversed())
        logger.info("Reversed the queue in guild ${guild.name}, new size is ${player.queue.size}")
        PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, this)
    }

    override fun toggleLoop(
        channel: MessageChannel,
        guild: Guild,
    ) {
        val player = getOrCreatePlayer(guild, channel)
        player.isLooping = !player.isLooping
        logger.info(
            if (player.isLooping) {
                "Enabled looping in guild ${guild.name}"
            } else {
                "Disabled looping in guild ${guild.name}"
            },
        )
        PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, this)
    }

    override fun getGuildPlayer(guild: Guild): GuildAudioPlayer? = players[guild.idLong]

    override fun clearQueue(
        channel: MessageChannel,
        guild: Guild,
    ) {
        val player = getOrCreatePlayer(guild, channel)
        player.queue.clear()
        logger.info("Cleared the queue in guild ${guild.name}")
        PlayerMessageManager.sendOrUpdatePlayerMessage(channel, guild, this)
    }

    override fun getLyrics(
        channel: MessageChannel,
        guild: Guild,
    ): AudioLyrics? {
        val player = getOrCreatePlayer(guild, channel)
        val currentTrack = player.player.playingTrack ?: return null

        if (System.getenv("SP_DC_COOKIE") == null) {
            return null
        }

        val lyrics = lyricsManager.loadLyrics(currentTrack)

        return lyrics
    }

    override fun getLavaPlayerStats(): String {
        val totalPlayers = players.size
        val playingPlayers = players.count { !it.value.isPaused }
        val pausedPlayers = players.count { it.value.isPaused }
        val memory = Runtime.getRuntime()
        val usedMemory = (memory.totalMemory() - memory.freeMemory()) / (1024 * 1024)
        val maxMemory = memory.maxMemory() / (1024 * 1024)

        return """
            **Lavaplayer Stats:**
            - Total players: $totalPlayers
            - Playing: $playingPlayers
            - Paused: $pausedPlayers
            - Memory usage: $usedMemory MB / $maxMemory MB
            """.trimIndent()
    }
}
