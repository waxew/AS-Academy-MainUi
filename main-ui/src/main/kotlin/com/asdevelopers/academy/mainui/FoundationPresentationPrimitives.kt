package com.asdevelopers.academy.mainui

import android.graphics.Color.parseColor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.asdevelopers.academy.core.settings.AcademyProfile
import com.asdevelopers.academy.course.model.CourseBranding
import com.asdevelopers.academy.course.model.Lesson

/** Drawer model is presentation-owned; callbacks are supplied by the thin host. */
data class AcademyMainUiDrawerItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val selected: Boolean = false,
    val onClick: () -> Unit
)

val DefaultMainUiBranding = CourseBranding(
    primaryColorHex = "#6750A4",
    secondaryColorHex = "#625B71",
    accentColorHex = "#7D5260"
)

@Composable
internal fun FoundationAcademyTheme(
    branding: CourseBranding,
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val primary = branding.primaryColorHex.toComposeColor(Color(0xFF6750A4))
    val secondary = branding.secondaryColorHex.toComposeColor(Color(0xFF625B71))
    val tertiary = branding.accentColorHex.toComposeColor(Color(0xFF7D5260))
    MaterialTheme(
        colorScheme = if (darkTheme) {
            darkColorScheme(primary = primary, secondary = secondary, tertiary = tertiary)
        } else {
            lightColorScheme(primary = primary, secondary = secondary, tertiary = tertiary)
        },
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FoundationAcademyShell(
    title: String,
    profile: AcademyProfile,
    courseItems: List<AcademyMainUiDrawerItem>,
    onProfileImageClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onShareClick: () -> Unit,
    onAboutClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Outlined.Menu, contentDescription = "باز کردن منو")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(profile.displayName.ifBlank { "پروفایل" }) },
                            onClick = { menuOpen = false; onProfileImageClick() }
                        )
                        courseItems.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.label) },
                                leadingIcon = { Icon(item.icon, contentDescription = null) },
                                onClick = { menuOpen = false; item.onClick() }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("تنظیمات") },
                            leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                            onClick = { menuOpen = false; onSettingsClick() }
                        )
                        DropdownMenuItem(
                            text = { Text("اشتراک‌گذاری") },
                            leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) },
                            onClick = { menuOpen = false; onShareClick() }
                        )
                        DropdownMenuItem(
                            text = { Text("درباره") },
                            leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                            onClick = { menuOpen = false; onAboutClick() }
                        )
                    }
                }
            )
        },
        content = content
    )
}

/** Foundation-owned safe lesson fallback. Rich block renderers can evolve here without Core UI dependencies. */
@Composable
internal fun FoundationLessonRenderer(
    lesson: Lesson,
    modifier: Modifier = Modifier,
    onExerciseClick: (String) -> Unit = {},
    onQuizClick: (String) -> Unit = {},
    onProjectClick: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        item { Text(lesson.title, style = MaterialTheme.typography.headlineMedium) }
        item {
            Text(
                text = lesson.toString(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onExerciseClick(lesson.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("تمرین‌های مرتبط")
                }
                Button(onClick = { onQuizClick(lesson.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("آزمون‌های مرتبط")
                }
                Button(onClick = { onProjectClick(lesson.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("پروژه‌های مرتبط")
                }
            }
        }
    }
}

private fun String.toComposeColor(fallback: Color): Color =
    runCatching { Color(parseColor(this)) }.getOrDefault(fallback)
