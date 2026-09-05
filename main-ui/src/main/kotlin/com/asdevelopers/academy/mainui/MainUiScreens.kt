package com.asdevelopers.academy.mainui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
) = AcademyLessonRenderer(lesson, modifier, onExerciseClick, onQuizClick, onProjectClick)

/** Compatibility settings surface for thin hosts that manage callbacks themselves. */
@Composable
fun AcademyMainUiSettingsScreen(
    settings: AcademySettings,
    onThemeChanged: (AcademyThemeMode) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onFontScaleChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("تنظیمات", style = MaterialTheme.typography.headlineMedium)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("پوسته", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AcademyThemeMode.entries.forEach { mode ->
                        Button(onClick = { onThemeChanged(mode) }, enabled = settings.themeMode != mode) {
                            Text(when (mode) { AcademyThemeMode.SYSTEM -> "سیستم"; AcademyThemeMode.LIGHT -> "روشن"; AcademyThemeMode.DARK -> "تیره" })
                        }
                    }
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("اعلان‌ها", style = MaterialTheme.typography.titleMedium)
                Switch(checked = settings.notificationsEnabled, onCheckedChange = onNotificationsChanged)
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("اندازه متن: ${(settings.fontScale * 100).toInt()}٪")
                Slider(value = settings.fontScale, onValueChange = onFontScaleChanged, valueRange = 0.85f..1.35f)
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
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("درباره نرم‌افزار", style = MaterialTheme.typography.headlineMedium)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(appTitle, style = MaterialTheme.typography.titleLarge)
                if (description.isNotBlank()) Text(description)
                if (versionName.isNotBlank()) Text("نسخه $versionName")
                Text("پشتیبانی: $supportEmail")
            }
        }
    }
}

@Composable
fun AcademyMainUiQuizScreen(
    quiz: Quiz,
    modifier: Modifier = Modifier,
    onCompleted: (QuizScore) -> Unit = {}
) = AcademyQuizRenderer(quiz, modifier, onCompleted)

@Composable
fun AcademyMainUiExerciseScreen(
    exercise: Exercise,
    modifier: Modifier = Modifier,
    initialAnswer: String = "",
    onDraftChanged: (String) -> Unit = {},
    onCompleted: (String) -> Unit = {}
) = AcademyExerciseRenderer(exercise, modifier, initialAnswer, onDraftChanged, onCompleted)

@Composable
fun AcademyMainUiProjectScreen(
    project: LearningProject,
    progress: ProjectProgress? = null,
    modifier: Modifier = Modifier,
    onProgressChanged: (ProjectProgress) -> Unit = {}
) = AcademyProjectRenderer(project, progress, modifier, onProgressChanged)

@Composable
fun AcademyMainUiPlacementSummaryScreen(
    recommendation: PlacementRecommendation,
    weakTags: Set<String>,
    modifier: Modifier = Modifier,
    onStartLevel: (CourseLevelType) -> Unit,
    onReviewWeakTopics: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("نتیجه تعیین سطح", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("امتیاز ${recommendation.scorePercent}٪", style = MaterialTheme.typography.headlineSmall)
                LinearProgressIndicator(progress = { recommendation.scorePercent / 100f }, modifier = Modifier.fillMaxWidth())
                Text("پیشنهاد شروع: ${recommendation.title}", style = MaterialTheme.typography.titleLarge)
                Text(if (recommendation.fastTrack) "مسیر فشرده برای این نتیجه مناسب است." else "این سطح نقطه شروع پیشنهادی شماست.")
            }
        }
        if (weakTags.isNotEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("موضوع‌های نیازمند مرور", style = MaterialTheme.typography.titleMedium)
                    Text(weakTags.sorted().joinToString("، "))
                    if (recommendation.reviewWeakTopics) {
                        OutlinedButton(onClick = onReviewWeakTopics, modifier = Modifier.fillMaxWidth()) { Text("مشاهده برنامه مرور") }
                    }
                }
            }
        }
        Button(onClick = { onStartLevel(recommendation.levelType) }, modifier = Modifier.fillMaxWidth()) { Text("شروع ${recommendation.title}") }
    }
}

@Composable
fun AcademyMainUiWeakTopicReviewScreen(
    recommendations: List<LessonReviewRecommendation>,
    modifier: Modifier = Modifier,
    onLessonClick: (lessonId: String) -> Unit
) {
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("مرور نقاط ضعف", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        if (recommendations.isEmpty()) {
            Text("فعلاً ضعف تکرارشونده‌ای ثبت نشده است.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(recommendations, key = LessonReviewRecommendation::lessonId) { item ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            Text("اولویت ${item.priority}")
                            Text("موضوع‌ها: ${item.matchedTags.sorted().joinToString("، ")}")
                            Button(onClick = { onLessonClick(item.lessonId) }, modifier = Modifier.fillMaxWidth()) { Text("مرور این درس") }
                        }
                    }
                }
            }
        }
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
    var currentIndex by remember(cards.map(Flashcard::id)) { mutableStateOf(0) }
    var answerVisible by remember(cards.map(Flashcard::id), currentIndex) { mutableStateOf(false) }
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(sessionTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        if (cards.isEmpty()) { Text("در حال حاضر کارتی برای مرور وجود ندارد."); return@Column }
        if (currentIndex >= cards.size) {
            LinearProgressIndicator(progress = { 1f }, modifier = Modifier.fillMaxWidth())
            Text("مرور این جلسه کامل شد.")
            Button(onClick = onSessionFinished, modifier = Modifier.fillMaxWidth()) { Text("پایان جلسه") }
            return@Column
        }
        val card = cards[currentIndex]
        LinearProgressIndicator(progress = { currentIndex.toFloat() / cards.size }, modifier = Modifier.fillMaxWidth())
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(card.front, style = MaterialTheme.typography.headlineSmall)
                if (!answerVisible) {
                    OutlinedButton(onClick = { answerVisible = true }, modifier = Modifier.fillMaxWidth()) { Text("نمایش پاسخ") }
                } else {
                    Text(card.back)
                }
            }
        }
        if (answerVisible) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReviewRating.entries.forEach { rating ->
                    OutlinedButton(
                        onClick = { onRated(card, rating); answerVisible = false; currentIndex += 1 },
                        modifier = Modifier.weight(1f)
                    ) { Text(rating.name) }
                }
            }
        }
    }
}
