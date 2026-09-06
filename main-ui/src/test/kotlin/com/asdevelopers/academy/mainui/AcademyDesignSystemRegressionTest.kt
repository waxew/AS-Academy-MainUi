package com.asdevelopers.academy.mainui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AcademyDesignSystemRegressionTest {
    @Test
    fun semanticDesignFingerprintIsStable() {
        assertEquals(
            "#6750A4|#625B71|#7D5260|4|6|12|14|24|48",
            AcademyDesignTokens.regressionFingerprint()
        )
    }

    @Test
    fun minimumTouchTargetMeetsAccessibilityBaseline() {
        assertTrue(AcademyDesignTokens.Accessibility.MinimumTouchTargetDp >= 48)
    }

    @Test
    fun largeTextScaleContractExceedsThirtyPercent() {
        assertTrue(AcademyDesignTokens.Accessibility.LargeBodyScale >= 1.30f)
    }
}
