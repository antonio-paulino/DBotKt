package pt.paulinoo.dbotkt

import pt.paulinoo.dbotkt.bot.DiscordBot
import java.util.TimeZone

fun main() {
    // Apply the configured time zone before anything logs, so every timestamp (including
    // slf4j-simple's) reflects it. On Linux the JVM already reads TZ, but setting it
    // explicitly also covers other platforms and makes the intent clear.
    System.getenv("TZ")?.takeIf { it.isNotBlank() }?.let {
        TimeZone.setDefault(TimeZone.getTimeZone(it))
    }

    val bot = DiscordBot()

    Runtime.getRuntime().addShutdownHook(
        Thread {
            println("Shutting down...")
            bot.shutdown()
        },
    )
}
