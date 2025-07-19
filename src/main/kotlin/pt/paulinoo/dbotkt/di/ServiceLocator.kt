package pt.paulinoo.dbotkt.di

import pt.paulinoo.dbotkt.commands.CommandHandler
import pt.paulinoo.dbotkt.commands.ServersCommand
import pt.paulinoo.dbotkt.player.audio.AudioManager
import pt.paulinoo.dbotkt.player.audio.LavaAudioManager
import pt.paulinoo.dbotkt.player.audio.SpotifyHandler
import pt.paulinoo.dbotkt.player.buttons.ButtonHandler
import pt.paulinoo.dbotkt.player.buttons.ClearQueueButton
import pt.paulinoo.dbotkt.player.buttons.LoopButton
import pt.paulinoo.dbotkt.player.buttons.LyricsButton
import pt.paulinoo.dbotkt.player.buttons.PauseResumeButton
import pt.paulinoo.dbotkt.player.buttons.QueueButton
import pt.paulinoo.dbotkt.player.buttons.ShuffleButton
import pt.paulinoo.dbotkt.player.buttons.SkipButton
import pt.paulinoo.dbotkt.player.buttons.StopButton
import pt.paulinoo.dbotkt.player.buttons.VolumeDownButton
import pt.paulinoo.dbotkt.player.buttons.VolumeUpButton
import pt.paulinoo.dbotkt.player.commands.ClearQueueCommand
import pt.paulinoo.dbotkt.player.commands.PauseCommand
import pt.paulinoo.dbotkt.player.commands.PlayCommand
import pt.paulinoo.dbotkt.player.commands.QueueCommand
import pt.paulinoo.dbotkt.player.commands.RemoveCommand
import pt.paulinoo.dbotkt.player.commands.ResumeCommand
import pt.paulinoo.dbotkt.player.commands.ReverseCommand
import pt.paulinoo.dbotkt.player.commands.ShuffleCommand
import pt.paulinoo.dbotkt.player.commands.SkipCommand
import pt.paulinoo.dbotkt.player.commands.SkipToCommand
import pt.paulinoo.dbotkt.player.commands.StatsCommand
import pt.paulinoo.dbotkt.player.commands.StopCommand
import pt.paulinoo.dbotkt.player.commands.SwapCommand
import pt.paulinoo.dbotkt.player.commands.VolumeCommand

object ServiceLocator {
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
                QueueCommand(audioManager),
                ClearQueueCommand(audioManager),
                ServersCommand(),
            ),
        )
    }
    val buttonHandler by lazy {
        ButtonHandler(
            listOf(
                PauseResumeButton(audioManager),
                StopButton(audioManager),
                SkipButton(audioManager),
                VolumeUpButton(audioManager),
                VolumeDownButton(audioManager),
                ClearQueueButton(audioManager),
                LoopButton(audioManager),
                QueueButton(audioManager),
                ShuffleButton(audioManager),
                LyricsButton(audioManager),
            ),
        )
    }
    val spotifyHandler by lazy {
        SpotifyHandler(
            clientId =
                System.getenv("SPOTIFY_CLIENT_ID")
                    ?: throw IllegalArgumentException("Missing SPOTIFY_CLIENT_ID"),
            clientSecret =
                System.getenv("SPOTIFY_CLIENT_SECRET")
                    ?: throw IllegalArgumentException("Missing SPOTIFY_CLIENT_SECRET"),
        )
    }
}
