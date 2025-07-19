# Stage 1: Download dependencies
FROM debian:bookworm-slim AS downloader

RUN apt-get update && \
    apt-get install -y --no-install-recommends curl ca-certificates && \
    curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux -o /yt-dlp && \
    chmod +x /yt-dlp && \
    apt-get purge -y curl && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/*

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine AS final

WORKDIR /app

# Install ffmpeg & ffprobe from mwader's static build
COPY --from=mwader/static-ffmpeg:7.1 /ffmpeg /usr/local/bin/
COPY --from=mwader/static-ffmpeg:7.1 /ffprobe /usr/local/bin/

# Copy yt-dlp binary
COPY --from=downloader /yt-dlp /usr/local/bin/yt-dlp

# Copy your compiled jar
COPY DBotKt.jar .

# Run your app
CMD ["java", "-jar", "DBotKt.jar"]
