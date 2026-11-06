// Public types, side-neutral. Plain Kotlin/JVM library that may use Minecraft and Forge types.
// CleanroomGradle must be applied here too: it makes the Minecraft classpath resolvable in this project.
plugins {
    `java-library`
    kotlin("jvm")
    id("com.cleanroommc.cleanroomgradle")
}

val cleanroom_version = project.property("cleanroom_version") as String

dependencies {
    // Only for compiling and tests. The mod jar takes the class output of each project, not its dependencies.
    api(cleanroom.userdev(cleanroom_version))
}
