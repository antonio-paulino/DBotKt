package pt.paulinoo.dbotkt.commands

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.components.buttons.Button
import pt.paulinoo.dbotkt.embed.Embed
import pt.paulinoo.dbotkt.embed.EmbedLevel

class HelpSlashCommand : SlashCommand {
    override val name: String = "help"

    override fun getCommandData(): SlashCommandData {
        return Commands.slash(name, "Shows the help menu")
    }

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val helpMessage =
            """
            Here are the commands you can use:
            - `!play <song || youtube link || spotify link>`: Plays a song or playlist.
            - `!pause`: Pauses the current song.
            - `!resume`: Resumes the paused song.
            - `!skip`: Skips the current song.
            - `!skipto <number>`: Skips to the specified song in the queue.
            - `!stop`: Stops the current song and clears the queue.
            - `!queue`: Displays the current song queue.
            - `!clearqueue`: Clears the current song queue.
            - `!volume <1-200>`: Sets the volume of the music player.
            - `!reverse`: Reverses the current song queue.
            - `!shuffle`: Shuffles the current song queue.
            - `!swap <index1> <index2>`: Swaps two songs in the queue.
            - `!remove <index>`: Removes a song from the queue by its index.
            - `!lyrics`: Displays the lyrics of the currently playing song.
            """.trimIndent()

        val embed =
            Embed.create(
                title = "Help Menu",
                description = helpMessage,
                level = EmbedLevel.INFO,
            ).build()

        val deleteEmoji = Emoji.fromUnicode("U+274C")

        event.replyEmbeds(
            embed,
        ).setActionRow(
            Button.secondary(
                "button_delete",
                deleteEmoji,
            ),
        ).queue()
    }
}
