plugins {
    kotlin("jvm") version "2.2.20-Beta2"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
}

group = "me.honkling"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.evokegames.gg/snapshots")
    maven("https://jitpack.io/")
    mavenLocal()
}

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
    implementation("me.tofaa.entitylib:spigot:+d4c3b1e-SNAPSHOT")
    implementation("io.github.honkling.commando:spigot:3.0.4")
    implementation("com.github.honkling:4koma-regex:1.3.0")
    implementation("dev.kord:kord-core:0.15.0")
    implementation(kotlin("reflect"))

    compileOnly("com.github.honkling:ruby:develop")
    compileOnly("com.github.retrooper:packetevents-spigot:2.9.3")
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21.4")
    }

    jar {
        manifest {
            attributes("paperweight-mappings-namespace" to "mojang")
        }
    }

    build {
        dependsOn("shadowJar")
    }

    shadowJar {
        dependencies {
            exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
            exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk7"))
            exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk8"))
            exclude(dependency("org.jetbrains.kotlin:kotlin-reflect"))
        }
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

kotlin {
    jvmToolchain(21)
}
