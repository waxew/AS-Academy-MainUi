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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asdevelopers.academy.core.exercise.Exercise
import com.asdevelopers.academy.core.project.LearningProject
import com.asdevelopers.academy.core.project.ProjectProgress
import com.asdevelopers.academy.core.quiz.Quiz
import com.asdevelopers.academy.core.quiz.QuizScore
import com.asdevelopers.academy.core.review.Flashcard
import com.asdevelopers.academy.core.review.LessonReviewRecommendation
import com.asdevelopers.academy.core.review.PlacementRecommendation
import com.asdevelopers.academy.core.review.ReviewRating
import com.asdevelopers.academy.core.settings.AcademySettings
import com.asdevelopers.academy.core.settings.AcademyThemeMode
import com.asdevelopers.academy.course.model.CourseLevelType
import com.asdevelopers.academy.course.model.Lesson

@Composable
fun AcademyMainUiLessonScreen(
    lesson: Lesson,
    modifier: Modifier = Modifier,
    onExerciseClick: (String) -> Unit = {},
    onQuizClick: (String) -> Unit = {},
    onProjectClick: (String) -> Unit = {}
) {
    FoundationLessonRenderer(
        lesson = lesson,
        modifier = modifier,
        onExerciseClick = onExerciseClick,
        onQuizClick = onQuizClick,
        onProjectClick = onProjectClick
    )
}

@Composable
fun AcademyMainUiSettingsScreen(
    settings: AcademySettings,
    onThemeChanged: (AcademyThemeMode) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onFontScaleChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Text("تنظیمات", style = MaterialTheme.typography.headlineMedium) }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("پوسته", style = MaterialTheme.typography.titleMedium)
                    AcademyThemeMode.entries.forEach { mode ->
                        Button(
                            onClick = { onThemeChanged(mode) },
                            enabled = settings.themeMode != mode,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(mode.name) }
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("اعلان مطالعه", style = MaterialTheme.typography.titleMedium)
                    Switch(
                        checked = settings.notificationsEnabled,
                        onCheckedChange = onNotificationsChanged
                    )
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("اندازه متن: ${(settings.fontScale * 100).toInt()}٪")
                    Slider(
                        value = settings.fontScale,
                        onValueChange = onFontScaleChanged,
                        valueRange = 0.85f..1.35f
                    )
                }
            }
        }
    }
}

@Composable
fun AcademyMainUiAboutScreen(
    appTitle: String,
    description: String,
    versionName: String,
    supportEmail: String = "AS.Developers.Support@Gmail.Com",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(appTitle, style = MaterialTheme.typography.headlineMedium)
        Text(description)
        Text("نسخه $versionName")
        Text("پشتیبانی: $supportEmail")
    }
}

@Composable
fun AcademyMainUiQuizScreen(
    quiz: Quiz,
    modifier: Modifier = Modifier,
    onCompleted: (QuizScore) -> Unit = {}
) = AcademyQuizRenderer(quiz = quiz, modifier = modifier, onCompleted = onCompleted)

@Composable
fun AcademyMainUiExerciseScreen(
    exercise: Exercise,
    modifier: Modifier = Modifier,
    initialAnswer: String = "",
    onDraftChanged: (String) -> Unit = {},
    onCompleted: (String) -> Unit = {}
) = AcademyExerciseRenderer(
    exercise = exercise,
    modifier = modifier,
    initialAnswer = initialAnswer,
    onDraftChanged = onDraftChanged,
    onCompleted = onCompleted
)

@Composable
fun AcademyMainUiProjectScreen(
    project: LearningProject,
    progress: ProjectProgress? = null,
    modifier: Modifier = Modifier,
    onProgressChanged: (ProjectProgress) -> Unit = {}
) = AcademyProjectRenderer(
    project = project,
    progress = progress,
    modifier = modifier,
    onProgressChanged = onProgressChanged
)

@Composable
fun AcademyMainUiPlacementSummaryScreen(
    recommendation: PlacementRecommendation,
    weakTags: Set<String>,
    modifier: Modifier = Modifier,
    onStartLevel: (CourseLevelType) -> Unit,
    onReviewWeakTopics: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("نتیجه تعیین سطح", style = MaterialTheme.typography.headlineMedium)
        Text(recommendation.toString())
        if (weakTags.isNotEmpty()) Text("موضوعات نیازمند مرور: ${weakTags.joinToString("، ")}")
        Button(onClick = onReviewWeakTopics, modifier = Modifier.fillMaxWidth()) { Text("مرور نقاط ضعف") }
        Text("شروع سطح از مسیر Host انجام می‌شود.")
        @Suppress("UNUSED_VARIABLE") val startLevelCallback = onStartLevel
    }
}

@Composable
fun AcademyMainUiWeakTopicReviewScreen(
    recommendations: List<LessonReviewRecommendation>,
    modifier: Modifier = Modifier,
    onLessonClick: (lessonId: String) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Text("مرور نقاط ضعف", style = MaterialTheme.typography.headlineMedium) }
        items(recommendations) { recommendation ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) { Text(recommendation.toString()) }
            }
        }
        item { @Suppress("UNUSED_VARIABLE") val callback = onLessonClick }
    }
}

@Composable
fun AcademyMainUiFlashcardReviewScreen(
    cards: List<Flashcard>,
    modifier: Modifier = Modifier,
    sessionTitle: String = "مرور فاصله‌دار",
    onRated: (Flashcard, ReviewRating) -> Unit,
    onSessionFinished: () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Text(sessionTitle, style = MaterialTheme.typography.headlineMedium) }
        items(cards) { card ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(card.toString())
                    ReviewRating.entries.forEach { rating ->
                        Button(onClick = { onRated(card, rating) }, modifier = Modifier.fillMaxWidth()) {
                            Text(rating.name)
                        }
                    }
                }
            }
        }
        item { Button(onClick = onSessionFinished, modifier = Modifier.fillMaxWidth()) { Text("پایان مرور") } }
    }
}
