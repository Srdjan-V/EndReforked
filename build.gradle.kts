import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    // Same version as the Kotlin stdlib that Forgelin-Continuous 2.4.20.0 ships
    kotlin("jvm") version "2.4.20" apply false
}

val kotlinVersion = "2.4.20"
val modGroup = project.property("modGroup") as String
val modVersion = project.property("modVersion") as String

group = modGroup
version = modVersion

// Shared config. Each subproject applies its own plugins in its own plugins block (for type-safe accessors).
subprojects {
    group = modGroup
    version = modVersion

    repositories {
        exclusiveContent {
            forRepository { maven("https://curse.cleanroommc.com") { name = "CurseMaven" } }
            filter { includeGroup("curse.maven") }
        }
        exclusiveContent {
            forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
            filter { includeGroup("maven.modrinth") }
        }
        exclusiveContent {
            forRepository { maven("https://maven.cleanroommc.com") { name = "CleanroomMC" } }
            filter { includeGroup("mezz") }
        }
        maven("https://maven.cleanroommc.com") {
            name = "CleanroomMCModules" // GroovyScript, the optional script engine
            content { includeModule("com.cleanroommc", "groovyscript") }
        }
        mavenCentral()
    }

    plugins.withType<JavaPlugin> {
        extensions.configure<BasePluginExtension> { archivesName = "EndReforked-${project.name}" }
        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion = JavaLanguageVersion.of(25)
        }
        tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }
        // The Kotlin runtime is provided by the Forgelin-Continuous mod (see :mod), so it is compile-only here
        dependencies.add("compileOnly", "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    }
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        extensions.configure<KotlinJvmProjectExtension> { jvmToolchain(25) }
    }
}
