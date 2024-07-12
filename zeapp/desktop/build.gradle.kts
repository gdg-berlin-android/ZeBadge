import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    alias(libs.plugins.jetbrains.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.roborazzi)
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation(projects.badge)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.roborazzi.compose.desktop)
    testImplementation(kotlin("test"))
}

compose.desktop {
    application {
        mainClass = "de.berlindroid.zekompanion.desktop.ZeBadgeKompanion"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "desktop"
            packageVersion = "1.0.0"
            macOS {
                iconFile.set(project.file("icon.icns"))
            }
        }
    }
}

roborazzi {
    outputDir.set(project.layout.projectDirectory.dir("src/snapshots/roborazzi/images"))
}

kotlin{
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}
