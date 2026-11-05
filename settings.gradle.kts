pluginManagement {
    repositories {
        maven("https://maven.cleanroommc.com")
        gradlePluginPortal()
    }
}

plugins {
    id("com.cleanroommc.cleanroomgradle.settings") version "0.23.0"
    // Downloads the Java toolchain when the local JDK does not match
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "EndReforked"

// Dependencies: impl -> api, wire; server -> impl; client -> server; mod -> all. Nothing depends on client or mod.
include("api", "wire", "impl", "server", "client", "mod")
