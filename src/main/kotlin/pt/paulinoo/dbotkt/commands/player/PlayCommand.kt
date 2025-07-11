package pt.paulinoo.dbotkt.commands.player

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.audio.AudioManager
import pt.paulinoo.dbotkt.commands.Command
import pt.paulinoo.dbotkt.embed.PlayerEmbed
class PlayCommand(
    private val audioManager: AudioManager,
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
            event.channel.sendMessage("You must be in a voice channel to use this command.").queue()
            return
        }

        val guildAudioManager = guild.audioManager
        if (!guildAudioManager.isConnected) {
            guildAudioManager.openAudioConnection(voiceChannel)
        }

        val isSpotifyPlaylist = query.contains("open.spotify.com/playlist")
        val isYoutubePlaylist = query.startsWith("http") && (query.contains("list=") || query.contains("playlist"))
        val isYoutubeVideo = query.startsWith("http") && (query.contains("youtube.com/watch") || query.contains("youtu.be"))

        when {
            isSpotifyPlaylist || isYoutubePlaylist -> {
                audioManager.loadAndPlayPlaylist(textChannel, guild, query)
                //event.channel.sendMessageEmbeds()
            }

            isYoutubeVideo -> {
                audioManager.loadAndPlaySong(textChannel, guild, query)
                event.channel.sendMessage("Loading song: $query").queue()
            }

            else -> {
                audioManager.loadAndPlaySong(textChannel, guild, "ytsearch:$query")
                event.channel.sendMessage("Loading: $query").queue()
            }
        }
    }
}
