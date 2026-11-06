import java.util.UUID
import com.cleanroommc.gradle.api.task.mc.RunMinecraft
import net.minecraftforge.renamer.gradle.RenameJar

// Assembles the one mod jar and owns the run tasks.
plugins {
    `java-library`
    kotlin("jvm")
    id("com.cleanroommc.cleanroomgradle")
    id("com.gradleup.shadow") version "9.2.2"
}

val cleanroom_version = project.property("cleanroom_version") as String
val minecraftUsername = project.property("minecraftUsername") as String
val modGroup = project.property("modGroup") as String
val coreModClass = project.property("coreModClass") as String
val modProjects = listOf(":api", ":wire", ":impl", ":server", ":client")

base.archivesName = "EndReforked"

// The dev run uses the unshaded runtime, only the built jar is relocated
val shade by configurations.creating
configurations.runtimeOnly { extendsFrom(shade) }

dependencies {
    implementation(cleanroom.userdev(cleanroom_version))
    modProjects.forEach { implementation(project(it)) }

    // Shaded and relocated into the mod jar, see shadowJar
    // Kotlin stdlib comes from Forgelin-Continuous, the annotations are not needed at runtime
    shade("com.squareup.wire:wire-runtime:${project.property("wireVersion")}") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains", module = "annotations")
    }

    // Kotlin runtime on Cleanroom. Cleanroom 0.7.x does not ship the Kotlin stdlib.
    runtimeOnly(cleanroom.deobf("maven.modrinth:forgelin-continuous:2.4.20.0"))

    // Optional mod for scripts, in the dev runs only (not shaded, not a dependency of the jar).
    // Not needed to load it: CodeChickenLib, TOP, and two Cleanroom helper mods (one fails to apply its mixin).
    // MixinBooter is built into Cleanroom, a second copy crashes the launch.
    runtimeOnly("com.cleanroommc:groovyscript:${project.property("groovyScriptVersion")}") {
        exclude(group = "codechicken")
        exclude(group = "curse.maven")
        exclude(group = "zone.rong")
        exclude(group = "com.cleanroommc")
    }
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

// Adds the shaded libraries to the jar. The reobfJar task is wired to this jar instead of the plain one
tasks.shadowJar {
    configurations = listOf(shade)
    from(tasks.jar.map { zipTree(it.archiveFile) })
    relocate("com.squareup.wire", "$modGroup.shadow.wire")
    relocate("okio", "$modGroup.shadow.okio")
    archiveClassifier = "mcp-shaded"
    addMultiReleaseAttribute = false
    exclude("META-INF/*.kotlin_module", "META-INF/versions/**")
}
tasks.named<RenameJar>("reobfJar") {
    from(tasks.shadowJar)
    archiveClassifier = ""
}

// Taken from CleanroomModTemplate (marked there as a Foundation issue). Without it, mods on the dev
// classpath are loaded by the app class loader and fail (SidedProxy is null, coremods cannot be cast).
tasks.withType<JavaExec>().configureEach {
    systemProperty("foundation.loadsall", "true")
}
