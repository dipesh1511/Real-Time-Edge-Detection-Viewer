// build.gradle.kts (Module: app)
import org.gradle.api.JavaVersion
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.realtime" // Check your actual package name here
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.realtime"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        kotlinOptions {
            jvmTarget = "17"
        }

        // --- NDK/CMake Configuration (CRITICAL FOR NATIVE CODE) ---
        externalNativeBuild {
            cmake {
                // Set C++ flags
                cppFlags.add("-std=c++17")

            }
        }

        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "x86_64"))
        }
    }

    // Tell Gradle where to find your CMake file
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    // This block tells Gradle to include the .so files found in jniLibs
    sourceSets.getByName("main") {
        jniLibs.srcDirs("src/main/jniLibs")
    }

    // ... rest of the android block
}

dependencies {
    // Use parentheses for function calls in Kotlin DSL
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.11.0")
}