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

tasks {
    val fatJar = register<Jar>("fatJar") {
        dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources")) // We need this for Gradle optimization to work
        archiveClassifier.set("standalone") // Naming the jar
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest { attributes(mapOf("Main-Class" to application.mainClass)) } // Provided we set it up in the application plugin configuration
        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) } +
                sourcesMain.output
        from(contents)
    }
    build {
        dependsOn(fatJar) // Trigger fat jar creation during build
    }
}
