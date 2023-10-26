plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.0"
}

group = "de.berlindroid"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(project(":badge"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ktor.core)
    implementation(libs.ktor.netty)
    implementation(libs.ktor.content.negotiation)
    implementation(libs.ktor.content.serialization)
    implementation(libs.kotlinx.serialization.json)
}

application {
    mainClass.set("de.berlindroid.zekompanion.server.Main")
}

