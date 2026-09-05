package com.asdevelopers.academy.mainui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asdevelopers.academy.core.progress.LessonProgress
import com.asdevelopers.academy.core.progress.LessonStatus

/**
 * Course progress dashboard rendered from Core-owned persisted LessonProgress state.
 * MainUi computes presentation-only aggregates and does not mutate progress here.
 */
@Composable
fun AcademyCourseProgressScreen(
    lessonIds: List<String>,
    progress: List<LessonProgress>,
    lessonTitle: (String) -> String,
    onOpenLesson: (String) -> Unit,
    onBack: () -> Unit
) {
    val progressByLesson = progress.associateBy { it.lessonId }
    val lessonProgress = lessonIds.mapNotNull(progressByLesson::get)
    val completed = lessonProgress.count { it.status == LessonStatus.COMPLETED }
    val inProgress = lessonProgress.count {
        it.status == LessonStatus.IN_PROGRESS || it.status == LessonStatus.NEEDS_REVIEW
    }
    val notStarted = (lessonIds.size - completed - inProgress).coerceAtLeast(0)
    val averagePercent = if (lessonIds.isEmpty()) 0 else {
        lessonIds.sumOf { progressByLesson[it]?.progressPercent ?: 0 }
            .div(lessonIds.size)
            .coerceIn(0, 100)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Button(onClick = onBack) { Text("بازگشت") } }
        item {
            Text("پیشرفت یادگیری", style = MaterialTheme.typography.headlineMedium)
            Text("میانگین پیشرفت دوره: $averagePercent٪")
        }
        item {
            LinearProgressIndicator(
                progress = { averagePercent / 100f },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("${lessonIds.size} درس", style = MaterialTheme.typography.titleMedium)
                    Text("$completed تکمیل‌شده • $inProgress در حال یادگیری • $notStarted شروع‌نشده")
                }
            }
        }
        if (lessonIds.isEmpty()) {
            item { Text("درسی برای نمایش پیشرفت وجود ندارد.") }
        } else {
            items(lessonIds, key = { it }) { lessonId ->
                val item = progressByLesson[lessonId]
                val percent = item?.progressPercent?.coerceIn(0, 100) ?: 0
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(lessonTitle(lessonId), style = MaterialTheme.typography.titleMedium)
                        Text(
                            when (item?.status) {
                                LessonStatus.COMPLETED -> "تکمیل‌شده"
                                LessonStatus.NEEDS_REVIEW -> "نیازمند مرور • $percent٪"
                                LessonStatus.IN_PROGRESS -> "در حال یادگیری • $percent٪"
                                else -> "شروع‌نشده"
                            }
                        )
                        LinearProgressIndicator(
                            progress = { percent / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { onOpenLesson(lessonId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (percent > 0) "ادامه درس" else "شروع درس")
                        }
                    }
                }
            }
        }
    }
}
