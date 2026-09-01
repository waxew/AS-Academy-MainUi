package com.asdevelopers.academy.mainui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asdevelopers.academy.core.content.CourseBundle
import com.asdevelopers.academy.core.settings.AcademyProfile
import com.asdevelopers.academy.core.ui.components.AcademyAppShell
import com.asdevelopers.academy.core.ui.components.AcademyDrawerItem
import com.asdevelopers.academy.core.ui.content.LessonRenderer
import com.asdevelopers.academy.core.ui.screens.AcademyLearningCatalogScreen
import com.asdevelopers.academy.core.ui.theme.AcademyTheme
import com.asdevelopers.academy.course.model.CourseBranding
import com.asdevelopers.academy.course.model.Lesson

/**
 * Transitional public MainUi facade.
 *
 * Course applications import shared visual primitives from this package instead of reaching into Core UI.
 * Core remains the engine/runtime underneath MainUi while older Core visual APIs stay available during migration.
 */
@Composable
fun AcademyMainUiTheme(
    branding: CourseBranding,
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    AcademyTheme(
        branding = branding,
        darkTheme = darkTheme,
        content = content
    )
}

/** MainUi owns the common app shell while the Course host only supplies configuration and actions. */
@Composable
fun AcademyMainUiShell(
    title: String,
    profile: AcademyProfile,
    courseItems: List<AcademyDrawerItem>,
    onProfileImageClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onShareClick: () -> Unit,
    onAboutClick: () -> Unit,
    contentIsRtl: Boolean = true,
    content: @Composable (PaddingValues) -> Unit
) {
    AcademyAppShell(
        title = title,
        profile = profile,
        courseItems = courseItems,
        onProfileImageClick = onProfileImageClick,
        onSettingsClick = onSettingsClick,
        onShareClick = onShareClick,
        onAboutClick = onAboutClick,
        contentIsRtl = contentIsRtl,
        content = content
    )
}

/**
 * Data-driven shared home screen. No course title, lesson ID or curriculum text is hard-coded here.
 * The same screen can render Basic, Kotlin, Python and future Course Packages.
 */
@Composable
fun AcademyCourseHomeScreen(
    bundle: CourseBundle,
    onOpenLesson: (String) -> Unit,
    onOpenPlacement: (() -> Unit)? = null,
    onOpenWeakReview: (() -> Unit)? = null,
    onOpenFlashcards: (() -> Unit)? = null,
    onOpenLearningCatalog: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = bundle.manifest.titleFa,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "${bundle.levels.size} سطح • ${bundle.chapters.size} فصل • ${bundle.lessons.size} درس"
                )
                onOpenPlacement?.let { action ->
                    Button(onClick = action, modifier = Modifier.fillMaxWidth()) {
                        Text("آزمون تعیین سطح")
                    }
                }
                onOpenLearningCatalog?.let { action ->
                    Button(onClick = action, modifier = Modifier.fillMaxWidth()) {
                        Text("تمرین، آزمون و پروژه")
                    }
                }
                onOpenWeakReview?.let { action ->
                    Button(onClick = action, modifier = Modifier.fillMaxWidth()) {
                        Text("مرور نقاط ضعف")
                    }
                }
                onOpenFlashcards?.let { action ->
                    Button(onClick = action, modifier = Modifier.fillMaxWidth()) {
                        Text("مرور فلش‌کارت")
                    }
                }
            }
        }

        items(
            items = bundle.lessons,
            key = { lesson -> lesson.id }
        ) { lesson ->
            Button(
                onClick = { onOpenLesson(lesson.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(lesson.title)
            }
        }

        item {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {}
        }
    }
}

/**
 * Shared Lesson Reader. Course apps only select a Lesson ID; all block rendering remains centralized.
 */
@Composable
fun AcademyLessonReaderScreen(
    lesson: Lesson,
    onExerciseClick: (String) -> Unit = {},
    onQuizClick: (String) -> Unit = {},
    onProjectClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LessonRenderer(
        lesson = lesson,
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        onExerciseClick = onExerciseClick,
        onQuizClick = onQuizClick,
        onProjectClick = onProjectClick
    )
}

/** Shared searchable/filterable activity catalog backed by Core models and navigation callbacks. */
@Composable
fun AcademyCourseLearningCatalog(
    bundle: CourseBundle,
    onQuizClick: (String) -> Unit,
    onExerciseClick: (String) -> Unit,
    onProjectClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AcademyLearningCatalogScreen(
        quizzes = bundle.quizzes,
        exercises = bundle.exercises,
        projects = bundle.projects,
        modifier = modifier,
        onQuizClick = onQuizClick,
        onExerciseClick = onExerciseClick,
        onProjectClick = onProjectClick
    )
}

/** Shared full-screen loading state used while a Course Package is being loaded or validated. */
@Composable
fun AcademyMainUiLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

/** Shared error/empty state keeps Course hosts free of one-off presentation code. */
@Composable
fun AcademyMainUiMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(message)
    }
}
