package com.asdevelopers.academy.mainui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asdevelopers.academy.core.database.QuizResultEntity
import com.asdevelopers.academy.core.settings.AcademyThemeMode
import kotlinx.coroutines.launch

@Composable
fun AcademySettingsScreen(
    runtime: AcademyMainUiRuntime,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val settings by runtime.preferencesRepository.settings.collectAsState(
        initial = com.asdevelopers.academy.core.settings.AcademySettings()
    )

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        scope.launch {
            runtime.preferencesRepository.setNotificationsEnabled(granted)
            if (granted) {
                runtime.studyReminderScheduler.scheduleEvery(
                    days = 1,
                    title = "AS Academy",
                    message = "زمان ادامه مسیر یادگیری است."
                )
            } else {
                runtime.studyReminderScheduler.cancel()
            }
        }
    }

    LaunchedEffect(settings.notificationsEnabled) {
        if (!settings.notificationsEnabled) {
            runtime.studyReminderScheduler.cancel()
        } else if (runtime.studyReminderScheduler.canPostNotifications()) {
            runtime.studyReminderScheduler.scheduleEvery(
                days = 1,
                title = "AS Academy",
                message = "زمان ادامه مسیر یادگیری است."
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Button(onClick = onBack) { Text("بازگشت") } }
        item { Text("تنظیمات", style = MaterialTheme.typography.headlineMedium) }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("پوسته", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AcademyThemeMode.entries.forEach { mode ->
                            Button(
                                onClick = { scope.launch { runtime.preferencesRepository.setThemeMode(mode) } },
                                enabled = settings.themeMode != mode
                            ) {
                                Text(
                                    when (mode) {
                                        AcademyThemeMode.SYSTEM -> "سیستم"
                                        AcademyThemeMode.LIGHT -> "روشن"
                                        AcademyThemeMode.DARK -> "تیره"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("یادآور مطالعه", style = MaterialTheme.typography.titleMedium)
                        Text(
                            when {
                                !settings.notificationsEnabled -> "غیرفعال"
                                runtime.studyReminderScheduler.canPostNotifications() -> "فعال • روزانه"
                                else -> "برای فعال‌سازی، مجوز اعلان لازم است"
                            }
                        )
                    }
                    Switch(
                        checked = settings.notificationsEnabled && runtime.studyReminderScheduler.canPostNotifications(),
                        onCheckedChange = { enabled ->
                            if (!enabled) {
                                scope.launch {
                                    runtime.studyReminderScheduler.cancel()
                                    runtime.preferencesRepository.setNotificationsEnabled(false)
                                }
                            } else if (runtime.studyReminderScheduler.canPostNotifications()) {
                                scope.launch {
                                    runtime.studyReminderScheduler.scheduleEvery(
                                        days = 1,
                                        title = "AS Academy",
                                        message = "زمان ادامه مسیر یادگیری است."
                                    )
                                    runtime.preferencesRepository.setNotificationsEnabled(true)
                                }
                            } else {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("اندازه متن", style = MaterialTheme.typography.titleMedium)
                    Text("${(settings.fontScale * 100).toInt()}٪")
                    Slider(
                        value = settings.fontScale,
                        onValueChange = { value ->
                            scope.launch { runtime.preferencesRepository.setFontScale(value) }
                        },
                        valueRange = 0.85f..1.35f
                    )
                }
            }
        }
    }
}

@Composable
fun AcademyQuizHistoryScreen(
    courseId: String,
    quizId: String,
    quizTitle: String,
    runtime: AcademyMainUiRuntime,
    onBack: () -> Unit
) {
    val attempts by runtime.quizHistoryRepository.observeQuiz(courseId, quizId)
        .collectAsState(initial = emptyList())
    val sorted = attempts.sortedByDescending(QuizResultEntity::completedAt)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Button(onClick = onBack) { Text("بازگشت") } }
        item {
            Text("تاریخچه آزمون", style = MaterialTheme.typography.headlineMedium)
            Text(quizTitle)
            Text("${sorted.size} تلاش ثبت‌شده")
        }
        if (sorted.isEmpty()) {
            item { Text("هنوز نتیجه‌ای برای این آزمون ثبت نشده است.") }
        } else {
            items(sorted, key = { it.attemptId }) { attempt ->
                QuizAttemptCard(attempt)
            }
        }
    }
}

@Composable
private fun QuizAttemptCard(attempt: QuizResultEntity) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("امتیاز: ${attempt.scorePercent}٪", style = MaterialTheme.typography.titleMedium)
            Text("صحیح: ${attempt.correctCount} • غلط: ${attempt.wrongCount}")
            if (attempt.weakTags.isNotBlank()) {
                Text("موضوعات نیازمند مرور: ${attempt.weakTags.replace('|', '،')}")
            }
        }
    }
}
