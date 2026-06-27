# syntax=docker/dockerfile:1

# ---- Build stage: compile the fat jar with the full JDK ----
FROM eclipse-temurin:25-jdk AS build
# Provided automatically by buildx; used to keep one Gradle cache per target arch so
# concurrent multi-platform builds don't fight over the same cache lock.
ARG TARGETARCH
WORKDIR /src

COPY . .

# Cache the Gradle home and project cache across builds so only changed sources recompile.
RUN --mount=type=cache,target=/root/.gradle,id=gradle-home-$TARGETARCH \
    --mount=type=cache,target=/src/.gradle,id=gradle-project-$TARGETARCH \
    chmod +x gradlew && ./gradlew --no-daemon clean uberJar

# ---- Runtime stage: small JRE image with only what playback needs ----
FROM eclipse-temurin:25-jre
WORKDIR /app

# ffmpeg + python3 are required by yt-dlp; curl only to fetch the yt-dlp binary.
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        ffmpeg \
        python3 \
        ca-certificates \
        curl && \
    curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp && \
    chmod a+rx /usr/local/bin/yt-dlp && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

COPY --from=build /src/DBotKt.jar .

CMD ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "DBotKt.jar"]
