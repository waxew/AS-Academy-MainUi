package com.asdevelopers.academy.mainui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.asdevelopers.academy.core.exercise.Exercise
import com.asdevelopers.academy.core.exercise.ExerciseDraft
import com.asdevelopers.academy.core.progress.LearningCompletion
import com.asdevelopers.academy.core.progress.LearningTargetType
import com.asdevelopers.academy.core.project.LearningProject
import com.asdevelopers.academy.core.ui.screens.AcademyExerciseScreen
import com.asdevelopers.academy.core.ui.screens.AcademyProjectScreen
import kotlinx.coroutines.launch

/** MainUi host that restores and autosaves an exercise answer through Core repositories. */
@Composable
fun AcademyTrackedExerciseScreen(
    courseId: String,
    exercise: Exercise,
    runtime: AcademyMainUiRuntime,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val savedDraft by runtime.exerciseDraftRepository
        .observe(courseId, exercise.id)
        .collectAsState(initial = null)

    AcademyExerciseScreen(
        exercise = exercise,
        modifier = modifier,
        initialAnswer = savedDraft?.answer.orEmpty(),
        onDraftChanged = { answer ->
            scope.launch {
                runtime.exerciseDraftRepository.save(
                    ExerciseDraft(
                        courseId = courseId,
                        exerciseId = exercise.id,
                        answer = answer,
                        updatedAtEpochMillis = System.currentTimeMillis()
                    )
                )
            }
        },
        onCompleted = { answer ->
            scope.launch {
                val now = System.currentTimeMillis()
                runtime.exerciseDraftRepository.save(
                    ExerciseDraft(
                        courseId = courseId,
                        exerciseId = exercise.id,
                        answer = answer,
                        updatedAtEpochMillis = now
                    )
                )
                runtime.learningCompletionRepository.save(
                    LearningCompletion(
                        courseId = courseId,
                        targetType = LearningTargetType.EXERCISE,
                        targetId = exercise.id,
                        completed = true,
                        completedAtEpochMillis = now
                    )
                )
            }
        }
    )
}

/** MainUi host that restores/saves project milestones and keeps generic completion in sync. */
@Composable
fun AcademyTrackedProjectScreen(
    courseId: String,
    project: LearningProject,
    runtime: AcademyMainUiRuntime,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val normalizedProject = remember(courseId, project) {
        if (project.courseId == courseId) project else project.copy(courseId = courseId)
    }
    val savedProgress by runtime.projectProgressRepository
        .observe(courseId, project.id)
        .collectAsState(initial = null)

    AcademyProjectScreen(
        project = normalizedProject,
        progress = savedProgress,
        modifier = modifier,
        onProgressChanged = { next ->
            scope.launch {
                runtime.projectProgressRepository.save(next)
                val now = System.currentTimeMillis()
                runtime.learningCompletionRepository.save(
                    LearningCompletion(
                        courseId = courseId,
                        targetType = LearningTargetType.PROJECT,
                        targetId = project.id,
                        completed = next.completedAtEpochMillis != null,
                        completedAtEpochMillis = next.completedAtEpochMillis ?: now
                    )
                )
            }
        }
    )
}
