// Side-neutral logic. Plain Kotlin/JVM library, no Minecraft dependency.
plugins {
    `java-library`
    kotlin("jvm")
}

val kotlinVersion = "2.4.20"

dependencies {
    api(project(":api"))
    api(project(":wire"))

    testImplementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test { useJUnitPlatform() }
