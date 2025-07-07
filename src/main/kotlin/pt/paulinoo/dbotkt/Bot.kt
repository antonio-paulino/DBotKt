package pt.paulinoo.dbotkt

import pt.paulinoo.dbotkt.bot.DiscordBot

fun main() {
    val bot = DiscordBot()

    Runtime.getRuntime().addShutdownHook(
        Thread {
            println("Shutting down...")
            bot.shutdown()
        }
    )
}
