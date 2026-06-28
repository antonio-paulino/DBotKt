package pt.paulinoo.dbotkt.health

import java.net.HttpURLConnection
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HealthServerTest {
    private fun get(port: Int): Pair<Int, String> {
        val connection = URI("http://localhost:$port/health").toURL().openConnection() as HttpURLConnection
        return try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            code to (stream?.bufferedReader()?.use { it.readText() } ?: "")
        } finally {
            connection.disconnect()
        }
    }

    @Test
    fun serves200WithJsonWhenHealthy() {
        val server = HealthServer(0) { true to "{\"healthy\":true}" }
        server.start()
        try {
            val (code, body) = get(server.boundPort)
            assertEquals(200, code)
            assertTrue(body.contains("\"healthy\":true"))
        } finally {
            server.stop()
        }
    }

    @Test
    fun serves503WhenUnhealthy() {
        val server = HealthServer(0) { false to "{\"healthy\":false}" }
        server.start()
        try {
            val (code, _) = get(server.boundPort)
            assertEquals(503, code)
        } finally {
            server.stop()
        }
    }
}
