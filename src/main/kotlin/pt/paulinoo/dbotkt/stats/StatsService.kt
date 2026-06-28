package pt.paulinoo.dbotkt.stats

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel
import pt.paulinoo.dbotkt.metrics.BotMetrics
import pt.paulinoo.dbotkt.player.audio.AudioManager

/** A full point-in-time view of bot health, gateway, audio, totals and system metrics. */
data class StatsSnapshot(
    val status: String,
    val gatewayPing: Long,
    val healthy: Boolean,
    val guilds: Int,
    val members: Long,
    val voiceConnections: Int,
    val players: Int,
    val playing: Int,
    val paused: Int,
    val queuedTracks: Int,
    val queuedDurationMs: Long,
    val tracksPlayed: Long,
    val commandsExecuted: Long,
    val errors: Long,
    val system: SystemSnapshot,
)

/** Gathers and renders [StatsSnapshot]s for the stats command, slash command and health endpoint. */
object StatsService {
    fun gather(
        jda: JDA,
        audio: AudioManager,
    ): StatsSnapshot {
        val audioStats = audio.getAudioStats()
        val metrics = BotMetrics.snapshot()
        return StatsSnapshot(
            status = jda.status.name,
            gatewayPing = jda.gatewayPing,
            healthy = jda.status == JDA.Status.CONNECTED,
            guilds = jda.guildCache.size().toInt(),
            members = jda.guilds.sumOf { it.memberCount.toLong() },
            voiceConnections = jda.guilds.count { it.audioManager.isConnected },
            players = audioStats.totalPlayers,
            playing = audioStats.playing,
            paused = audioStats.paused,
            queuedTracks = audioStats.queuedTracks,
            queuedDurationMs = audioStats.queuedDurationMs,
            tracksPlayed = metrics.tracksPlayed,
            commandsExecuted = metrics.commandsExecuted,
            errors = metrics.errors,
            system = SystemStats.snapshot(),
        )
    }

    fun embed(
        snapshot: StatsSnapshot,
        requestedBy: String?,
    ): MessageEmbed {
        val system = snapshot.system
        return Embed.create(
            level = if (snapshot.healthy) EmbedLevel.SUCCESS else EmbedLevel.WARNING,
            title = "📊 Bot Health & Statistics",
            footer = requestedBy?.let { "Requested by $it" },
        ).apply {
            addField("Status", "${if (snapshot.healthy) "🟢" else "🟡"} ${snapshot.status}", true)
            addField("Gateway Ping", "${snapshot.gatewayPing} ms", true)
            addField("Uptime", SystemStats.formatUptime(system.uptimeMillis), true)

            addField("Servers", snapshot.guilds.toString(), true)
            addField("Members", snapshot.members.toString(), true)
            addField("Voice Connections", snapshot.voiceConnections.toString(), true)

            addField("Players", "${snapshot.players} (${snapshot.playing} ▶ / ${snapshot.paused} ⏸)", true)
            addField("Queued Tracks", snapshot.queuedTracks.toString(), true)
            addField("Queued Duration", SystemStats.formatUptime(snapshot.queuedDurationMs), true)

            addField("Tracks Played", snapshot.tracksPlayed.toString(), true)
            addField("Commands Run", snapshot.commandsExecuted.toString(), true)
            addField("Errors", snapshot.errors.toString(), true)

            addField(
                "Heap Memory",
                "${SystemStats.formatBytes(system.usedHeapBytes)} / ${SystemStats.formatBytes(system.maxHeapBytes)}",
                true,
            )
            addField(
                "CPU",
                "proc ${SystemStats.formatLoad(system.processCpuLoad)} • sys ${SystemStats.formatLoad(system.systemCpuLoad)}",
                true,
            )
            addField("Threads / Cores", "${system.threadCount} / ${system.availableProcessors}", true)

            addField("JVM", system.jvmVersion, true)
            addField("OS", "${system.osName} (${system.osArch})", true)
        }.build()
    }

    fun toJson(snapshot: StatsSnapshot): String {
        val s = snapshot
        val sys = s.system
        return buildString {
            append("{")
            append("\"status\":\"").append(s.status).append("\",")
            append("\"healthy\":").append(s.healthy).append(",")
            append("\"gatewayPingMs\":").append(s.gatewayPing).append(",")
            append("\"uptimeMs\":").append(sys.uptimeMillis).append(",")
            append("\"guilds\":").append(s.guilds).append(",")
            append("\"members\":").append(s.members).append(",")
            append("\"voiceConnections\":").append(s.voiceConnections).append(",")
            append("\"players\":{\"total\":").append(s.players)
            append(",\"playing\":").append(s.playing).append(",\"paused\":").append(s.paused).append("},")
            append("\"queue\":{\"tracks\":").append(s.queuedTracks).append(",\"durationMs\":").append(s.queuedDurationMs).append("},")
            append("\"totals\":{\"tracksPlayed\":").append(s.tracksPlayed)
            append(",\"commands\":").append(s.commandsExecuted).append(",\"errors\":").append(s.errors).append("},")
            append(
                "\"memory\":{\"usedHeapBytes\":",
            ).append(sys.usedHeapBytes).append(",\"maxHeapBytes\":").append(sys.maxHeapBytes).append("},")
            append("\"cpu\":{\"process\":").append(sys.processCpuLoad).append(",\"system\":").append(sys.systemCpuLoad).append("},")
            append("\"threads\":").append(sys.threadCount).append(",")
            append("\"cores\":").append(sys.availableProcessors)
            append("}")
        }
    }
}
