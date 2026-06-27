package pt.paulinoo.dbotkt.player.listeners

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import pt.paulinoo.dbotkt.player.audio.AudioManager
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds

class VoiceChannelEmptyListener(
    private val audioCommandManager: AudioManager,
) : ListenerAdapter() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val pendingLeaves = ConcurrentHashMap<Long, Job>()

    private val graceSeconds: Long =
        System.getenv("EMPTY_CHANNEL_TIMEOUT_SECONDS")?.toLongOrNull()?.coerceAtLeast(0) ?: 60L

    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        val guild: Guild = event.guild
        val connectedChannel = guild.selfMember.voiceState?.channel as? VoiceChannel ?: return

        val hasListeners = connectedChannel.members.any { !it.user.isBot }

        if (hasListeners) {
            // Someone is (still) around: cancel any scheduled departure.
            pendingLeaves.remove(guild.idLong)?.cancel()
            return
        }

        // Channel is empty. Schedule a single delayed leave, re-checking before acting
        // so we don't disconnect if a member rejoins within the grace period.
        pendingLeaves.computeIfAbsent(guild.idLong) {
            scope.launch {
                delay((graceSeconds * 1000).milliseconds)
                val current = guild.selfMember.voiceState?.channel as? VoiceChannel
                if (current != null && current.members.none { !it.user.isBot }) {
                    audioCommandManager.stop(current, guild)
                }
                pendingLeaves.remove(guild.idLong)
            }
        }
    }
}
