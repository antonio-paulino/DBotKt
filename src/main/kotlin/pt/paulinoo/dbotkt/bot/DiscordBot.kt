package pt.paulinoo.dbotkt.bot

import club.minnced.discord.jdave.interop.JDaveSessionFactory
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.audio.AudioModuleConfig
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import pt.paulinoo.dbotkt.commands.help.HelpPaginationListener
import pt.paulinoo.dbotkt.di.ServiceLocator
import pt.paulinoo.dbotkt.di.ServiceLocator.audioManager
import pt.paulinoo.dbotkt.health.HealthServer
import pt.paulinoo.dbotkt.player.listeners.ButtonListener
import pt.paulinoo.dbotkt.player.listeners.EqualizerSelectListener
import pt.paulinoo.dbotkt.player.listeners.LyricsSelectListener
import pt.paulinoo.dbotkt.player.listeners.QueueButtonListener
import pt.paulinoo.dbotkt.player.listeners.VoiceChannelEmptyListener
import pt.paulinoo.dbotkt.stats.StatsService
import java.time.Duration

class DiscordBot() : CoroutineScope {
    private var token: String =
        System.getenv("DISCORD_TOKEN")
            ?: throw IllegalArgumentException("DISCORD_TOKEN environment variable is not set")

    private val job = SupervisorJob()
    override val coroutineContext = Dispatchers.Default + job

    @OptIn(DelicateCoroutinesApi::class)
    private val jda =
        JDABuilder.createDefault(token)
            .enableIntents(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_WEBHOOKS,
            )
            .addEventListeners(
                object : ListenerAdapter() {
                    override fun onMessageReceived(event: MessageReceivedEvent) {
                        launch {
                            ServiceLocator.commandHandler.handle(event)
                        }
                    }

                    override fun onButtonInteraction(event: ButtonInteractionEvent) {
                        launch {
                            ServiceLocator.buttonHandler.handle(event)
                        }
                    }

                    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
                        launch {
                            ServiceLocator.slashCommandHandler.handle(event)
                        }
                    }
                },
                QueueButtonListener(audioManager),
                ButtonListener(audioManager),
                HelpPaginationListener(),
                EqualizerSelectListener(audioManager),
                LyricsSelectListener(audioManager),
                VoiceChannelEmptyListener(audioManager),
            )
            .setAudioModuleConfig(
                AudioModuleConfig()
                    .withAudioSendFactory(
                        NativeAudioSendFactory(),
                    )
                    .withDaveSessionFactory(JDaveSessionFactory()),
            )
            .setActivity(Activity.listening("/help"))
            .build()
            .awaitReady()

    private val healthServer: HealthServer? = startHealthServer()

    init {
        registerCommands()
    }

    private fun registerCommands() {
        val commands = ServiceLocator.slashCommandHandler.commands
        jda.updateCommands().addCommands(commands.map { it.getCommandData() }).queue()
    }

    private fun startHealthServer(): HealthServer? {
        val portValue = System.getenv("HEALTH_PORT") ?: "8080"
        if (portValue.equals("off", ignoreCase = true) || portValue.isBlank()) return null
        val port = portValue.toIntOrNull() ?: return null

        return HealthServer(port) {
            val snapshot = StatsService.gather(jda, audioManager)
            snapshot.healthy to StatsService.toJson(snapshot)
        }.also { it.start() }
    }

    fun shutdown() {
        healthServer?.stop()
        jda.shutdown()
        if (!jda.awaitShutdown(Duration.ofSeconds(10))) {
            jda.shutdownNow()
            jda.awaitShutdown()
            job.cancel()
        }
    }
}
