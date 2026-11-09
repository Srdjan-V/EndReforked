import com.cleanroommc.gradle.api.task.mc.RunMinecraft
import net.minecraftforge.renamer.gradle.RenameJar
import java.util.*

plugins {
    `java-library`
    kotlin("jvm") version "2.4.20"
    id("com.cleanroommc.cleanroomgradle")
    id("com.cleanroommc.conventions") version "1.4.0"
    id("com.squareup.wire") version "7.1.2"
    id("com.gradleup.shadow") version "9.2.2"
}

val kotlinVersion = "2.4.20"
val cleanroomVersion = property("cleanroom_version") as String
val modGroup = property("modGroup") as String
val modVersion = property("modVersion") as String
val modId = property("modId") as String
val modName = property("modName") as String
val minecraftUsername = property("minecraftUsername") as String
val coreModClass = property("coreModClass") as String
group = modGroup
base.archivesName = "EndReforked"

repositories {
    exclusiveContent { forRepository { maven("https://curse.cleanroommc.com") }; filter { includeGroup("curse.maven") } }
    exclusiveContent { forRepository { maven("https://api.modrinth.com/maven") }; filter { includeGroup("maven.modrinth") } }
    exclusiveContent { forRepository { maven("https://maven.cleanroommc.com") }; filter { includeGroup("mezz") } }
    maven("https://maven.cleanroommc.com") { content { includeModule("com.cleanroommc", "groovyscript") } }
    mavenCentral()
}
java.toolchain.languageVersion = JavaLanguageVersion.of(25)
kotlin { jvmToolchain(25) }
tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }
val shade = configurations.create("shade")

fun unsigned(notation: String): FileCollection {
    val name = notation.split(':')[1]
    val signed = configurations.create("signed-$name") { isTransitive = false }
    dependencies.add(signed.name, cleanroom.deobf(notation))
    val archives = objects.newInstance<ArchiveOps>().archives
    val unsign = tasks.register<Zip>("unsign-$name") {
        archiveFileName.set("$name-deobf-unsigned.jar")
        destinationDirectory.set(layout.buildDirectory.dir("unsigned"))
        from(signed.elements.map { jars -> jars.map { archives.zipTree(it) } }) {
            exclude("META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA", "META-INF/*.EC")
        }
    }
    return files(unsign)
}

interface ArchiveOps {
    @get:javax.inject.Inject val archives: ArchiveOperations
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    compileOnly("org.jetbrains:annotations:24.1.0")

    runtimeOnly(cleanroom.deobf("maven.modrinth:forgelin-continuous:2.4.20.0"))


    implementation(cleanroom.userdev(cleanroomVersion))

    api("mezz:jei:4.36.1:dev")
    api(cleanroom.deobf("curse.maven:baubles-227083:2518667"))
    api(cleanroom.deobf("curse.maven:tinkersconstruct-74072:2902483"))
    api(cleanroom.deobf("curse.maven:mantle-74924:2713386"))
    api(unsigned("curse.maven:constructs-armory-287683:3174535"))
    // Ender Storage (optional) and its library CodeChickenLib. Both are signed, so they are unsigned like Armory.
    // CodeChickenLib needs MCP csv files in dev: see ccl-mcp below.
    api(unsigned("maven.modrinth:ender-storage:2.4.6.137"))
    api(unsigned("curse.maven:codechicken-lib-1-8-242818:2779848"))
    api("com.cleanroommc:modularui:2.4.3") {
        exclude(group = "zone.rong")
        exclude(group = "curse.maven")
    }
    api(cleanroom.deobf("maven.modrinth:nether-api:1.3.0"))
    api(cleanroom.deobf("curse.maven:just_enough_resources-240630:2728586"))
    compileOnly("zone.rong:mixinbooter:9.1")

    implementation(cleanroom.deobf("curse.maven:crafttweaker-239197:${property("craftTweakerFileId")}"))
    implementation(cleanroom.deobf("curse.maven:the-one-probe-245211:2667280"))
    implementation(cleanroom.deobf("curse.maven:hwyla-253449:2568751"))

