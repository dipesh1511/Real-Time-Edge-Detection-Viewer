// build.gradle.kts (Module: app)

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.realtime" // Check your actual package name here
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.realtime"
        minSdk = 27 // Perfect for CameraX!
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        // --- NDK/CMake Configuration (CRITICAL FOR NATIVE CODE) ---
        externalNativeBuild {
            cmake {
                // Set C++ flags
                arguments += listOf("-DANDROID_STL=c++_shared")
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
}

dependencies {

    // 🚨 CLEANED CAMERA X LIBRARIES
    val cameraxVersion = "1.3.3"

    implementation("androidx.camera:camera-core:1.3.3")
    implementation("androidx.camera:camera-camera2:1.3.3")
    implementation("androidx.camera:camera-lifecycle:1.3.3")
    implementation("androidx.camera:camera-view:1.3.3")
    implementation("androidx.camera:camera-extensions:1.3.3")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("com.google.android.material:material:1.11.0")

   }