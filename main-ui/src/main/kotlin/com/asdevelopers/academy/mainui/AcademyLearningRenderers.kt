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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asdevelopers.academy.core.exercise.Exercise
import com.asdevelopers.academy.core.project.LearningProject
import com.asdevelopers.academy.core.project.ProjectMilestone
import com.asdevelopers.academy.core.project.ProjectProgress
import com.asdevelopers.academy.core.quiz.QuestionSubmission
import com.asdevelopers.academy.core.quiz.QuestionType
import com.asdevelopers.academy.core.quiz.Quiz
import com.asdevelopers.academy.core.quiz.QuizAnswer
import com.asdevelopers.academy.core.quiz.QuizEngine
import com.asdevelopers.academy.core.quiz.QuizQuestion
import com.asdevelopers.academy.core.quiz.QuizScore

/** MainUi-owned quiz presentation. Scoring remains exclusively in Core QuizEngine. */
@Composable
fun AcademyQuizRenderer(
    quiz: Quiz,
    modifier: Modifier = Modifier,
    onCompleted: (QuizScore) -> Unit = {}
) {
    val selectedAnswers = remember(quiz.id) { mutableStateMapOf<String, Set<String>>() }
    val textAnswers = remember(quiz.id) { mutableStateMapOf<String, String>() }
    val orderedAnswers = remember(quiz.id) {
        mutableStateMapOf<String, List<String>>().apply {
            quiz.questions.filter { it.type == QuestionType.ORDER_STEPS }
                .forEach { question -> put(question.id, question.answers.shuffled().map(QuizAnswer::id)) }
        }
    }
    val matchingAnswers = remember(quiz.id) { mutableStateMapOf<String, Map<String, String>>() }
    val displayedQuestions = remember(quiz.id) {
        if (quiz.shuffleQuestions) quiz.questions.shuffled() else quiz.questions
    }
    val displayedAnswers = remember(quiz.id) {
        quiz.questions.associate { question ->
            question.id to if (quiz.shuffleAnswers && question.type != QuestionType.ORDER_STEPS) {
                question.answers.shuffled()
            } else question.answers
        }
    }
    var score by remember(quiz.id) { mutableStateOf<QuizScore?>(null) }

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "quiz-header") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(quiz.title, style = MaterialTheme.typography.headlineMedium)
                Text("حد نصاب قبولی: ${quiz.passingScorePercent}٪")
                Text("تعداد سؤال: ${quiz.questions.size}")
            }
        }
        items(displayedQuestions, key = QuizQuestion::id) { question ->
            AcademyQuizQuestionCard(
                question = question,
                answers = displayedAnswers[question.id].orEmpty(),
                selectedIds = selectedAnswers[question.id].orEmpty(),
                textAnswer = textAnswers[question.id].orEmpty(),
                orderedIds = orderedAnswers[question.id].orEmpty(),
                matchedKeys = matchingAnswers[question.id].orEmpty(),
                locked = score != null,
                onSingleSelected = { answerId -> selectedAnswers[question.id] = setOf(answerId) },
                onMultiSelected = { answerId, checked ->
                    val current = selectedAnswers[question.id].orEmpty().toMutableSet()
                    if (checked) current += answerId else current -= answerId
                    selectedAnswers[question.id] = current
                },
                onTextChanged = { textAnswers[question.id] = it },
                onOrderChanged = { orderedAnswers[question.id] = it },
                onMatchChanged = { answerId, key ->
                    val current = matchingAnswers[question.id].orEmpty().toMutableMap()
                    current[answerId] = key
                    matchingAnswers[question.id] = current
                }
            )
        }
        item(key = "quiz-submit") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (score == null) {
                    Button(
                        onClick = {
                            val submissions = quiz.questions.map { question ->
                                QuestionSubmission(
                                    questionId = question.id,
                                    selectedAnswerIds = selectedAnswers[question.id].orEmpty(),
                                    textAnswer = textAnswers[question.id],
                                    orderedAnswerIds = orderedAnswers[question.id].orEmpty(),
                                    matchedAnswerKeys = matchingAnswers[question.id].orEmpty()
                                )
                            }
                            val resolved = QuizEngine.score(quiz, submissions)
                            score = resolved
                            onCompleted(resolved)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("ثبت پاسخ‌ها") }
                } else {
                    val resolved = score ?: return@Column
                    Text(
                        if (resolved.passed) "قبول شدید" else "نیاز به مرور دارید",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text("امتیاز: ${resolved.scorePercent}٪")
                    Text("پاسخ درست: ${resolved.correctQuestionIds.size} از ${quiz.questions.size}")
                    if (resolved.weakTags.isNotEmpty()) {
                        Text("موضوعات نیازمند مرور: ${resolved.weakTags.sorted().joinToString("، ")}")
                    }
                }
            }
        }
    }
}

