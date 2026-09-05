package com.asdevelopers.academy.mainui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.asdevelopers.academy.core.settings.AcademySettings
import com.asdevelopers.academy.core.settings.AcademyThemeMode
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

/** Basic root retained for hosts that deliberately control theme themselves. */
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

/**
 * Preferred runtime-aware root. Persisted theme mode and font scale are applied immediately while the
 * configuration's [AcademyMainUiConfig.darkTheme] remains the fallback until DataStore emits.
 */
@Composable
fun AcademyPreferenceAwareMainUi(
    config: AcademyMainUiConfig,
    runtime: AcademyMainUiRuntime,
    content: @Composable () -> Unit
) {
    val settings by runtime.preferencesRepository.settings.collectAsState(initial = AcademySettings())
    val baseDensity = LocalDensity.current
    val systemDark = isSystemInDarkTheme()
    val resolvedDarkTheme = when (settings.themeMode) {
        AcademyThemeMode.SYSTEM -> systemDark
        AcademyThemeMode.LIGHT -> false
        AcademyThemeMode.DARK -> true
    }
    val scaledDensity = Density(
        density = baseDensity.density,
        fontScale = settings.fontScale
    )

    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        AcademyMainUiTheme(
            branding = config.branding,
            darkTheme = resolvedDarkTheme,
            content = content
        )
    }
}
