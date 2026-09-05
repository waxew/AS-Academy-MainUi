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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asdevelopers.academy.core.content.CourseBundle
import com.asdevelopers.academy.course.model.Lesson

/** Data-driven shared home screen. Curriculum data always comes from the selected Course Package. */
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
                onOpenPlacement?.let { action -> Button(action, Modifier.fillMaxWidth()) { Text("آزمون تعیین سطح") } }
                onOpenLearningCatalog?.let { action -> Button(action, Modifier.fillMaxWidth()) { Text("تمرین، آزمون و پروژه") } }
                onOpenWeakReview?.let { action -> Button(action, Modifier.fillMaxWidth()) { Text("مرور نقاط ضعف") } }
                onOpenFlashcards?.let { action -> Button(action, Modifier.fillMaxWidth()) { Text("مرور فلش‌کارت") } }
            }
        }
        items(bundle.lessons, key = { it.id }) { lesson ->
            Button(onClick = { onOpenLesson(lesson.id) }, modifier = Modifier.fillMaxWidth()) { Text(lesson.title) }
        }
        item { Column(Modifier.padding(bottom = 24.dp)) {} }
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
    AcademyLessonRenderer(
        lesson = lesson,
        modifier = modifier.fillMaxSize().padding(20.dp),
        onExerciseClick = onExerciseClick,
        onQuizClick = onQuizClick,
        onProjectClick = onProjectClick
    )
}

/** Shared searchable activity entry surface; activity execution stays in MainUi renderers/Core engines. */
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
        item { Text("تمرین، آزمون و پروژه", style = MaterialTheme.typography.headlineMedium) }
        if (bundle.quizzes.isEmpty() && bundle.exercises.isEmpty() && bundle.projects.isEmpty()) {
            item { Text("فعالیتی برای این دوره تعریف نشده است.") }
        }
        items(bundle.quizzes, key = { "quiz:${it.id}" }) { quiz ->
            CatalogCard("آزمون", quiz.title) { onQuizClick(quiz.id) }
        }
        items(bundle.exercises, key = { "exercise:${it.id}" }) { exercise ->
            CatalogCard("تمرین", exercise.title) { onExerciseClick(exercise.id) }
        }
        items(bundle.projects, key = { "project:${it.id}" }) { project ->
            CatalogCard("پروژه", project.title) { onProjectClick(project.id) }
        }
    }
}

@Composable
private fun CatalogCard(kind: String, title: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(kind, style = MaterialTheme.typography.labelLarge)
            Text(title, style = MaterialTheme.typography.titleMedium)
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text("باز کردن") }
        }
    }
}

@Composable
fun AcademyMainUiLoading(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun AcademyMainUiMessage(message: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) { Text(message) }
}
