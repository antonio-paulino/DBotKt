package pt.paulinoo.dbotkt.bot

import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import pt.paulinoo.dbotkt.di.ServiceLocator
import pt.paulinoo.dbotkt.di.ServiceLocator.audioManager
import pt.paulinoo.dbotkt.player.listeners.LyricsButtonListener
import pt.paulinoo.dbotkt.player.listeners.QueueButtonListener
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
                },
                QueueButtonListener(audioManager),
                LyricsButtonListener(audioManager),
            )
            .setAudioSendFactory(NativeAudioSendFactory())
            .setActivity(Activity.listening("!help"))
            .build()
            .awaitReady()

    fun shutdown() {
        jda.shutdown()
        if (!jda.awaitShutdown(Duration.ofSeconds(10))) {
            jda.shutdownNow()
            jda.awaitShutdown()

            job.cancel()
        }
    }
}
