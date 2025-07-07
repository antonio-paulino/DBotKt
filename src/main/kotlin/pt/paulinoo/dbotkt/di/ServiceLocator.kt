package pt.paulinoo.dbotkt.di

import io.github.cdimascio.dotenv.dotenv
import pt.paulinoo.dbotkt.audio.AudioCommandManager
import pt.paulinoo.dbotkt.audio.LavaAudioManager
import pt.paulinoo.dbotkt.audio.SpotifyHandler
import pt.paulinoo.dbotkt.command.CommandHandler
import pt.paulinoo.dbotkt.command.ServersCommand
import pt.paulinoo.dbotkt.command.player.PauseCommand
import pt.paulinoo.dbotkt.command.player.PlayCommand
import pt.paulinoo.dbotkt.command.player.RemoveCommand
import pt.paulinoo.dbotkt.command.player.ResumeCommand
import pt.paulinoo.dbotkt.command.player.ReverseCommand
import pt.paulinoo.dbotkt.command.player.ShuffleCommand
import pt.paulinoo.dbotkt.command.player.SkipCommand
import pt.paulinoo.dbotkt.command.player.SkipToCommand
import pt.paulinoo.dbotkt.command.player.StatsCommand
import pt.paulinoo.dbotkt.command.player.StopCommand
import pt.paulinoo.dbotkt.command.player.SwapCommand
import pt.paulinoo.dbotkt.command.player.VolumeCommand

object ServiceLocator {
    private val dotenv = dotenv()
    val audioManager: AudioCommandManager by lazy { LavaAudioManager() }
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
