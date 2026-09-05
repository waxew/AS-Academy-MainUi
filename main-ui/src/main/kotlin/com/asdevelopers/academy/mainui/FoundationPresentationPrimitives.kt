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
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.asdevelopers.academy.core.settings.AcademyProfile
import com.asdevelopers.academy.course.model.CourseBranding
import com.asdevelopers.academy.course.model.Lesson
import com.asdevelopers.academy.course.model.LessonBlock
import com.asdevelopers.academy.course.model.LessonBlockType

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

/** Canonical block renderer owned by MainUi. Core only supplies the versioned content model. */
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
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(lesson.title, style = MaterialTheme.typography.headlineMedium)
                if (lesson.summary.isNotBlank()) Text(lesson.summary, style = MaterialTheme.typography.bodyLarge)
                Text("زمان تقریبی: ${lesson.estimatedMinutes} دقیقه", style = MaterialTheme.typography.labelMedium)
            }
        }
        items(lesson.blocks, key = LessonBlock::id) { block ->
            AcademyLessonBlock(
                block = block,
                onExerciseClick = onExerciseClick,
                onQuizClick = onQuizClick,
                onProjectClick = onProjectClick
            )
        }
    }
}

@Suppress("DEPRECATION")
@Composable
private fun AcademyLessonBlock(
    block: LessonBlock,
    onExerciseClick: (String) -> Unit,
    onQuizClick: (String) -> Unit,
    onProjectClick: (String) -> Unit
) {
    when (block.type) {
        LessonBlockType.TITLE -> Text(block.content, style = MaterialTheme.typography.headlineSmall)
        LessonBlockType.SUBTITLE -> Text(block.content, style = MaterialTheme.typography.titleLarge)
        LessonBlockType.PARAGRAPH -> Text(block.content, style = MaterialTheme.typography.bodyLarge)
        LessonBlockType.LIST -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            block.content.lineSequence().filter(String::isNotBlank).forEach { line -> Text("• ${line.trim().removePrefix("-").trim()}") }
        }
        LessonBlockType.TABLE -> AcademyTextCard(block.content, monospace = true)
        LessonBlockType.CODE,
        LessonBlockType.OUTPUT -> AcademyTextCard(block.content, monospace = true)
        LessonBlockType.TIP -> AcademyLabeledCard("نکته", block.content)
        LessonBlockType.WARNING -> AcademyLabeledCard("هشدار", block.content)
        LessonBlockType.NOTE -> AcademyLabeledCard("یادداشت", block.content)
        LessonBlockType.IMPORTANT -> AcademyLabeledCard("مهم", block.content)
        LessonBlockType.IMAGE,
        LessonBlockType.DIAGRAM -> AcademyLabeledCard(
            block.accessibilityLabel ?: if (block.type == LessonBlockType.IMAGE) "تصویر" else "نمودار",
            block.metadata["caption"] ?: block.content
        )
        LessonBlockType.EXERCISE,
        LessonBlockType.EXERCISE_LINK -> Button(
            onClick = { onExerciseClick(block.content.trim()) },
            modifier = Modifier.fillMaxWidth()
        ) { Text(block.metadata["label"] ?: "باز کردن تمرین") }
        LessonBlockType.QUIZ -> Button(
            onClick = { onQuizClick(block.content.trim()) },
            modifier = Modifier.fillMaxWidth()
        ) { Text(block.metadata["label"] ?: "باز کردن آزمون") }
        LessonBlockType.PROJECT_LINK,
        LessonBlockType.PROJECT -> Button(
            onClick = { onProjectClick(block.content.trim()) },
            modifier = Modifier.fillMaxWidth()
        ) { Text(block.metadata["label"] ?: "باز کردن پروژه") }
        LessonBlockType.REFERENCE -> AcademyLabeledCard("مرجع", block.content)
    }
}

@Composable
private fun AcademyTextCard(content: String, monospace: Boolean = false) {
    Card(Modifier.fillMaxWidth()) {
        Text(
            text = content,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = if (monospace) FontFamily.Monospace else null
        )
    }
}

@Composable
private fun AcademyLabeledCard(label: String, content: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, style = MaterialTheme.typography.titleSmall)
            Text(content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun String.toComposeColor(fallback: Color): Color =
    runCatching { Color(parseColor(this)) }.getOrDefault(fallback)
