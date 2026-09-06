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
        mavenLocal()

        val githubActor = System.getenv("GITHUB_ACTOR")
        val githubToken = System.getenv("GITHUB_TOKEN")
        if (!githubActor.isNullOrBlank() && !githubToken.isNullOrBlank()) {
            maven {
                name = "AcademyCoreGitHubPackages"
                url = uri("https://maven.pkg.github.com/waxew/AS-Academy-Core")
                credentials {
                    username = githubActor
                    password = githubToken
                }
            }
        }
    }
}

rootProject.name = "AS-Academy-MainUi"
include(":main-ui")
include(":academy-viewer")
include(":thin-app-template")

// Composite Core is the default for local cross-repo development.
// CI/release consumers can force normal Maven resolution to prove that MainUi
// works against the published Core artifact chain without source inclusion.
val usePublishedCore = System.getenv("ACADEMY_USE_PUBLISHED_CORE")
    ?.equals("true", ignoreCase = true) == true

if (gradle.parent == null && !usePublishedCore) {
    val academyCoreDir = System.getenv("ACADEMY_CORE_DIR") ?: "../AS-Academy-Core"
    includeBuild(academyCoreDir)
}
