package pt.paulinoo.dbotkt.health

import com.sun.net.httpserver.HttpServer
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

/**
 * Tiny dependency-free HTTP server exposing the bot's health for uptime monitoring and
 * container healthchecks. Built on the JDK's [HttpServer] — no extra footprint.
 *
 * - `GET /health` returns 200 with the full stats JSON when the gateway is connected,
 *   503 (same body) otherwise, so a `HEALTHCHECK` can detect an unhealthy bot.
 * - [healthyJsonProvider] returns `(healthy, json)`.
 */
class HealthServer(
    private val port: Int,
    private val healthyJsonProvider: () -> Pair<Boolean, String>,
) {
    private val logger = LoggerFactory.getLogger(HealthServer::class.java)
    private var server: HttpServer? = null

    /** The actually-bound port (useful when constructed with port 0 for an ephemeral port). */
    val boundPort: Int
        get() = server?.address?.port ?: -1

    fun start() {
        val httpServer = HttpServer.create(InetSocketAddress(port), 0)
        httpServer.createContext("/health") { exchange ->
            try {
                val (healthy, json) = healthyJsonProvider()
                val bytes = json.toByteArray(StandardCharsets.UTF_8)
                exchange.responseHeaders.add("Content-Type", "application/json; charset=utf-8")
                exchange.sendResponseHeaders(if (healthy) 200 else 503, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            } catch (e: Exception) {
                logger.warn("Health request failed", e)
                val bytes = "{\"healthy\":false}".toByteArray(StandardCharsets.UTF_8)
                exchange.sendResponseHeaders(500, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
        }
        httpServer.executor = null
        httpServer.start()
        server = httpServer
        logger.info("Health endpoint listening on :{}/health", port)
    }

    fun stop() {
        server?.stop(0)
        server = null
    }
}
