plugins {
    kotlin("jvm") version "2.1.0"
    id("com.gradleup.shadow") version "8.3.2"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "me.honkling"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://jitpack.io/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    mavenLocal()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    implementation(kotlin("reflect"))
    implementation("com.github.honkling:commonlib:0782646294")
    implementation("com.github.honkling:pocket:b647474ec0")
    implementation("me.honkling.commando:spigot:3.0.0-Build12") {
        exclude(group = "org.jetbrains")
    }
    implementation("cc.ekblad:4koma:1.2.0")
    compileOnly("com.github.retrooper:packetevents-spigot:2.7.0")
    implementation("org.jetbrains.kotlin:kotlin-scripting-common:2.1.0")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:2.1.0")
    implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies:2.1.0")
    implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies-maven:2.1.0")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:2.1.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
}

kotlin {
    jvmToolchain(21)
}

tasks.jar {
    manifest {
        attributes("paperweight-mappings-namespace" to "mojang")
    }
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.runServer {
    minecraftVersion("1.21.1")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
