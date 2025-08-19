package pt.paulinoo.dbotkt.di

import pt.paulinoo.dbotkt.commands.CommandHandler
import pt.paulinoo.dbotkt.commands.ServersCommand
import pt.paulinoo.dbotkt.commands.player.ClearQueueCommand
import pt.paulinoo.dbotkt.commands.player.LyricsCommand
import pt.paulinoo.dbotkt.commands.player.PauseCommand
import pt.paulinoo.dbotkt.commands.player.PlayCommand
import pt.paulinoo.dbotkt.commands.player.QueueCommand
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
import pt.paulinoo.dbotkt.commands.slash.HelpSlashCommand
import pt.paulinoo.dbotkt.commands.slash.SlashCommandHandler
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
                LyricsCommand(audioManager),
                ServersCommand(),
            ),
        )
    }
    val slashCommandHandler: SlashCommandHandler by lazy {
        SlashCommandHandler(
            listOf(
                HelpSlashCommand(),
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
