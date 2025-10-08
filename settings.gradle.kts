// settings.gradle.kts (Project Root)

pluginManagement {
    // Define repositories from where Gradle plugins are downloaded
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    // Define repositories for dependencies (like androidx libraries)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Flam"

// CRITICAL: This line registers your main Android code folder as a buildable module.
include(":app")

// Note: Agar aapne root mein "web" folder rakha hai, use yahan include karne ki zarurat nahi hai
// jab tak woh Gradle module na ho. Use web build system se hi manage karein.