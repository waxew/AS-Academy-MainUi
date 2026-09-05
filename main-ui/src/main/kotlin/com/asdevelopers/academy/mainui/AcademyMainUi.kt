package com.asdevelopers.academy.mainui

import androidx.compose.runtime.Composable
import com.asdevelopers.academy.course.model.CourseBranding

/** Features that a thin Course App can explicitly expose through MainUi. */
enum class AcademyCapability {
    SEARCH,
    BOOKMARKS,
    NOTES,
    ACHIEVEMENTS,
    PROGRESS,
    SETTINGS,
    QUIZZES,
    EXERCISES,
    PROJECTS,
    SHARE,
    UPDATE
}

/** Host-owned application metadata. MainUi renders it but never invents version/update state. */
data class AcademyAppInfo(
    val versionName: String = "",
    val versionCode: Long? = null,
    val shareText: String = "",
    val updateLabel: String = "",
    val updateAvailable: Boolean = false
)

/**
 * Stable root configuration supplied by a thin Course App.
 *
 * MainUi does not resolve or hard-code curriculum data. The host identifies the Course Package and
 * supplies its branding/capabilities while MainCourse remains the content source and Core remains the runtime.
 */
data class AcademyMainUiConfig(
    val courseId: String,
    val branding: CourseBranding,
    val darkTheme: Boolean,
    val capabilities: Set<AcademyCapability> = emptySet(),
    val appInfo: AcademyAppInfo = AcademyAppInfo()
) {
    init {
        require(courseId.isNotBlank()) { "courseId must not be blank" }
        require(appInfo.versionCode == null || appInfo.versionCode >= 0L) { "versionCode must be non-negative" }
    }
}

/**
 * Public MainUi entry boundary.
 *
 * The former implementation called the removed Core `AcademyCourseApp` composable and therefore could
 * not compile against Core 1.3. The root now owns presentation configuration only. Course loading,
 * persistence and navigation are wired by the host through the shared MainUi/Core facade components.
 */
@Composable
fun AcademyMainUi(
    config: AcademyMainUiConfig,
    content: @Composable () -> Unit
) {
    AcademyMainUiTheme(
        branding = config.branding,
        darkTheme = config.darkTheme,
        content = content
    )
}
