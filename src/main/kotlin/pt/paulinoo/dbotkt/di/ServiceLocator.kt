package pt.paulinoo.dbotkt.di

import io.github.cdimascio.dotenv.dotenv
import pt.paulinoo.dbotkt.audio.AudioManager
import pt.paulinoo.dbotkt.audio.LavaAudioManager
import pt.paulinoo.dbotkt.audio.SpotifyHandler
import pt.paulinoo.dbotkt.buttons.ButtonHandler
import pt.paulinoo.dbotkt.buttons.PauseResumeButton
import pt.paulinoo.dbotkt.commands.CommandHandler
import pt.paulinoo.dbotkt.commands.ServersCommand
import pt.paulinoo.dbotkt.commands.player.PauseCommand
import pt.paulinoo.dbotkt.commands.player.PlayCommand
import pt.paulinoo.dbotkt.commands.player.RemoveCommand
import pt.paulinoo.dbotkt.commands.player.ResumeCommand
import pt.paulinoo.dbotkt.commands.player.ReverseCommand
import pt.paulinoo.dbotkt.commands.player.ShuffleCommand
import pt.paulinoo.dbotkt.commands.player.SkipCommand
import pt.paulinoo.dbotkt.commands.player.SkipToCommand
import pt.paulinoo.dbotkt.commands.player.StatsCommand
import pt.paulinoo.dbotkt.commands.player.StopCommand
import pt.paulinoo.dbotkt.commands.player.SwapCommand
import pt.paulinoo.dbotkt.commands.player.VolumeCommand

object ServiceLocator {
    private val dotenv = dotenv()
    val audioManager: AudioManager by lazy { LavaAudioManager() }
    val commandHandler: CommandHandler by lazy {
        CommandHandler(
            listOf(
                PauseCommand(audioManager),
                PlayCommand(audioManager),
                RemoveCommand(audioManager),
                ResumeCommand(audioManager),
                ReverseCommand(audioManager),
                ShuffleCommand(audioManager),
                SkipCommand(audioManager),
                SkipToCommand(audioManager),
                StatsCommand(audioManager),
                StopCommand(audioManager),
                SwapCommand(audioManager),
                VolumeCommand(audioManager),
                ServersCommand(),
            ),
        )
    }
    val buttonHandler by lazy {
        ButtonHandler(
            listOf(
                PauseResumeButton(audioManager)
            )
        )
    }
    val spotifyHandler by lazy {
        SpotifyHandler(
            clientId =
                dotenv["SPOTIFY_CLIENT_ID"]
                    ?: throw IllegalArgumentException("Missing SPOTIFY_CLIENT_ID"),
            clientSecret =
                dotenv["SPOTIFY_CLIENT_SECRET"]
                    ?: throw IllegalArgumentException("Missing SPOTIFY_CLIENT_SECRET"),
        )
    }
}
