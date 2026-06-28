package pt.paulinoo.dbotkt.player.listeners

import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import pt.paulinoo.dbotkt.player.audio.AudioManager

class VoiceChannelEmptyListener(
    private val audioCommandManager: AudioManager,
) : ListenerAdapter() {
    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        val guild = event.guild
        val connectedChannel = guild.selfMember.voiceState?.channel as? VoiceChannel ?: return

        if (connectedChannel.members.none { !it.user.isBot }) {
            audioCommandManager.stop(connectedChannel, guild)
        }
    }
}
