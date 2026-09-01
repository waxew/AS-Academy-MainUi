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

// MainUi can build standalone by including Core itself.
// A Course App that already includes Core sets ACADEMY_MAIN_UI_EXTERNAL_CORE=1 to prevent a duplicate composite build.
if (System.getenv("ACADEMY_MAIN_UI_EXTERNAL_CORE") != "1") {
    val academyCoreDir = System.getenv("ACADEMY_CORE_DIR") ?: "../AS-Academy-Core"
    includeBuild(academyCoreDir)
}
