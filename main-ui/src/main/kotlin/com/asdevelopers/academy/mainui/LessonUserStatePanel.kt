package com.asdevelopers.academy.mainui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** Lesson-scoped user state backed exclusively by Core repositories. */
@Composable
internal fun LessonUserStatePanel(
    courseId: String,
    lessonId: String,
    runtime: AcademyMainUiRuntime
) {
    val scope = rememberCoroutineScope()
    val bookmarks by runtime.bookmarkRepository.observeCourse(courseId).collectAsState(initial = emptyList())
    val notes by runtime.userNoteRepository.observeLesson(courseId, lessonId).collectAsState(initial = emptyList())

    val lessonBookmark = bookmarks.firstOrNull {
        it.targetType == LESSON_TARGET_TYPE && it.targetId == lessonId
    }
    val lessonNote = notes
        .filter { it.blockId == null }
        .maxByOrNull { it.updatedAt }
    var noteText by remember(lessonId) { mutableStateOf("") }

    LaunchedEffect(lessonNote?.id) {
        noteText = lessonNote?.text.orEmpty()
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = {
                scope.launch {
                    if (lessonBookmark == null) {
                        runtime.bookmarkRepository.add(
                            courseId = courseId,
                            targetType = LESSON_TARGET_TYPE,
                            targetId = lessonId,
                            lessonId = lessonId,
                            createdAt = System.currentTimeMillis()
                        )
                    } else {
                        runtime.bookmarkRepository.remove(lessonBookmark.id)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (lessonBookmark == null) "افزودن به نشان‌شده‌ها" else "حذف از نشان‌شده‌ها")
        }

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("یادداشت این درس") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Button(
            enabled = noteText.isNotBlank(),
            onClick = {
                scope.launch {
                    runtime.userNoteRepository.save(
                        id = lessonNote?.id,
                        courseId = courseId,
                        lessonId = lessonId,
                        blockId = null,
                        text = noteText,
                        updatedAt = System.currentTimeMillis()
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (lessonNote == null) "ذخیره یادداشت" else "به‌روزرسانی یادداشت")
        }

        if (lessonNote != null) {
            Button(
                onClick = {
                    scope.launch {
                        runtime.userNoteRepository.remove(lessonNote.id)
                        noteText = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("حذف یادداشت")
            }
        }
    }
}

private const val LESSON_TARGET_TYPE = "lesson"
