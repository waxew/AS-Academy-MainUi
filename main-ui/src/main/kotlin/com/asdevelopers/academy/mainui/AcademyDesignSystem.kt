package com.asdevelopers.academy.mainui

import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Stable MainUi-owned semantic design contract. Thin apps configure branding only;
 * layout rhythm and accessibility dimensions remain consistent across Academy apps.
 */
object AcademyDesignTokens {
    object Colors {
        const val DefaultPrimary = "#6750A4"
        const val DefaultSecondary = "#625B71"
        const val DefaultAccent = "#7D5260"
    }

    object Spacing {
        const val Xs = 4
        const val Sm = 6
        const val Md = 12
        const val Lg = 14
        const val Xl = 24
    }

    object Accessibility {
        const val MinimumTouchTargetDp = 48
        const val MinimumBodyScale = 1.0f
        const val LargeBodyScale = 1.30f
    }

    /** Deterministic fingerprint used as a regression contract in unit/CI tests. */
    fun regressionFingerprint(): String = listOf(
        Colors.DefaultPrimary,
        Colors.DefaultSecondary,
        Colors.DefaultAccent,
        Spacing.Xs,
        Spacing.Sm,
        Spacing.Md,
        Spacing.Lg,
        Spacing.Xl,
        Accessibility.MinimumTouchTargetDp
    ).joinToString("|")
}

@Composable
internal fun AcademyAccessibleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    require(contentDescription.isNotBlank()) {
        "Interactive Academy icons require a non-empty accessibility description"
    }
    IconButton(
        onClick = onClick,
        modifier = modifier.sizeIn(
            minWidth = AcademyDesignTokens.Accessibility.MinimumTouchTargetDp.dp,
            minHeight = AcademyDesignTokens.Accessibility.MinimumTouchTargetDp.dp
        )
    ) {
        Icon(icon, contentDescription = contentDescription)
    }
}
