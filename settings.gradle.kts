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
include(":academy-viewer")

// When MainUi is the root build it includes Core so it can be developed and tested standalone.
// When MainUi is itself included by a Course App, the parent build owns the single Core composite.
if (gradle.parent == null) {
    val academyCoreDir = System.getenv("ACADEMY_CORE_DIR") ?: "../AS-Academy-Core"
    includeBuild(academyCoreDir)
}
