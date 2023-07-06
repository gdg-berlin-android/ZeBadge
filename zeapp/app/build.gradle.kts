import org.jetbrains.kotlin.incremental.createDirectory
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import java.lang.ProcessBuilder.Redirect

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ktlint.gradle)
    alias(libs.plugins.detekt.gradle)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.google.play.services)
    alias(libs.plugins.firebase.appdistribution)
}

android {
    namespace = "de.berlindroid.zeapp"
    compileSdk = 33

    defaultConfig {
        applicationId = "de.berlindroid.zeapp"
        minSdk = 29
        targetSdk = 33
        versionCode = 1
        versionName =  "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        configureEach {
            buildConfigField("String", "OPEN_API_TOKEN", "\"${System.getenv("DALE2_TOKEN")}\"" ?: "\"\"")

            firebaseAppDistribution {
                releaseNotesFile="./release-notes.txt"
                groups="testers"
            }
        }

        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    sourceSets.getByName("main").assets.srcDir(
        "$projectDir/build/generated/assets"
    )

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"

        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi")

    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

detekt {
    allRules = true
    config = files("$rootDir/config/detekt/detekt-config.yml")
    baseline = file("detekt-baseline.xml")
    buildUponDefaultConfig = true
    reports {
        html { enabled = true }
        xml { enabled = true }
        txt { enabled = false }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.toolingpreview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.retrofit2.retrofit)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.mik3y.usb.serial.android)
    implementation(libs.zxing)
    implementation(libs.material3.wsc)
    implementation(libs.dagger.hilt)
    implementation(libs.coil.compose)
    implementation(libs.coil.transformations)
    implementation(platform(libs.firebase))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.test.assertk)
    testImplementation(libs.test.junit)

    androidTestImplementation(libs.test.compose.junit)
    debugImplementation(libs.test.compose.manifest)
    kapt(libs.dagger.hilt.compiler)
}

// Ktlint
ktlint {
    debug.set(true)
    verbose.set(false)
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(true)
    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.CHECKSTYLE)
    }
    filter {
        exclude("**/generated/**")
    }
}

kapt {
    correctErrorTypes = true
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KaptGenerateStubs::class).configureEach {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
}

tasks.create("generateContributorsAsset") {
    val command = "git shortlog -sne --all"
    val process = ProcessBuilder()
        .command(command.split(" "))
        .directory(rootProject.projectDir)
        .redirectOutput(Redirect.PIPE)
        .redirectError(Redirect.PIPE)
        .start()
    process.waitFor(60, TimeUnit.SECONDS)
    val result = process.inputStream.bufferedReader().readText()

    val contributors = result.lines()
            .joinToString(separator = System.lineSeparator()) { it.substringAfter("\t") }

    val assetDir = layout.buildDirectory.dir("generated/assets").get().asFile
    assetDir.createDirectory()
    File(assetDir, "test.txt").writeText(contributors)

}
tasks.getByName("build").dependsOn("generateContributorsAsset")
