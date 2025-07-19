FROM openjdk:21-jdk-slim-buster

WORKDIR /app

# Install Python3 and pip
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

RUN curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux -o /usr/local/bin/yt-dlp && \
    chmod a+rx /usr/local/bin/yt-dlp

COPY --from=mwader/static-ffmpeg:7.1 /ffmpeg /usr/local/bin/
COPY --from=mwader/static-ffmpeg:7.1 /ffprobe /usr/local/bin/

# Copy your jar file into the container
COPY DBotKt.jar .

# Run the jar file
CMD ["java", "-jar", "DBotKt.jar"]