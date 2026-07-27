pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/") { name = "PaperMC" }
    }
}

// Lets Gradle auto-provision the JDK 21 toolchain when the build runs on a different
// JDK (e.g. the JDK 25 release job that also builds the mod).
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "simple-frames-plugin"
