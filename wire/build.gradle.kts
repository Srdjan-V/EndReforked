// Wire (Square) protobuf layer. Pure Kotlin codegen. Depends on nothing except the Wire runtime, which :mod shades and relocates.
plugins {
    `java-library`
    kotlin("jvm")
    id("com.squareup.wire") version "7.1.2"
}

val wireVersion = project.property("wireVersion") as String

dependencies {
    // The Kotlin stdlib (wire needs 2.1.21 at least) is provided by Forgelin-Continuous
    api("com.squareup.wire:wire-runtime:$wireVersion") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains", module = "annotations")
    }

    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

wire {
    kotlin {}
}

tasks.test { useJUnitPlatform() }
