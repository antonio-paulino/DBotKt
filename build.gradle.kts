plugins {
    kotlin("jvm") version "2.1.21"
    id("org.jlleitschuh.gradle.ktlint") version "12.3.0"
}

group = "pt.paulinoo"
version = "0.1"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.lavalink.dev/releases")
    maven("https://maven.topi.wtf/releases")
    maven("https://maven.lavalink.dev/snapshots")
}

dependencies {
    implementation("com.github.topi314.lavasrc:lavasrc:d8cba39")
    implementation("com.github.topi314.lavasrc:lavasrc-protocol:4.7.2")
    implementation("dev.arbjerg:lavaplayer:2.2.4")
    implementation("dev.lavalink.youtube:v2:1.13.3")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
    implementation("net.dv8tion:JDA:5.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("se.michaelthelin.spotify:spotify-web-api-java:9.2.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

tasks.register<Copy>("copyRuntimeDependencies") {
    into("build/libs")
    from(configurations.runtimeClasspath)
}
private val outputName = "DBotKt"
tasks.register<Jar>("uberJar") {
    dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources"))
    manifest {
        attributes["Main-Class"] = "pt.paulinoo.dbotkt.BotKt"
    }
    archiveBaseName.set(outputName)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    destinationDirectory.set(file("."))
}

tasks {
    build {
        dependsOn("uberJar")
    }
    clean {
        doLast {
            file("$outputName.jar").delete()
        }
    }
}
