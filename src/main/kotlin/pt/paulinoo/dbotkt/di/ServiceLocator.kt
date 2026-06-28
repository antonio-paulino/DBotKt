package pt.paulinoo.dbotkt.di

import pt.paulinoo.dbotkt.commands.CommandHandler
import pt.paulinoo.dbotkt.commands.CooldownManager
import pt.paulinoo.dbotkt.commands.ServersCommand
import pt.paulinoo.dbotkt.commands.player.ClearQueueCommand
import pt.paulinoo.dbotkt.commands.player.EqualizerCommand
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
import pt.paulinoo.dbotkt.commands.settings.PrefixCommand
import pt.paulinoo.dbotkt.commands.settings.SettingsCommand
import pt.paulinoo.dbotkt.commands.slash.HelpSlashCommand
import pt.paulinoo.dbotkt.commands.slash.PingSlashCommand
import pt.paulinoo.dbotkt.commands.slash.SlashCommandHandler
import pt.paulinoo.dbotkt.commands.slash.StatsSlashCommand
import pt.paulinoo.dbotkt.config.GuildSettingsStore
import pt.paulinoo.dbotkt.player.audio.AudioManager
import pt.paulinoo.dbotkt.player.audio.LavaAudioManager
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
import java.nio.file.Path

object ServiceLocator {
    val guildSettings: GuildSettingsStore by lazy {
        val file = System.getenv("GUILD_SETTINGS_FILE")?.takeIf { it.isNotBlank() } ?: "data/guild-settings.json"
        GuildSettingsStore(Path.of(file))
    }

    private val cooldownManager: CooldownManager by lazy {
        val seconds = System.getenv("COMMAND_COOLDOWN_SECONDS")?.toLongOrNull()?.coerceAtLeast(0) ?: 2L
        CooldownManager(seconds * 1000)
    }

    val audioManager: AudioManager by lazy { LavaAudioManager(guildSettings) }

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
                EqualizerCommand(audioManager),
                PrefixCommand(guildSettings),
                SettingsCommand(guildSettings),
                ServersCommand(),
            ),
            guildSettings,
            cooldownManager,
        )
    }

    val slashCommandHandler: SlashCommandHandler by lazy {
        SlashCommandHandler(
            listOf(
                HelpSlashCommand(),
                StatsSlashCommand(audioManager),
                PingSlashCommand(),
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
}
