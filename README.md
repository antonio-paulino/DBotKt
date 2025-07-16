# DBotKT

A Kotlin Discord bot project using Lavaplayer for audio playback and Spotify integration.

## TODO
- [ ] Finish implementing loop, queue, clear and lyrics commands and it's buttons.
- [ ] Add command restrictions based on owner id (stats and servers).
- [ ] Make the bot more configurable via environment variables.
- [ ] Add support for more audio sources.
- [ ] Add more commands and features.
- [ ] Improve error handling and logging.

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

## Setup

1. **Clone the repository:**
   ```
   git clone https://github.com/antonio-paulino/DBotKt.git
   ```
   ```
   cd DBotKt
   ```

2. **Configure environment variables `.env` file:**
    - `DISCORD_TOKEN`: Your Discord bot token
    - `SPOTIFY_CLIENT_ID`: Your Spotify client ID
    - `SPOTIFY_CLIENT_SECRET`: Your Spotify client secret
    - `YT_REFRESH_TOKEN`: Your YouTube refresh token
    - `PREFIXES`: Command prefixes for the bot separated by a space (e.g., `! ?`)

3. **Build the project:**
   ```
   ./gradlew build
   ```

4. **Run the bot:**
   ```
   java -jar ./DBotKt-<version>.jar
   ```

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

## License

See `LICENSE` for details.