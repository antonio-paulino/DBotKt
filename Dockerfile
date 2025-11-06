FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

RUN apt-get update && \
    apt-get install -y --no-install-recommends ffmpeg python3 ca-certificates curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

RUN curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp && \
    chmod a+rx /usr/local/bin/yt-dlp

COPY DBotKt.jar .

CMD ["java", "-jar", "DBotKt.jar"]