@Composable
private fun AcademyQuizQuestionCard(
    question: QuizQuestion,
    answers: List<QuizAnswer>,
    selectedIds: Set<String>,
    textAnswer: String,
    orderedIds: List<String>,
    matchedKeys: Map<String, String>,
    locked: Boolean,
    onSingleSelected: (String) -> Unit,
    onMultiSelected: (String, Boolean) -> Unit,
    onTextChanged: (String) -> Unit,
    onOrderChanged: (List<String>) -> Unit,
    onMatchChanged: (String, String) -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(question.question, style = MaterialTheme.typography.titleMedium)
            when (question.type) {
                QuestionType.MULTIPLE_SELECT -> answers.forEach { answer ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = answer.id in selectedIds,
                            onCheckedChange = if (locked) null else { checked -> onMultiSelected(answer.id, checked) }
                        )
                        Text(answer.text)
                    }
                }
                QuestionType.FILL_CODE -> OutlinedTextField(
                    value = textAnswer,
                    onValueChange = if (locked) ({}) else onTextChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("پاسخ") },
                    enabled = !locked,
                    minLines = 3
                )
                QuestionType.ORDER_STEPS -> orderedIds.forEachIndexed { index, answerId ->
                    question.answers.firstOrNull { it.id == answerId }?.let { answer ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${index + 1}. ${answer.text}")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    enabled = !locked && index > 0,
                                    onClick = {
                                        val next = orderedIds.toMutableList()
                                        val previous = next[index - 1]
                                        next[index - 1] = next[index]
                                        next[index] = previous
                                        onOrderChanged(next)
                                    }
                                ) { Text("بالا") }
                                OutlinedButton(
                                    enabled = !locked && index < orderedIds.lastIndex,
                                    onClick = {
                                        val next = orderedIds.toMutableList()
                                        val following = next[index + 1]
                                        next[index + 1] = next[index]
                                        next[index] = following
                                        onOrderChanged(next)
                                    }
                                ) { Text("پایین") }
                            }
                        }
                    }
                }
                QuestionType.MATCHING -> {
                    val keys = question.answers.mapNotNull(QuizAnswer::matchKey).distinct()
                    answers.forEach { answer ->
                        val currentKey = matchedKeys[answer.id]
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(answer.text)
                            OutlinedButton(
                                enabled = !locked && keys.isNotEmpty(),
                                onClick = {
                                    val currentIndex = keys.indexOf(currentKey)
                                    val nextIndex = if (currentIndex < 0 || currentIndex == keys.lastIndex) 0 else currentIndex + 1
                                    onMatchChanged(answer.id, keys[nextIndex])
                                }
                            ) { Text(currentKey ?: "انتخاب جفت") }
                        }
                    }
                }
                QuestionType.MULTIPLE_CHOICE,
                QuestionType.TRUE_FALSE,
                QuestionType.CODE_OUTPUT,
                QuestionType.FIND_ERROR -> answers.forEach { answer ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = answer.id in selectedIds,
                            onClick = if (locked) null else { { onSingleSelected(answer.id) } }
                        )
                        Text(answer.text)
                    }
                }
            }
            if (locked && question.explanation.isNotBlank()) {
                Text(question.explanation, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** MainUi-owned exercise presentation. Draft/completion persistence stays in the host/Core repositories. */
@Composable
fun AcademyExerciseRenderer(
    exercise: Exercise,
    modifier: Modifier = Modifier,
    initialAnswer: String = "",
    onDraftChanged: (String) -> Unit = {},
    onCompleted: (String) -> Unit = {}
) {
    var answer by remember(exercise.id, initialAnswer) {
        mutableStateOf(initialAnswer.ifBlank { exercise.starterCode.orEmpty() })
    }
    var visibleHints by remember(exercise.id) { mutableStateOf(0) }
    var showSolution by remember(exercise.id) { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(exercise.title, style = MaterialTheme.typography.headlineMedium)
        Text("سطح: ${exercise.difficulty.name}")
        Text(exercise.description, style = MaterialTheme.typography.bodyLarge)
        OutlinedTextField(
            value = answer,
            onValueChange = { answer = it; onDraftChanged(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("پاسخ / پیش‌نویس") },
            minLines = 8
        )
        exercise.expectedOutput?.takeIf(String::isNotBlank)?.let {
            Text("خروجی مورد انتظار:")
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }
        if (visibleHints < exercise.hints.size) {
            OutlinedButton(onClick = { visibleHints += 1 }) { Text("نمایش راهنمای بعدی") }
        }
        exercise.hints.take(visibleHints).forEachIndexed { index, hint -> Text("راهنما ${index + 1}: $hint") }
        exercise.solution?.takeIf(String::isNotBlank)?.let { solution ->
            OutlinedButton(onClick = { showSolution = !showSolution }) {
                Text(if (showSolution) "پنهان کردن پاسخ نمونه" else "نمایش پاسخ نمونه")
            }
            if (showSolution) {
                Text(solution, style = MaterialTheme.typography.bodyMedium)
                exercise.explanation?.takeIf(String::isNotBlank)?.let { Text(it) }
            }
        }
        Button(
            onClick = { onCompleted(answer) },
            enabled = answer.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("ثبت تمرین به‌عنوان انجام‌شده") }
    }
}

/** MainUi-owned project presentation. Project state continues to use Core models/repositories. */
@Composable
fun AcademyProjectRenderer(
    project: LearningProject,
    progress: ProjectProgress? = null,
    modifier: Modifier = Modifier,
    onProgressChanged: (ProjectProgress) -> Unit = {}
) {
    var completedIds by remember(project.id, progress?.updatedAtEpochMillis) {
        mutableStateOf(progress?.completedMilestoneIds.orEmpty())
    }
    var draft by remember(project.id, progress?.updatedAtEpochMillis) {
        mutableStateOf(progress?.draft.orEmpty())
    }

    fun publishProgress(nextCompletedIds: Set<String>, nextDraft: String) {
        val now = System.currentTimeMillis()
        val allCompleted = project.milestones.isNotEmpty() && project.milestones.all { it.id in nextCompletedIds }
        val completedAt = if (allCompleted) progress?.completedAtEpochMillis ?: now else null
        onProgressChanged(
            ProjectProgress(
                courseId = project.courseId,
                projectId = project.id,
                completedMilestoneIds = nextCompletedIds,
                draft = nextDraft,
                updatedAtEpochMillis = now,
                completedAtEpochMillis = completedAt
            )
        )
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(project.title, style = MaterialTheme.typography.headlineMedium)
        Text("سطح: ${project.difficulty} • زمان تقریبی: ${project.estimatedMinutes} دقیقه")
        Text(project.description, style = MaterialTheme.typography.bodyLarge)
        project.milestones.sortedBy(ProjectMilestone::order).forEach { milestone ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = milestone.id in completedIds,
                            onCheckedChange = { checked ->
                                val next = completedIds.toMutableSet()
                                if (checked) next += milestone.id else next -= milestone.id
                                completedIds = next
                                publishProgress(next, draft)
                            }
                        )
                        Text(milestone.title, style = MaterialTheme.typography.titleMedium)
                    }
                    Text(milestone.description)
                    milestone.acceptanceCriteria.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
                }
            }
        }
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it; publishProgress(completedIds, it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("یادداشت و خروجی پروژه") },
            minLines = 6
        )
        Text("پیشرفت مراحل: ${completedIds.size} از ${project.milestones.size}")
        if (project.milestones.isNotEmpty() && project.milestones.all { it.id in completedIds }) {
            Text("پروژه تکمیل شده است.", style = MaterialTheme.typography.titleMedium)
        }
    }
}
