plugins {
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlinx.kover")
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

// Coverage verification thresholds are configured per module (see individual build.gradle.kts files)
