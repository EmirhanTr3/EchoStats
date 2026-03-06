plugins {
    kotlin("jvm") version "2.3.20-RC"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("de.eldoria.plugin-yml.paper") version "0.7.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

group = "cat.emir"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("1.21.10-R0.1-SNAPSHOT")
    paperLibrary("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

tasks {
    runServer {
        minecraftVersion("1.21.6")
    }

    jar.get().enabled = false

    build {
        dependsOn("shadowJar")
    }

    shadowJar {
        archiveClassifier = ""
    }
}

paper {
    authors = listOf("EmirhanTr3")
    description = "Display item statistics in lore."
    website = "https://github.com/EmirhanTr3/EchoStats"
    main = "cat.emir.echostats.EchoStats"
    loader = "cat.emir.echostats.load.LibraryLoader"
    apiVersion = "1.21.10"

    // Keep this on!
    generateLibrariesJson = true

    serverDependencies {
    }
}

kotlin {
    jvmToolchain(21)
}