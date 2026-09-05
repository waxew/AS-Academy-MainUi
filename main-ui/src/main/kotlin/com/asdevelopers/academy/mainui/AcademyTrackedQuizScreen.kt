package com.asdevelopers.academy.mainui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asdevelopers.academy.core.quiz.Quiz
import kotlinx.coroutines.launch

/**
 * MainUi-owned quiz host that records each completed attempt through Core's QuizHistoryRepository.
 * Scoring remains inside Core QuizEngine while MainUi owns presentation and persistence wiring.
 */
@Composable
fun AcademyTrackedQuizScreen(
    courseId: String,
    quiz: Quiz,
    runtime: AcademyMainUiRuntime,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        Button(
            onClick = onOpenHistory,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("تاریخچه این آزمون")
        }

        AcademyQuizRenderer(
            quiz = quiz,
            modifier = Modifier.fillMaxSize(),
            onCompleted = { score ->
                scope.launch {
                    runtime.quizHistoryRepository.record(
                        quiz = quiz,
                        score = score,
                        completedAt = System.currentTimeMillis()
                    )
                }
            }
        )
    }
}
