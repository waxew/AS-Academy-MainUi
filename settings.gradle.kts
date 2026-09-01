pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AS-Academy-MainUi"
include(":main-ui")

// MainUi consumes Core APIs but owns the visual/presentation layer.
// CI and local development can override the sibling path without editing source files.
val academyCoreDir = System.getenv("ACADEMY_CORE_DIR") ?: "../AS-Academy-Core"
includeBuild(academyCoreDir)
