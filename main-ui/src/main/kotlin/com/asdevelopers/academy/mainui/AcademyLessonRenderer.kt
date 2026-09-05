package com.asdevelopers.academy.mainui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.asdevelopers.academy.course.model.Lesson
import com.asdevelopers.academy.course.model.LessonBlock
import com.asdevelopers.academy.course.model.LessonBlockType

@Composable
fun AcademyLessonRenderer(
    lesson: Lesson,
    modifier: Modifier = Modifier,
    onExerciseClick: (String) -> Unit = {},
    onQuizClick: (String) -> Unit = {},
    onProjectClick: (String) -> Unit = {},
    assetRenderer: @Composable ((LessonBlock) -> Unit)? = null
) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item(key = "lesson-header") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(lesson.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(lesson.summary, style = MaterialTheme.typography.bodyLarge)
                Text("زمان تقریبی: ${lesson.estimatedMinutes} دقیقه", style = MaterialTheme.typography.labelMedium)
            }
        }
        items(lesson.blocks, key = LessonBlock::id) { block ->
            RenderAcademyLessonBlock(block, onExerciseClick, onQuizClick, onProjectClick, assetRenderer)
        }
    }
}

@Composable
fun RenderAcademyLessonBlock(
    block: LessonBlock,
    onExerciseClick: (String) -> Unit = {},
    onQuizClick: (String) -> Unit = {},
    onProjectClick: (String) -> Unit = {},
    assetRenderer: @Composable ((LessonBlock) -> Unit)? = null
) {
    when (block.type) {
        LessonBlockType.TITLE -> Text(block.content, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        LessonBlockType.SUBTITLE -> Text(block.content, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        LessonBlockType.PARAGRAPH -> SelectionContainer { Text(block.content, style = MaterialTheme.typography.bodyLarge) }
        LessonBlockType.LIST -> BulletList(block.content)
        LessonBlockType.TABLE -> SimpleTable(block.content)
        LessonBlockType.CODE -> CodeBlock(block.content, block.metadata["language"])
        LessonBlockType.OUTPUT -> CodeBlock(block.content, "output")
        LessonBlockType.NOTE -> Callout(block.content, Icons.Outlined.Info, MaterialTheme.colorScheme.secondaryContainer)
        LessonBlockType.TIP -> Callout(block.content, Icons.Outlined.Lightbulb, MaterialTheme.colorScheme.tertiaryContainer)
        LessonBlockType.WARNING -> Callout(block.content, Icons.Outlined.Warning, MaterialTheme.colorScheme.errorContainer)
        LessonBlockType.IMPORTANT -> Callout(block.content, Icons.Outlined.Info, MaterialTheme.colorScheme.primaryContainer)
        LessonBlockType.IMAGE,
        LessonBlockType.DIAGRAM -> assetRenderer?.invoke(block) ?: AssetPlaceholder(block)
        LessonBlockType.EXERCISE,
        LessonBlockType.EXERCISE_LINK -> ActionBlock("شروع تمرین", block.content) { onExerciseClick(block.metadata["exerciseId"] ?: block.content) }
        LessonBlockType.QUIZ -> ActionBlock("شروع آزمون", block.content) { onQuizClick(block.metadata["quizId"] ?: block.content) }
        LessonBlockType.PROJECT_LINK,
        LessonBlockType.PROJECT -> ActionBlock("مشاهده پروژه", block.content) { onProjectClick(block.metadata["projectId"] ?: block.content) }
        LessonBlockType.REFERENCE -> Callout(block.content, Icons.Outlined.Info, MaterialTheme.colorScheme.surfaceVariant)
    }
}

@Composable
private fun BulletList(content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        content.lines().map(String::trim).filter(String::isNotBlank).forEach { line ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("•", fontWeight = FontWeight.Bold)
                Text(line.removePrefix("-").trim())
            }
        }
    }
}

@Composable
private fun SimpleTable(content: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            content.lines().filter(String::isNotBlank).forEachIndexed { index, row ->
                val cells = row.trim().trim('|').split('|').map(String::trim)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    cells.forEach { cell ->
                        Text(cell, Modifier.weight(1f), fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeBlock(content: String, language: String?) {
    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Code, null)
                Text(language ?: "code", style = MaterialTheme.typography.labelMedium)
            }
            SelectionContainer { Text(content, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
private fun Callout(content: String, icon: ImageVector, background: Color) {
    Row(Modifier.fillMaxWidth().background(background, RoundedCornerShape(12.dp)).padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, null)
        Text(content, Modifier.weight(1f))
    }
}

@Composable
private fun AssetPlaceholder(block: LessonBlock) {
    Card(Modifier.fillMaxWidth()) {
        Text(block.accessibilityLabel ?: block.content.ifBlank { "نمایش فایل آموزشی" }, Modifier.padding(24.dp))
    }
}

@Composable
private fun ActionBlock(buttonText: String, description: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(description)
            Button(onClick = onClick) { Text(buttonText) }
        }
    }
}
