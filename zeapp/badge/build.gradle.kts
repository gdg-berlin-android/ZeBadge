plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targetHierarchy.default()

    android {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }

            dependencies {
                implementation(libs.mik3y.usb.serial.android)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.timber)
            }
        }
    }

    val os = System.getProperty("os.name")
    when {
        os.startsWith("Mac OS") ->
            jvm("mac") {
                withJavaEnabled

                jvmToolchain {
                    languageVersion.set(JavaLanguageVersion.of(8))
                }
            }

        os.startsWith("Linux") ->
            jvm("linux") {
                withJavaEnabled

                jvmToolchain {
                    languageVersion.set(JavaLanguageVersion.of(8))
                }
            }

        else -> throw StopActionException("Your operating system is not supported at this time: '${os}'.")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                //put your multiplatform dependencies here
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val mac by creating {
            dependsOn(commonMain)
        }

    }
}

android {
    namespace = "de.berlindroid.zebadge"
    compileSdk = 34
    defaultConfig {
        minSdk = 29
    }
}
