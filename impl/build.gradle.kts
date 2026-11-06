// Side-neutral logic. Plain Kotlin/JVM library that may use Minecraft and Forge types.
// CleanroomGradle must be applied here too: it makes the Minecraft classpath resolvable in this project.
plugins {
    `java-library`
    kotlin("jvm")
    id("com.cleanroommc.cleanroomgradle")
}

val kotlinVersion = "2.4.20"
val cleanroom_version = project.property("cleanroom_version") as String

dependencies {
    // Each project resolves the userdev itself. The mod jar takes the class output of each project, not its dependencies.
    implementation(cleanroom.userdev(cleanroom_version))
    api(project(":api"))
    api(project(":wire"))

    testImplementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test { useJUnitPlatform() }
