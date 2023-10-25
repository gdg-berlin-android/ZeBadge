// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.detekt.gradle) apply false
    alias(libs.plugins.dagger.hilt) apply false
    alias(libs.plugins.license.report.gradle) apply false
    id("com.android.test") version "8.0.2" apply false
    kotlin("multiplatform") version "1.9.10" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
