import java.util.UUID
import com.cleanroommc.gradle.api.task.mc.RunMinecraft

// Assembles the one mod jar and owns the run tasks.
plugins {
    `java-library`
    kotlin("jvm")
    id("com.cleanroommc.cleanroomgradle")
}

val cleanroom_version = project.property("cleanroom_version") as String
val minecraftUsername = project.property("minecraftUsername") as String
val coreModClass = project.property("coreModClass") as String
val modProjects = listOf(":api", ":wire", ":impl", ":server", ":client")

base.archivesName = "EndReforked"

dependencies {
    implementation(cleanroom.userdev(cleanroom_version))
    modProjects.forEach { implementation(project(it)) }

    // Kotlin runtime on Cleanroom. Cleanroom 0.7.x does not ship the Kotlin stdlib.
    runtimeOnly(cleanroom.deobf("maven.modrinth:forgelin-continuous:2.4.20.0"))
}

// JER or TiC bring in the old JEI 4.16 transitively, which clashes with HEI/JEI 4.36
configurations.configureEach { exclude(group = "mezz.jei") }

// A fixed UUID avoids the Mojang name lookup of CleanroomGradle, which fails on a non-JSON reply
val devUuid = UUID.nameUUIDFromBytes("OfflinePlayer:$minecraftUsername".toByteArray()).toString()
cleanroom {
    runs {
        maybeCreate("client").configure(RunMinecraft::class.java) {
            username.set(minecraftUsername)
            getUUID().set(devUuid)
            jvmArgs("-Dfml.coreMods.load=$coreModClass")
        }
        maybeCreate("server").configure(RunMinecraft::class.java) {
            getUUID().set(devUuid)
            jvmArgs("-Dfml.coreMods.load=$coreModClass")
        }
    }
}

// The reobfJar task renames this jar from MCP to SRG names
tasks.jar {
    modProjects.forEach { from(project(it).extensions.getByType<SourceSetContainer>()["main"].output) }
    duplicatesStrategy = DuplicatesStrategy.FAIL
    manifest {
        attributes(
            "FMLCorePlugin" to coreModClass,
            "FMLCorePluginContainsFMLMod" to true,
            "ForceLoadAsMod" to true,
        )
    }
}

// Taken from CleanroomModTemplate (marked there as a Foundation issue). Without it, mods on the dev
// classpath are loaded by the app class loader and fail (SidedProxy is null, coremods cannot be cast).
tasks.withType<JavaExec>().configureEach {
    systemProperty("foundation.loadsall", "true")
}