    implementation("org.apache.groovy:groovy:4.0.30")
    implementation("com.cleanroommc:groovyscript:${property("groovyScriptVersion")}") {
        exclude(group = "codechicken")
        exclude(group = "curse.maven")
        exclude(group = "zone.rong")
        exclude(group = "com.cleanroommc")
    }
    implementation(cleanroom.deobf("curse.maven:crafttweaker-239197:${property("craftTweakerFileId")}"))

    shade("com.squareup.wire:wire-runtime:${property("wireVersion")}") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains", module = "annotations")
    }
}
configurations.configureEach { exclude(group = "mezz.jei") }
wire { kotlin {} }

tokenEnvoy {
    set("MODID", modId)
    set("MODNAME", modName)
    set("VERSION", modVersion)
    set(
        "MOD_DEPS",
        "required-after:modularui@[2.4.3,);required-after:nether_api@[1.3.0,);required-after:forgelin_continuous@[2.4.20.0,);after:tconstruct;after:mantle;after:conarm;after:enderstorage"
    )
    set("MOD_PREFIX", "$modId:")
    set("DATAFIXER_VERSION", "1")
    set("CLIENTSERVICES", "$modGroup.client.di.ClientServices")
    set("ISSUES_URL", "https://github.com/Srdjan-V/EndReforked/issues")
}

configurations.runtimeOnly { extendsFrom(shade) }
val devUuid = UUID.nameUUIDFromBytes("OfflinePlayer:$minecraftUsername".toByteArray()).toString()
cleanroom {
    runs {
        maybeCreate("client").configure(RunMinecraft::class.java) {
            username.set(minecraftUsername)
            uuid.set(devUuid)
            jvmArgs("-Dfml.coreMods.load=$coreModClass")
        }
        maybeCreate("server").configure(RunMinecraft::class.java) {
            uuid.set(devUuid)
            jvmArgs("-Dfml.coreMods.load=$coreModClass")
        }
    }
}
tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.FAIL; manifest {
    attributes(
        "FMLCorePlugin" to coreModClass,
        "FMLCorePluginContainsFMLMod" to true,
        "ForceLoadAsMod" to true
    )
}
}
tasks.shadowJar {
    configurations = listOf(shade); relocate(
    "com.squareup.wire",
    "$modGroup.shadow.wire"
); relocate("okio", "$modGroup.shadow.okio"); archiveClassifier = "mcp-shaded"; addMultiReleaseAttribute =
    false; exclude("META-INF/*.kotlin_module", "META-INF/versions/**")
}
tasks.named<RenameJar>("reobfJar") { from(tasks.shadowJar); archiveClassifier = "" }
tasks.withType<JavaExec>().configureEach { systemProperty("foundation.loadsall", "true") }

// ccl-mcp: CodeChickenLib's ObfMapping reads the ForgeGradle 2 properties GradleStart.srg.notch-srg and GradleStart.csvDir.
// CleanroomGradle only makes srg2mcp.tsrg, so we write fields.csv and methods.csv from it before each run.
val cclMcpDir = layout.buildDirectory.dir("ccl-mcp")
tasks.withType<RunMinecraft>().configureEach {
    systemProperty("net.minecraftforge.gradle.GradleStart.srg.notch-srg", cclMcpDir.get().file("notch-srg.srg").asFile.path)
    systemProperty("net.minecraftforge.gradle.GradleStart.csvDir", cclMcpDir.get().asFile.path)
    doFirst {
        val dir = cclMcpDir.get().asFile.apply { mkdirs() }
        File(dir, "notch-srg.srg").writeText("")
        val fields = StringBuilder("searge,name,side,desc\n")
        val methods = StringBuilder("searge,name,side,desc\n")
        layout.buildDirectory.file("cleanroom_gradle/userdev/srg2mcp.tsrg").get().asFile.forEachLine { line ->
            val p = line.trim().split(' ')
            if (!line.startsWith("\t")) return@forEachLine
            when {
                p[0].startsWith("field_") -> fields.append("${p[0]},${p[1]},2,\n")
                p[0].startsWith("func_") -> methods.append("${p[0]},${p.last()},2,\n")
            }
        }
        File(dir, "fields.csv").writeText(fields.toString())
        File(dir, "methods.csv").writeText(methods.toString())
    }
}
tasks.test { useJUnitPlatform() }
// The Java style findings (Configs, mixins) exist since before the progression phase; they must not fail the build.
tasks.withType<Checkstyle>().configureEach { isIgnoreFailures = true }
