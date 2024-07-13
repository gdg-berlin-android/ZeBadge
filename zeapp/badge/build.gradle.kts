plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.android.library")
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    applyDefaultHierarchyTemplate()

    kotlin {
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    androidTarget {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        }

        compilations.all {
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
            }

        os.startsWith("Linux") ->
            jvm("linux") {
                withJavaEnabled
            }

        os.startsWith("Windows") ->
            jvm("windows") {
                withJavaEnabled
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
        minSdk = 29
    }
}
