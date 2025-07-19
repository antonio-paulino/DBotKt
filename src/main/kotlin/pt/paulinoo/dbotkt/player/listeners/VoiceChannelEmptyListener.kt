package pt.paulinoo.dbotkt.player.listeners

import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import pt.paulinoo.dbotkt.player.audio.AudioManager

class VoiceChannelEmptyListener(
    private val audioCommandManager: AudioManager
) : ListenerAdapter() {
    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        val guild: Guild = event.guild
        val selfMember: Member = guild.selfMember
        val connectedChannel = selfMember.voiceState?.channel as? VoiceChannel ?: return

        val nonBotMembers = connectedChannel.members.filter { !it.user.isBot }
        if (nonBotMembers.isEmpty()) {
            audioCommandManager.stop(connectedChannel, guild)
        }
    }
}