plugins {
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
}
