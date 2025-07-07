package pt.paulinoo.dbotkt.di

import io.github.cdimascio.dotenv.dotenv
import pt.paulinoo.dbotkt.audio.AudioCommandManager
import pt.paulinoo.dbotkt.audio.LavaAudioManager
import pt.paulinoo.dbotkt.audio.SpotifyHandler
import pt.paulinoo.dbotkt.command.CommandHandler
import pt.paulinoo.dbotkt.command.PauseCommand
import pt.paulinoo.dbotkt.command.PlayCommand
import pt.paulinoo.dbotkt.command.StopCommand

object ServiceLocator {
    private val dotenv = dotenv()
    val audioManager: AudioCommandManager by lazy { LavaAudioManager() }
    val commandHandler: CommandHandler by lazy {
        CommandHandler(
            listOf(
                PlayCommand(audioManager),
                StopCommand(audioManager),
                PauseCommand(audioManager)
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
