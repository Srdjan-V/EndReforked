// Forge glue for the logical server: everything except client code. Depends on impl.
// The Minecraft and mod dependencies are 'api' so that :client sees them through this project.
plugins {
    `java-library`
    kotlin("jvm")
    id("com.cleanroommc.cleanroomgradle")
}

val cleanroom_version = project.property("cleanroom_version") as String
val modId = project.property("modId") as String
val modName = project.property("modName") as String
val modVersion = project.property("modVersion") as String
val modGroup = project.property("modGroup") as String

val modularUI = "2.4.3"
val netherAPI = "1.3.0"
val groovyScript = project.property("groovyScriptVersion") as String
val craftTweaker = project.property("craftTweakerFileId") as String

dependencies {
    api(project(":impl"))

    api(cleanroom.userdev(cleanroom_version))

    compileOnly("zone.rong:mixinbooter:9.1")
    compileOnly("org.jetbrains:annotations:24.1.0")

    api("mezz:jei:4.36.1:dev")

    api(cleanroom.deobf("curse.maven:baubles-227083:2518667"))
    api(cleanroom.deobf("curse.maven:tinkersconstruct-74072:2902483"))
    api(cleanroom.deobf("curse.maven:mantle-74924:2713386"))
    api("com.cleanroommc:modularui:$modularUI")
    api(cleanroom.deobf("maven.modrinth:nether-api:$netherAPI"))
    api(cleanroom.deobf("curse.maven:just_enough_resources-240630:2728586"))

    // Optional: the script API in compat/groovyscript. Compile only, the dev-named jar has the MCP names of this classpath.
    // GroovyScript's own dependencies (CodeChickenLib, TOP, ...) are not needed to compile against it, Groovy is.
    compileOnly("com.cleanroommc:groovyscript:$groovyScript") { isTransitive = false }
    compileOnly("org.apache.groovy:groovy:4.0.30")

    // Optional: the script API in compat/crafttweaker. Compile only, deobfuscated to the MCP names of this classpath.
    compileOnly(cleanroom.deobf("curse.maven:crafttweaker-239197:$craftTweaker"))

    // Dropped: CodeChickenLib, Redstone Flux, CoFH Core/World, Thermal Expansion/Foundation/Dynamics.
    // No source uses them, and CCL 3.2.3 crashes in the Cleanroom dev env (see report).
}

// JER or TiC bring in the old JEI 4.16 transitively, which clashes with HEI/JEI 4.36
configurations.configureEach { exclude(group = "mezz.jei") }

// Generates io.github.srdjanv.endreforked.Tags
val tags = mapOf(
    "MODID" to "\"$modId\"",
    "MODNAME" to "\"$modName\"",
    "VERSION" to "\"$modVersion\"",
    "MOD_DEPS" to "\"required-after:modularui@[$modularUI,);required-after:nether_api@[$netherAPI,);required-after:forgelin_continuous@[2.4.20.0,)\"",
    "MOD_PREFIX" to "\"$modId:\"",
    "DATAFIXER_VERSION" to "1",
    "CLIENTSERVICES" to "\"$modGroup.client.di.ClientServices\"",
    "ISSUES_URL" to "\"https://github.com/Srdjan-V/EndReforked/issues\"",
)
val tagsDir = layout.buildDirectory.dir("generated/tags")
val generateTags = tasks.register("generateTags") {
    // Local copies: a task action must not capture the script object (configuration cache)
    val tags = tags
    val tagsDir = tagsDir
    val tagsPackage = modGroup
    inputs.property("tags", tags)
    outputs.dir(tagsDir)
    doLast {
        val f = tagsDir.get().file(tagsPackage.replace('.', '/') + "/Tags.java").asFile
        f.parentFile.mkdirs()
        val fields = tags.map { (k, v) ->
            "    public static final ${if (v.startsWith("\"")) "String" else "int"} $k = $v;"
        }
        f.writeText("package $tagsPackage;\n\npublic final class Tags {\n\n${fields.joinToString("\n")}\n\n    private Tags() {}\n}\n")
    }
}
sourceSets.main { java.srcDir(generateTags.map { tagsDir }) }
