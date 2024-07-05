import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ktlint.gradle)
    alias(libs.plugins.detekt.gradle)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.license.report.gradle)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.aboutlibraries.gradle)
    alias(libs.plugins.roborazzi)
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
        resourceConfigurations.addAll(
            listOf(
                "ar-rEG",
                "de-rDE",
                "en-rGB",
                "fr",
                "hi",
                "hr-rHR",
                "ja",
                "lt",
                "mr",
                "nl",
                "sq",
                "tr",
                "uk",
                "ur",
                "bs",
                "pt-rBR",
                "pl",
                "et",
                "ru",
            ),
        )

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
                "\"${System.getenv("DALE2_TOKEN")}\"",
            )
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
                "proguard-rules.pro",
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
        "${layout.buildDirectory}/generated/assets",
    )

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"

        freeCompilerArgs +=
            listOf(
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                "-opt-in=androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi",
            )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion =
            libs.versions.androidx.compose.compiler
                .get()
    }

    packaging {
        resources {
            excludes +=
                arrayOf(
                    "META-INF/AL2.0",
                    "META-INF/LGPL2.1",
                    "META-INF/*.kotlin_module",
                    "META-INF/LICENSE.*",
                    "META-INF/LICENSE-notice.*",
                )
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

detekt {
    allRules = true
    config.from(files("$rootDir/config/detekt/detekt-config.yml"))
    baseline = file("detekt-baseline.xml")
    buildUponDefaultConfig = true
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(projects.badge)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui.ui)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.toolingpreview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation)
    implementation(libs.retrofit2.retrofit)
    implementation(libs.retrofit2.converter.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.zxing)
    implementation(libs.material3.wsc)
    implementation(libs.dagger.hilt)
    implementation(libs.coil.compose)
    implementation(libs.coil.transformations)
    implementation(libs.timber)
    implementation(libs.aboutlibraries.compose)
    implementation(libs.androidx.compose.hilt.navigation)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.testAssertk)
    testImplementation(libs.testJunit4)
    testImplementation(libs.testMockk)
    testImplementation(libs.testCoroutines)
    testImplementation(libs.bundles.screeshottest.android)

    androidTestImplementation(libs.testComposeJunit)
    debugImplementation(libs.testComposeManifest)
    kapt(libs.dagger.hilt.compiler)
    baselineProfile(projects.benchmark)
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

licenseReport {
    generateHtmlReport = true
    generateJsonReport = false
    copyHtmlReportToAssets = false
}

roborazzi {
    outputDir.set(project.layout.projectDirectory.dir("src/snapshots/roborazzi/images"))
}
