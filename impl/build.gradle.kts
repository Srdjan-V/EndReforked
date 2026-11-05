// Side-neutral logic. Plain Kotlin/JVM library, no Minecraft dependency yet.
plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    api(project(":api"))
    api(project(":wire"))
}
