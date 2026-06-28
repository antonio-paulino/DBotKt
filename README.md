# DBotKT

A high-quality, low-footprint Discord music bot written in Kotlin, built on JDA and
Lavaplayer. It is designed to run across many guilds at once while keeping audio quality
at the maximum Discord allows (48 kHz Opus, high-quality resampling).

## Features

- Discord bot built with **JDA** (with native voice sending via **udpqueue**/**jdave**, incl. DAVE E2E voice)
- Audio playback using **Lavaplayer**, robust YouTube support via **youtube-source**
- **Spotify** playlist/track support via **LavaSrc** (mirrored for playback)
- **Optional `yt-dlp` backend** — when enabled it becomes the primary YouTube source, with the native source as fallback
- **Interactive player message** with buttons (pause, skip, stop, queue, volume, shuffle, loop, clear, lyrics)
- **Equalizer** with presets (Flat, Bass Boost, Treble Boost, Pop, Rock, Jazz, Classical, Loudness) via a select menu or `!eq`, using Lavaplayer's native 15-band PCM equalizer (Flat = zero added CPU)
- **Lyrics** via [LRCLIB](https://lrclib.net) with a **pick-from-results** menu — choose the exact version (synced or plain)
- Loop modes (single / queue), shuffle, reverse, swap, remove, skip-to, paginated queue
- **Persisted per-server settings** (prefix, volume, equalizer) restored on restart
- Per-user command **cooldown** to throttle spam
- **Health endpoint** (`GET /health`) + Docker `HEALTHCHECK`, and cumulative metrics (tracks played, commands, errors)
- Admin health panels (`!stats`, `/stats`, `/ping`)
- Auto-leave immediately when the voice channel is empty, or after 5 minutes of idle queue
- Graceful shutdown

## Requirements

- Java 25 or higher
- Gradle (wrapper included)
- A Discord bot token
- Spotify API credentials (Client ID & Secret)
- *(optional)* `yt-dlp` binary — only if you want to use the yt-dlp backend
- *(optional)* a [yt-cipher](https://github.com/kikkia/yt-cipher/) instance — only if you want remote signature solving

## Configuration

Environment variables:

| Variable | Required | Default | Description |
| --- | --- | --- | --- |
| `DISCORD_TOKEN` | ✅ | — | Discord bot token |
| `SPOTIFY_CLIENT_ID` | ✅ | — | Spotify client ID |
| `SPOTIFY_CLIENT_SECRET` | ✅ | — | Spotify client secret |
| `PREFIXES` | ✅ | — | Command prefixes, space-separated (e.g. `! .`) |
| `ADMIN_IDS` | ✅ | — | Comma-separated Discord user IDs allowed to use admin commands (`stats`, `servers`) |
| `SPOTIFY_MARKET` | ➖ | `US` | ISO country code used for Spotify lookups |
| `TZ` | ➖ | system / `UTC` | Time zone for log timestamps (e.g. `Europe/Lisbon`) |
| `YTDLP_PATH` | ➖ | — | Path to the `yt-dlp` binary. If set, yt-dlp becomes the primary YouTube backend |
| `YT_REFRESH_TOKEN` | ➖ | — | YouTube OAuth2 refresh token (enables OAuth on the native source) |
| `YT_CIPHER` | ➖ | — | URL of a yt-cipher instance for remote signature solving |
| `YT_CIPHER_PASSWORD` | ➖ | — | Password/token for the yt-cipher instance |
| `HEALTH_PORT` | ➖ | `8080` | Port for the `GET /health` endpoint; set to `off` to disable |
| `COMMAND_COOLDOWN_SECONDS` | ➖ | `2` | Per-user, per-command cooldown to throttle spam (`0` disables) |
| `GUILD_SETTINGS_FILE` | ➖ | `data/guild-settings.json` | Where per-guild settings are persisted |

Required variables must be present or the bot will not start.

> **Note:** The bot will not work properly if you do not provide a Spotify client ID and
> secret, as they are required for Spotify track/playlist lookups. The use of yt-cipher
> (`YT_CIPHER`) is optional but recommended for better YouTube support.

## Commands

Default prefix `!` (configurable via `PREFIXES`):

| Command | Description |
| --- | --- |
| `!play <query \| YouTube/Spotify URL>` | Play a track or playlist (searches YouTube for plain text) |
| `!pause` / `!resume` | Pause / resume playback |
| `!skip` | Skip the current track |
| `!skipto <n>` | Skip to track number `n` in the queue |
| `!stop` | Stop, clear the queue and leave the channel |
| `!queue` | Show the paginated queue |
| `!clearqueue` | Clear the queue |
| `!volume <0-200>` | Set the playback volume |
| `!shuffle` / `!reverse` | Shuffle / reverse the queue |
| `!swap <a> <b>` | Swap two queued tracks |
| `!remove <n>` | Remove track `n` from the queue |
| `!eq <preset>` | Apply an equalizer preset (e.g. `bass_boost`, `rock`, `flat`) — persisted per server |
| `!lyrics` | Show lyrics for the current track, with a menu to pick the version |
| `!settings` | Show this server's settings |
| `!prefix <p\|clear>` | Set or clear a server-specific command prefix (Manage Server) |
| `!stats` | Admin-only health & stats panel (uptime, ping, servers/members, players, totals, memory, CPU, JVM/OS) |
| `!servers` | Admin-only list of connected servers (requires `ADMIN_IDS`) |
| `/help` | Slash command listing the available commands |
| `/stats`, `/ping` | Admin-only ephemeral health & latency (requires `ADMIN_IDS`) |

The persistent player message also exposes buttons and an equalizer/lyrics selector.

Per-server settings (prefix, volume, equalizer) are persisted to disk
([`GUILD_SETTINGS_FILE`](#configuration)) and restored on restart.

## Health & Monitoring

The bot serves a tiny HTTP health endpoint (no extra dependencies) for uptime monitoring
and container healthchecks:

```bash
curl http://localhost:8080/health
```

It returns **200** with a JSON snapshot (gateway ping, uptime, guilds/members, players,
queue, cumulative totals, memory, CPU, threads) when the Discord gateway is connected, and
**503** otherwise. The Docker image declares a `HEALTHCHECK` against it, so orchestrators
can detect and restart an unhealthy bot. Set `HEALTH_PORT=off` to disable.

## Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/antonio-paulino/DBotKt.git
   cd DBotKt
   ```

2. **Configure environment variables** (see [Configuration](#configuration)).

3. **Build the project:**
   ```bash
   ./gradlew build
   ```
   This produces a fat jar `DBotKt.jar` in the project root.

4. **Run the bot:**
   ```bash
   java --enable-native-access=ALL-UNNAMED -jar ./DBotKt.jar
   ```

## Docker

### Using the prebuilt image

Update [docker-compose.yml](docker-compose.yml) with your environment variables, then:

```bash
docker compose up -d
```

The provided compose file also starts a [yt-cipher](https://github.com/kikkia/yt-cipher/)
(`ejs-api`) service. The bundled [Dockerfile](Dockerfile) already installs `yt-dlp`,
`python3` and `ffmpeg`, so setting `YTDLP_PATH=/usr/local/bin/yt-dlp` enables the yt-dlp
backend out of the box.

### Or run with the Docker CLI

```bash
docker run -d \
  --name DBotKT \
  --restart always \
  -e DISCORD_TOKEN=${DISCORD_TOKEN} \
  -e SPOTIFY_CLIENT_ID=${SPOTIFY_CLIENT_ID} \
  -e SPOTIFY_CLIENT_SECRET=${SPOTIFY_CLIENT_SECRET} \
  -e SPOTIFY_MARKET=US \
  -e PREFIXES="! ." \
  -e ADMIN_IDS=${ADMIN_IDS} \
  -e EMPTY_CHANNEL_TIMEOUT_SECONDS=60 \
  -e YTDLP_PATH=/usr/local/bin/yt-dlp \
  -e YT_REFRESH_TOKEN=${YT_REFRESH_TOKEN} \
  -e YT_CIPHER=${YT_CIPHER} \
  -e YT_CIPHER_PASSWORD=${YT_CIPHER_PASSWORD} \
  paulinoo/dbotkt:latest
```

### Local testing with only the yt-cipher service

To run the bot locally (e.g. from your IDE) against just the signature service:

```bash
docker compose -f docker-compose.cipher.yml up
```

It exposes the service on `http://localhost:8001`; point the bot at it with
`YT_CIPHER=http://localhost:8001` and `YT_CIPHER_PASSWORD=test`.

## Testing

```bash
./gradlew test
```

The suite mixes deterministic unit tests with real integration tests against the live
LRCLIB API. The integration tests retry and **skip** (rather than fail) when the network
is unavailable, so the build stays green offline.

## Development

- Main code is in `src/main/kotlin/`, tests in `src/test/kotlin/`.
- Gradle manages dependencies; update them in [build.gradle.kts](build.gradle.kts).
- Code style is enforced with ktlint: `./gradlew ktlintCheck` (auto-fix with `ktlintFormat`).

## Troubleshooting

- **Bot won't start:** verify the required environment variables are set.
- **No YouTube playback:** try enabling `YT_CIPHER` (remote signatures) or `YTDLP_PATH` (yt-dlp backend).
- **No lyrics found:** LRCLIB matches by title + artist; obscure or mistitled tracks may have no entry. Use the version menu to pick an alternative.

## Credits

- [JDA](https://github.com/discord-jda/JDA)
- [Lavaplayer](https://github.com/lavalink-devs/lavaplayer)
- [LavaSrc](https://github.com/topi314/LavaSrc)
- [LavaLyrics](https://github.com/topi314/LavaSrc) / [LRCLIB](https://lrclib.net)
- [youtube-source](https://github.com/lavalink-devs/youtube-source)
- [udpqueue](https://github.com/MinnDevelopment/udpqueue.rs)
- [jdave](https://github.com/MinnDevelopment/jdave)
- [yt-dlp](https://github.com/yt-dlp/yt-dlp)
- [yt-cipher](https://github.com/kikkia/yt-cipher/)

## License

See [LICENSE](LICENSE) for details.
