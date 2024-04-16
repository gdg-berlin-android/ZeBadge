plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targetHierarchy.default()

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
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
                    languageVersion.set(JavaLanguageVersion.of(17))
                }
            }

        os.startsWith("Linux") ->
            jvm("linux") {
                withJavaEnabled

                jvmToolchain {
                    languageVersion.set(JavaLanguageVersion.of(17))
                }
            }

        os.startsWith("Windows") ->
            jvm("windows") {
                withJavaEnabled

                jvmToolchain {
                    languageVersion.set(JavaLanguageVersion.of(17))
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
                implementation(libs.testJunit4)
                implementation(libs.testAssertk)
            }
        }

        val jvmMain by creating {
            sourceSets {
                dependsOn(sourceSets.getAt("commonMain"))
            }
        }

        when {
            os.startsWith("Mac OS") -> {
                val macMain by getting {
                    dependencies {
                        implementation(libs.jSerialComm)
                    }

                    sourceSets {
                        dependsOn(sourceSets.getAt("jvmMain"))
                    }
                }
            }

            os.startsWith("Linux") -> {
                val linuxMain by getting {
                    dependencies {
                        implementation(libs.jSerialComm)
                    }

                    sourceSets {
                        dependsOn(sourceSets.getAt("jvmMain"))
                    }
                }
            }

            os.startsWith("Windows") -> {
                val windowsMain by getting {
                    dependencies {
                        implementation(libs.jSerialComm)
                    }

                    sourceSets {
                        dependsOn(sourceSets.getAt("jvmMain"))
                    }
                }
            }
        }
    }
}

android {
    namespace = "de.berlindroid.zebadge"
    compileSdk = 34
    defaultConfig {
        minSdk = 33
    }
}
