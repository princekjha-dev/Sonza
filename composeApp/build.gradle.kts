import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    // AGP 9+ KMP-aware Android library plugin (replaces com.android.library +
    // androidTarget(); see :core:model/build.gradle.kts for context).
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    androidLibrary {
        namespace = "com.sonza.app.composeapp"
        compileSdk = 36
        minSdk = 26
        withHostTestBuilder { }
    }

    jvm("desktop") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                // Coil 3 — KMP image loading. Network engine is Ktor so
                // the same AsyncImage call works on Android + Desktop.
                // ktor-client-cio is the JVM HTTP engine Coil uses to
                // actually fetch URLs.
                implementation(libs.coil.compose)
                implementation(libs.coil.network.ktor3)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.cio)
                implementation(project(":core:model"))
                // :core:domain provides the MusicPlayer expect class
                // (Phase 4) — backed by VLCJ on Desktop, by a stub on
                // Android until Phase 4.2 wires the existing Media3
                // player. Brings in kotlinx-coroutines transitively.
                implementation(project(":core:domain"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                // NewPipe Extractor for YouTube search + stream URL
                // resolution. Pure JVM library — works on Desktop without
                // any Android-specific bits. Brings in jsoup + Mozilla
                // Rhino transitively (~5 MB extra in the MSI).
                implementation(libs.newpipe.extractor)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.sonza.app.composeapp.MainKt"

        buildTypes.release.proguard {
            isEnabled.set(false)
        }

        nativeDistributions {
            targetFormats(
                TargetFormat.Deb,
                TargetFormat.Rpm,
                TargetFormat.Msi,
                TargetFormat.Exe,
                TargetFormat.Dmg
            )
            packageName = "Sonza"
            packageVersion = "1.0.0"
            description = "Sonza Desktop — High Fidelity Audiophile Music Player"
            vendor = "Sonza"
            copyright = "Copyright (c) 2026 Sonza. All rights reserved."
            licenseFile.set(rootProject.file("LICENSE"))

            linux {
                iconFile.set(project.file("src/desktopMain/resources/icons/icon.png"))
            }

            windows {
                menu = true
                shortcut = true
                upgradeUuid = "9F8D2C1A-4B7E-4F3D-9A6C-3E2B1A8D5C7F"
                iconFile.set(project.file("src/desktopMain/resources/icons/icon.ico"))
            }

            macOS {
                bundleID = "com.sonza.app"
                iconFile.set(project.file("src/desktopMain/resources/icons/icon.icns"))
            }
        }
    }
}
