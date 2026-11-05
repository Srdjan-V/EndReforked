// Side-neutral logic. Plain Kotlin/JVM library, no Minecraft dependency.
plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.4.20"
}

val kotlinVersion = "2.4.20"
// Exactly the version that Forgelin-Continuous 2.4.20.0 ships (its class files are byte-identical to 1.12.0-RC)
val serializationVersion = "1.12.0-RC"

dependencies {
    api(project(":api"))
    api(project(":wire"))

    // The runtime comes from the Forgelin-Continuous mod, so it is compile-only (like the Kotlin stdlib)
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test { useJUnitPlatform() }
