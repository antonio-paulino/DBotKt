# DBotKT

A Kotlin Discord bot project using Lavaplayer for audio playback and Spotify integration.

## TODO
- [ ] Make the bot more configurable via environment variables.
- [ ] Improve error handling and logging.
- [ ] Add spotify lyrics support.

## Features

- Discord bot built with JDA
- Audio playback using Lavaplayer
- Spotify playlist and track metadata support via Lavasrc
- Player statistics reporting
- Graceful shutdown support

## Requirements

- Java 21 or higher
- Gradle
- Discord bot token
- Spotify API credentials (Client ID & Secret)
- yt-dlp

## Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/antonio-paulino/DBotKt.git
   ```
   ```bash
   cd DBotKt
   ```

2. **Configure environment variables:**
    - `DISCORD_TOKEN`: Your Discord bot token
    - `SPOTIFY_CLIENT_ID`: Your Spotify client ID
    - `SPOTIFY_CLIENT_SECRET`: Your Spotify client secret
    - `PREFIXES`: Command prefixes for the bot separated by a space (e.g., `! ?`)
    - `ADMIN_IDS`: Comma-separated list of Discord user IDs with admin privileges
    - `YTDLP_PATH`: Path to yt-dlp executable for improved YouTube support

3. **Build the project:**
   ```bash
   ./gradlew build
   ```

4. **Run the bot:**
   ```bash
   java -jar ./DBotKt-<version>.jar
   ```

## Docker

You can also run the bot using Docker.  
Make sure you have Docker installed.

### Using Prebuilt Image

1. **Run with Docker Compose:**  
   Update the [docker-compose.yml](docker-compose.yml) file to use the prebuilt image:

   ```yaml
   services:
     dbotkt:
       container_name: DBotKT
       restart: always
       image: paulinoo/dbotkt:latest
       environment:
         - SPOTIFY_CLIENT_ID=${SPOTIFY_CLIENT_ID}
         - SPOTIFY_CLIENT_SECRET=${SPOTIFY_CLIENT_SECRET}
         - DISCORD_TOKEN=${DISCORD_TOKEN}
         - YT_REFRESH_TOKEN=${YT_REFRESH_TOKEN}
         - PREFIXES=! .
         - ADMIN_IDS=${ADMIN_IDS}
         - YTDLP_PATH=${YTDLP_PATH}
   ```
   Then start the container:

   ```bash
    docker compose up -d
   ```

2. **Or run with Docker CLI:**
   ```bash
   docker run -d \
     --name DBotKT \
     --restart always \
     -e DISCORD_TOKEN=${DISCORD_TOKEN} \
     -e SPOTIFY_CLIENT_ID=${SPOTIFY_CLIENT_ID} \
     -e SPOTIFY_CLIENT_SECRET=${SPOTIFY_CLIENT_SECRET} \
     -e YT_REFRESH_TOKEN=${YT_REFRESH_TOKEN} \
     -e PREFIXES="! ." \
     -e ADMIN_IDS=${ADMIN_IDS} \
     -e YTDLP_PATH=${YTDLP_PATH} \
     paulinoo/dbotkt:latest
   ```
   If you want to build the image yourself, you can use the provided [Dockerfile](Dockerfile):
   ```bash
    docker build -t dbotkt .
   ```
   Make sure to set the environment variables as needed.
    
## Usage

- Use Discord commands to play tracks or playlists from supported sources.
- Use the stats command to get Lavaplayer statistics.

## Development

- Main code is in `src/main/kotlin/`
- Gradle is used for dependency management.
- Update dependencies as needed in `build.gradle.kts`.

## Troubleshooting

- If Spotify playback fails, ensure your credentials are correct and the handler is up to date.
- For Lavaplayer errors, check for library updates or compatibility issues.

## Contributing
Contributions are welcome! Please fork the repository and submit a pull request with your changes.

## Credits
- [JDA](https://github.com/discord-jda/JDA)
- [Lavaplayer](https://github.com/lavalink-devs/lavaplayer)
- [Lavasrc](https://github.com/topi314/LavaSrc)
- [Youtube-source](https://github.com/lavalink-devs/youtube-source)
- [udpqueue](https://github.com/MinnDevelopment/udpqueue.rs)
- [yt-dlp](https://github.com/yt-dlp/yt-dlp)
- [yt-cipher](https://github.com/kikkia/yt-cipher/)

## License

See [LICENSE](LICENSE) for details.