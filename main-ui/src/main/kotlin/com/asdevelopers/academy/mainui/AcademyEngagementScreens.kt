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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asdevelopers.academy.core.database.BookmarkEntity
import com.asdevelopers.academy.core.database.UserNoteEntity
import kotlinx.coroutines.launch

/**
 * Course-scoped bookmark list backed by Core's BookmarkRepository.
 * MainUi owns presentation while Core remains the persistence source of truth.
 */
@Composable
fun AcademyBookmarksScreen(
    courseId: String,
    runtime: AcademyMainUiRuntime,
    lessonTitle: (String) -> String?,
    onOpenLesson: (String) -> Unit,
    onBack: () -> Unit
) {
    val bookmarks by runtime.bookmarkRepository.observeCourse(courseId).collectAsState(initial = emptyList())
    val lessonBookmarks = bookmarks
        .filter { it.targetType == BOOKMARK_TYPE_LESSON }
        .sortedByDescending(BookmarkEntity::createdAt)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Button(onClick = onBack) { Text("بازگشت") }
        }
        item {
            Text("نشان‌شده‌ها", style = MaterialTheme.typography.headlineMedium)
            Text("${lessonBookmarks.size} درس نشان‌شده")
        }
        if (lessonBookmarks.isEmpty()) {
            item { Text("هنوز درسی نشان نشده است.") }
        } else {
            items(lessonBookmarks, key = { it.id }) { bookmark ->
                val title = lessonTitle(bookmark.targetId) ?: bookmark.targetId
                Button(
                    onClick = { onOpenLesson(bookmark.targetId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(title)
                }
            }
        }
    }
}

/**
 * Lesson-scoped notes editor backed by Core's UserNoteRepository.
 * Empty notes never reach the repository and all mutations remain course-aware.
 */
@Composable
fun AcademyLessonNotesScreen(
    courseId: String,
    lessonId: String,
    lessonTitle: String,
    runtime: AcademyMainUiRuntime,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val notes by runtime.userNoteRepository.observeLesson(courseId, lessonId).collectAsState(initial = emptyList())
    var draft by rememberSaveable(courseId, lessonId) { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Button(onClick = onBack) { Text("بازگشت") }
        }
        item {
            Text("یادداشت‌های درس", style = MaterialTheme.typography.headlineMedium)
            Text(lessonTitle)
        }
        item {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = { Text("یادداشت جدید") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
        item {
            Button(
                enabled = draft.isNotBlank(),
                onClick = {
                    val text = draft.trim()
                    if (text.isBlank()) return@Button
                    scope.launch {
                        runtime.userNoteRepository.save(
                            id = null,
                            courseId = courseId,
                            lessonId = lessonId,
                            blockId = null,
                            text = text,
                            updatedAt = System.currentTimeMillis()
                        )
                        draft = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ذخیره یادداشت")
            }
        }
        if (notes.isEmpty()) {
            item { Text("برای این درس هنوز یادداشتی ثبت نشده است.") }
        } else {
            items(notes.sortedByDescending(UserNoteEntity::updatedAt), key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    onDelete = {
                        scope.launch { runtime.userNoteRepository.remove(note.id) }
                    }
                )
            }
        }
    }
}

/**
 * Reusable lesson bookmark action. It intentionally knows nothing about navigation.
 */
@Composable
fun AcademyLessonBookmarkButton(
    courseId: String,
    lessonId: String,
    runtime: AcademyMainUiRuntime,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val bookmarks by runtime.bookmarkRepository.observeCourse(courseId).collectAsState(initial = emptyList())
    val current = bookmarks.firstOrNull {
        it.targetType == BOOKMARK_TYPE_LESSON && it.targetId == lessonId
    }

    Button(
        onClick = {
            scope.launch {
                if (current == null) {
                    runtime.bookmarkRepository.add(
                        courseId = courseId,
                        targetType = BOOKMARK_TYPE_LESSON,
                        targetId = lessonId,
                        lessonId = lessonId,
                        createdAt = System.currentTimeMillis()
                    )
                } else {
                    runtime.bookmarkRepository.remove(current.id)
                }
            }
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Text(if (current == null) "نشان کردن درس" else "حذف از نشان‌شده‌ها")
    }
}

@Composable
private fun NoteCard(note: UserNoteEntity, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(note.text)
            Button(onClick = onDelete) { Text("حذف") }
        }
    }
}

private const val BOOKMARK_TYPE_LESSON = "lesson"
