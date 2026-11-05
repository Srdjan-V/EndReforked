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
    shade("com.google.dagger:dagger:2.60.1") {
        exclude(group = "org.jspecify") // annotations only
    }

    // Kotlin stdlib comes from Forgelin-Continuous, the annotations are not needed at runtime
    shade("com.squareup.wire:wire-runtime:${project.property("wireVersion")}") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains", module = "annotations")
    }

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

// Adds the shaded libraries to the jar. The reobfJar task is wired to this jar instead of the plain one
tasks.shadowJar {
    configurations = listOf(shade)
    from(tasks.jar.map { zipTree(it.archiveFile) })
    relocate("dagger", "$modGroup.shadow.dagger")
    relocate("javax.inject", "$modGroup.shadow.javax.inject")
    relocate("jakarta.inject", "$modGroup.shadow.jakarta.inject")
    relocate("com.squareup.wire", "$modGroup.shadow.wire")
    relocate("okio", "$modGroup.shadow.okio")
    archiveClassifier = "mcp-shaded"
    addMultiReleaseAttribute = false
    // dagger.internal.Provider extends javax.inject.Provider, Scope is a meta-annotation of dagger.Reusable, the rest is unused
    exclude("javax/inject/Inject.class", "javax/inject/Named.class", "javax/inject/Qualifier.class")
    exclude("javax/inject/Singleton.class")
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
