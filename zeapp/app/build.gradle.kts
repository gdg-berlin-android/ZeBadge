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
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.license.report.gradle)
    alias(libs.plugins.baselineprofile)
}

val isCi = System.getenv("CI") == "true"
val zeAppPassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
val enableRelease = isCi && zeAppPassword != ""
val appVersionCode = System.getenv("GITHUB_RUN_NUMBER")?.toInt() ?: 1
val zeAppDebug = "ZEapp23"

android {
    namespace = "de.berlindroid.zeapp"

    defaultConfig {
        applicationId = "de.berlindroid.zeapp"
        compileSdk = 34
        targetSdk = 34
        minSdk = 29
        versionCode = appVersionCode
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
        resourceConfigurations.addAll(listOf("ar-rEG", "de-rDE", "en-rGB", "fr", "hi", "hr-rHR", "ja", "lt", "mr", "nl", "sq", "tr", "uk", "ur", "bs", "pt-rBR"))

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        named("debug") {
            keyAlias = zeAppDebug
            keyPassword = zeAppDebug
            storeFile = file("$rootDir/zeapp_debug")
            storePassword = zeAppDebug
        }
        if (enableRelease) {
            create("release") {
                keyAlias = "zeapp-sample"
                keyPassword = zeAppPassword
                storeFile = file("$rootDir/zeapp")
                storePassword = zeAppPassword
            }
        }
    }

    buildTypes {
        configureEach {
            buildConfigField(
                "String",
                "OPEN_API_TOKEN",
                "\"${System.getenv("DALE2_TOKEN")}\"" ?: "\"\""
            )

            firebaseAppDistribution {
                releaseNotesFile = "./release-notes.txt"
                groups = "testers"
            }
        }
        create("benchmark") {
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            if (enableRelease) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    lint {
        disable.add("MissingTranslation")
    }

    sourceSets.getByName("main").assets.srcDir(
        "$buildDir/generated/assets",
    )

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"

        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi",
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }

    packaging {
        resources {
            excludes += arrayOf(
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/*.kotlin_module",
                "META-INF/LICENSE.*",
                "META-INF/LICENSE-notice.*"
            )
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
    implementation(libs.androidx.datastore)
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
    implementation(libs.timber)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.test.assertk)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.coroutines)

    androidTestImplementation(libs.test.compose.junit)
    debugImplementation(libs.test.compose.manifest)
    kapt(libs.dagger.hilt.compiler)
    baselineProfile(project(":benchmark"))
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

licenseReport {
    generateHtmlReport = true
    generateJsonReport = false
    copyHtmlReportToAssets = false
}
