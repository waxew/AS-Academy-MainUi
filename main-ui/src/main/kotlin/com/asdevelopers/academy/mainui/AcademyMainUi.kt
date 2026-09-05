package com.asdevelopers.academy.mainui

import androidx.compose.runtime.Composable
import com.asdevelopers.academy.course.model.CourseBranding

/** Features that a thin Course App can explicitly enable in the shared MainUi. */
enum class AcademyCapability {
    SEARCH,
    BOOKMARKS,
    NOTES,
    PROGRESS,
    ACHIEVEMENTS,
    SETTINGS,
    PROFILE,
    SHARE,
    ABOUT,
    UPDATE
}

/**
 * Host-supplied application metadata.
 *
 * Update remains opt-in: MainUi never invents an endpoint or performs an implicit network check.
 * When [updateUri] is absent, an update action can be hidden or rendered unavailable by the host.
 */
data class AcademyAppInfo(
    val versionName: String = "",
    val description: String = "",
    val supportEmail: String = "AS.Developers.Support@Gmail.Com",
    val shareText: String? = null,
    val updateUri: String? = null
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
    /** Legacy string capability surface retained so existing Course Apps remain source-compatible. */
    val capabilities: Set<String> = emptySet(),
    val typedCapabilities: Set<AcademyCapability> = emptySet(),
    val appInfo: AcademyAppInfo = AcademyAppInfo()
) {
    init {
        require(courseId.isNotBlank()) { "courseId must not be blank" }
    }

    fun hasCapability(capability: AcademyCapability): Boolean =
        capability in typedCapabilities || capability.name in capabilities
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
