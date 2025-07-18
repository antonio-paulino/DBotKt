package pt.paulinoo.dbotkt.player.commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.player.audio.AudioManager
import java.util.concurrent.TimeUnit

class PlayCommand(
    private val audioCommandManager: AudioManager,
) : Command {
    override val name: String = "play"

    override suspend fun execute(
        event: MessageReceivedEvent,
        args: List<String>,
    ) {
        val query = args.joinToString(" ").takeIf { it.isNotBlank() } ?: return
        val guild = event.guild
        val member = event.member ?: return
        val voiceChannel = member.voiceState?.channel
        val textChannel = event.channel

        if (voiceChannel == null) {
            val embed =
                Embed.create(
                    description = "You must be in a voice channel to use this command.",
                    level = EmbedLevel.WARNING,
                ).build()
            textChannel.sendMessageEmbeds(embed).queue { message ->
                message.delete().queueAfter(10, TimeUnit.SECONDS)
            }
            return
        }

        val guildAudioManager = guild.audioManager
        if (!guildAudioManager.isConnected) {
            guildAudioManager.openAudioConnection(voiceChannel)
            guildAudioManager.isSelfDeafened = true
        }

        val isSpotifyPlaylist = query.contains("open.spotify.com/playlist")
        val isYoutubePlaylist = query.startsWith("http") && (query.contains("list=") || query.contains("playlist"))
        val isYoutubeVideo = query.startsWith("http") && (query.contains("youtube.com/watch") || query.contains("youtu.be"))

        val requesterId = event.author.idLong

        when {
            isSpotifyPlaylist || isYoutubePlaylist -> {
                audioCommandManager.loadAndPlayPlaylist(textChannel, guild, query, requesterId)
            }

            isYoutubeVideo -> {
                audioCommandManager.loadAndPlaySong(textChannel, guild, query, requesterId)
            }

            else -> {
                audioCommandManager.loadAndPlaySong(textChannel, guild, "ytsearch:$query", requesterId)
            }
        }
    }
}
