plugins {
    application
    kotlin("jvm")
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
}

application {
    mainClass.set("de.berlindroid.zekompanion.terminal.MainKt")
}

