package com.asdevelopers.academy.mainui

import androidx.compose.runtime.Composable
import com.asdevelopers.academy.core.ui.AcademyCourseApp

/**
 * ورودی عمومی و پایدار لایه UI مجموعه AS Academy.
 *
 * Course appها فقط courseId را ارسال می‌کنند. جزئیات Navigation، Progress،
 * Search، Quiz، Exercise، Project و سایر موتورهای آموزشی در Core باقی می‌ماند؛
 * بنابراین هیچ Course app نباید صفحه‌های مشترک را دوباره پیاده‌سازی کند.
 */
@Composable
fun AcademyMainUi(
    courseId: String,
) {
    AcademyCourseApp(courseId = courseId)
}
