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
import com.asdevelopers.academy.course.model.CourseBranding
import com.asdevelopers.academy.course.model.Lesson

/** MainUi is the authoritative presentation layer; Core supplies only runtime/data APIs. */
@Composable
fun AcademyMainUiTheme(
    branding: CourseBranding,
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    FoundationAcademyTheme(branding = branding, darkTheme = darkTheme, content = content)
}

@Composable
fun AcademyMainUiShell(
    title: String,
    profile: AcademyProfile,
    courseItems: List<AcademyMainUiDrawerItem>,
    onProfileImageClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onShareClick: () -> Unit,
    onAboutClick: () -> Unit,
    contentIsRtl: Boolean = true,
    content: @Composable (PaddingValues) -> Unit
) {
    @Suppress("UNUSED_VARIABLE")
    val rtl = contentIsRtl
    FoundationAcademyShell(
        title = title,
        profile = profile,
        courseItems = courseItems,
        onProfileImageClick = onProfileImageClick,
        onSettingsClick = onSettingsClick,
        onShareClick = onShareClick,
        onAboutClick = onAboutClick,
        content = content
    )
}

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
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(bundle.manifest.titleFa, style = MaterialTheme.typography.headlineMedium)
                Text("${bundle.levels.size} سطح • ${bundle.chapters.size} فصل • ${bundle.lessons.size} درس")
                onOpenPlacement?.let { action -> Button(onClick = action, modifier = Modifier.fillMaxWidth()) { Text("آزمون تعیین سطح") } }
                onOpenLearningCatalog?.let { action -> Button(onClick = action, modifier = Modifier.fillMaxWidth()) { Text("تمرین، آزمون و پروژه") } }
                onOpenWeakReview?.let { action -> Button(onClick = action, modifier = Modifier.fillMaxWidth()) { Text("مرور نقاط ضعف") } }
                onOpenFlashcards?.let { action -> Button(onClick = action, modifier = Modifier.fillMaxWidth()) { Text("مرور فلش‌کارت") } }
            }
        }
        items(bundle.lessons, key = { it.id }) { lesson ->
            Button(onClick = { onOpenLesson(lesson.id) }, modifier = Modifier.fillMaxWidth()) { Text(lesson.title) }
        }
        item { Column(modifier = Modifier.padding(bottom = 24.dp)) {} }
    }
}

@Composable
fun AcademyLessonReaderScreen(
    lesson: Lesson,
    onExerciseClick: (String) -> Unit = {},
    onQuizClick: (String) -> Unit = {},
    onProjectClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    FoundationLessonRenderer(
        lesson = lesson,
        modifier = modifier.fillMaxSize().padding(20.dp),
        onExerciseClick = onExerciseClick,
        onQuizClick = onQuizClick,
        onProjectClick = onProjectClick
    )
}

@Composable
fun AcademyCourseLearningCatalog(
    bundle: CourseBundle,
    onQuizClick: (String) -> Unit,
    onExerciseClick: (String) -> Unit,
    onProjectClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Text("آزمون‌ها", style = MaterialTheme.typography.titleLarge) }
        items(bundle.quizzes, key = { it.id }) { quiz ->
            Button(onClick = { onQuizClick(quiz.id) }, modifier = Modifier.fillMaxWidth()) { Text(quiz.title) }
        }
        item { Text("تمرین‌ها", style = MaterialTheme.typography.titleLarge) }
        items(bundle.exercises, key = { it.id }) { exercise ->
            Button(onClick = { onExerciseClick(exercise.id) }, modifier = Modifier.fillMaxWidth()) { Text(exercise.title) }
        }
        item { Text("پروژه‌ها", style = MaterialTheme.typography.titleLarge) }
        items(bundle.projects, key = { it.id }) { project ->
            Button(onClick = { onProjectClick(project.id) }, modifier = Modifier.fillMaxWidth()) { Text(project.title) }
        }
    }
}

@Composable
fun AcademyMainUiLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) { CircularProgressIndicator() }
}

@Composable
fun AcademyMainUiMessage(message: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text(message)
    }
}
