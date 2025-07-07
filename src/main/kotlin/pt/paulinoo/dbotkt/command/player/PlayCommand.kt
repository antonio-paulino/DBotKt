package pt.paulinoo.dbotkt.command.player

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import pt.paulinoo.dbotkt.audio.AudioCommandManager
import pt.paulinoo.dbotkt.command.Command

class PlayCommand(
    private val audioCommandManager: AudioCommandManager,
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

        if (voiceChannel == null) {
            event.channel.sendMessage("You must be in a voice channel to use this command.").queue()
            return
        }

        val audioManager = guild.audioManager
        if (!audioManager.isConnected) {
            audioManager.openAudioConnection(voiceChannel)
        }

        if (query.startsWith("http") && query.contains("playlist")) {
            audioCommandManager.loadAndPlayPlaylist(guild, query)
            event.channel.sendMessage("Loading playlist...").queue()
        } else {
            audioCommandManager.loadAndPlaySong(guild, "ytsearch:$query")
            event.channel.sendMessage("Loading: $query").queue()
        }
    }
}
