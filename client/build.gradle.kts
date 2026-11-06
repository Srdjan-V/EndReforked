// Renderers, models, GUIs, particles, JEI and client handlers. Depends on server, never the other way round.
// CleanroomGradle must be applied here too: it makes the Minecraft classpath resolvable in this project.
plugins {
    `java-library`
    kotlin("jvm")
    id("com.cleanroommc.cleanroomgradle")
}

val cleanroom_version = project.property("cleanroom_version") as String

dependencies {
    implementation(cleanroom.userdev(cleanroom_version))
    api(project(":server"))
}

// In the dev run each project is a separate jar, and Cleanroom scans only the jars that declare a mod id
// (mcmod.info or @Mod). Without one, the annotations of this jar (@JEIPlugin, ...) are never discovered.
// The built mod jar is one jar, so this file goes into the client jar only, not into the main source set.
tasks.jar {
    from(resources.text.fromString("""[{"modid": "endreborn_client"}]""")) { rename { "mcmod.info" } }
}
