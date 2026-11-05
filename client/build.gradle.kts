// Renderers, models, GUIs, particles, JEI and client handlers. Depends on server, never the other way round.
// CleanroomGradle must be applied here too: it makes the Minecraft classpath resolvable in this project.
plugins {
    `java-library`
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("com.cleanroommc.cleanroomgradle")
}

val cleanroom_version = project.property("cleanroom_version") as String

dependencies {
    // The runtime is shaded into the mod jar by :mod
    compileOnly("com.google.dagger:dagger:2.60.1")
    ksp("com.google.dagger:dagger-compiler:2.60.1")

    implementation(cleanroom.userdev(cleanroom_version))
    api(project(":server"))
}
