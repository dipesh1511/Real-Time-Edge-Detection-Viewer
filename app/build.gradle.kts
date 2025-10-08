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
        minSdk = 24
//        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // --- NDK/CMake Configuration (CRITICAL FOR NATIVE CODE) ---
        externalNativeBuild {
            cmake {
                // Set C++ flags
                cppFlags.add("-std=c++17")

                // Arguments passed to CMake (for OpenCV path)
                arguments.addAll(listOf(
                    // NOTE: Change this path if your OpenCV SDK location is different!
                    // This path points to the compiled .so files that CMake will link against.
                    "-DOPENCV_LIB_DIR=${project.rootDir}/app/src/main/jniLibs/arm64-v8a"
                ))
            }
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
    // ... other dependencies
}