package com.asdevelopers.academy.mainui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.asdevelopers.academy.core.ui.components.AcademyDrawerItem
import com.asdevelopers.academy.core.ui.screens.AcademyAboutScreen
import com.asdevelopers.academy.core.ui.screens.AcademyFlashcardReviewScreen
import com.asdevelopers.academy.core.ui.screens.AcademyPlacementSummaryScreen
import com.asdevelopers.academy.core.ui.screens.AcademySettingsScreen
import com.asdevelopers.academy.core.ui.screens.AcademyWeakTopicReviewScreen
import com.asdevelopers.academy.core.ui.theme.DefaultAcademyBranding
import com.asdevelopers.academy.course.model.CourseBranding
import com.asdevelopers.academy.course.model.CourseLevelType
import com.asdevelopers.academy.course.model.Lesson

/**
 * Presentation-facing alias for Drawer items.
 * Course Apps should import this name from MainUi rather than treating Core UI packages as a public API.
 */
typealias AcademyMainUiDrawerItem = AcademyDrawerItem

/** Default visual fallback while a Course Package is still loading. */
val DefaultMainUiBranding: CourseBranding
    get() = DefaultAcademyBranding

/** Shared lesson reader owned by MainUi. */
@Composable
fun AcademyMainUiLessonScreen(
    lesson: Lesson,
    modifier: Modifier = Modifier,
    onExerciseClick: (String) -> Unit = {},
    onQuizClick: (String) -> Unit = {},
    onProjectClick: (String) -> Unit = {}
) {
    AcademyLessonRenderer(
        lesson = lesson,
        modifier = modifier,
        onExerciseClick = onExerciseClick,
        onQuizClick = onQuizClick,
        onProjectClick = onProjectClick
    )
}

/** Shared settings presentation; persistence stays in Core repositories supplied by the host. */
@Composable
fun AcademyMainUiSettingsScreen(
    settings: AcademySettings,
    onThemeChanged: (AcademyThemeMode) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onFontScaleChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    AcademySettingsScreen(
        settings = settings,
        onThemeChanged = onThemeChanged,
        onNotificationsChanged = onNotificationsChanged,
        onFontScaleChanged = onFontScaleChanged,
        modifier = modifier
    )
}

/** Shared About surface with the AS Academy support/footer convention. */
@Composable
fun AcademyMainUiAboutScreen(
    appTitle: String,
    description: String,
    versionName: String,
    supportEmail: String = "AS.Developers.Support@Gmail.Com",
    modifier: Modifier = Modifier
) {
    AcademyAboutScreen(
        appTitle = appTitle,
        description = description,
        versionName = versionName,
        supportEmail = supportEmail,
        modifier = modifier
    )
}

/** Shared quiz presentation; grading remains inside the Core quiz engine. */
@Composable
fun AcademyMainUiQuizScreen(
    quiz: Quiz,
    modifier: Modifier = Modifier,
    onCompleted: (QuizScore) -> Unit = {}
) {
    AcademyQuizRenderer(
        quiz = quiz,
        modifier = modifier,
        onCompleted = onCompleted
    )
}

/** Shared exercise authoring/answer surface owned by MainUi. */
@Composable
fun AcademyMainUiExerciseScreen(
    exercise: Exercise,
    modifier: Modifier = Modifier,
    initialAnswer: String = "",
    onDraftChanged: (String) -> Unit = {},
    onCompleted: (String) -> Unit = {}
) {
    AcademyExerciseRenderer(
        exercise = exercise,
        modifier = modifier,
        initialAnswer = initialAnswer,
        onDraftChanged = onDraftChanged,
        onCompleted = onCompleted
    )
}

/** Shared project milestone/draft surface owned by MainUi. */
@Composable
fun AcademyMainUiProjectScreen(
    project: LearningProject,
    progress: ProjectProgress? = null,
    modifier: Modifier = Modifier,
    onProgressChanged: (ProjectProgress) -> Unit = {}
) {
    AcademyProjectRenderer(
        project = project,
        progress = progress,
        modifier = modifier,
        onProgressChanged = onProgressChanged
    )
}

/** Shared placement result screen. */
@Composable
fun AcademyMainUiPlacementSummaryScreen(
    recommendation: PlacementRecommendation,
    weakTags: Set<String>,
    modifier: Modifier = Modifier,
    onStartLevel: (CourseLevelType) -> Unit,
    onReviewWeakTopics: () -> Unit = {}
) {
    AcademyPlacementSummaryScreen(
        recommendation = recommendation,
        weakTags = weakTags,
        modifier = modifier,
        onStartLevel = onStartLevel,
        onReviewWeakTopics = onReviewWeakTopics
    )
}

/** Shared weak-topic review queue. */
@Composable
fun AcademyMainUiWeakTopicReviewScreen(
    recommendations: List<LessonReviewRecommendation>,
    modifier: Modifier = Modifier,
    onLessonClick: (lessonId: String) -> Unit
) {
    AcademyWeakTopicReviewScreen(
        recommendations = recommendations,
        modifier = modifier,
        onLessonClick = onLessonClick
    )
}

/** Shared spaced-review session. */
@Composable
fun AcademyMainUiFlashcardReviewScreen(
    cards: List<Flashcard>,
    modifier: Modifier = Modifier,
    sessionTitle: String = "مرور فاصله‌دار",
    onRated: (Flashcard, ReviewRating) -> Unit,
    onSessionFinished: () -> Unit = {}
) {
    AcademyFlashcardReviewScreen(
        cards = cards,
        modifier = modifier,
        sessionTitle = sessionTitle,
        onRated = onRated,
        onSessionFinished = onSessionFinished
    )
}
