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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asdevelopers.academy.core.database.AchievementEntity
import com.asdevelopers.academy.core.database.SearchIndexEntity
import kotlinx.coroutines.launch

/**
 * Course-scoped full-text search backed by Core's SearchRepository.
 * Search indexing remains a Core concern; MainUi only owns query interaction and result rendering.
 */
@Composable
fun AcademySearchScreen(
    courseId: String,
    runtime: AcademyMainUiRuntime,
    onOpenResult: (refId: String, refType: String) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var query by rememberSaveable(courseId) { mutableStateOf("") }
    var results by remember(courseId) { mutableStateOf(emptyList<SearchIndexEntity>()) }
    var hasSearched by rememberSaveable(courseId) { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Button(onClick = onBack) { Text("بازگشت") } }
        item { Text("جست‌وجو", style = MaterialTheme.typography.headlineMedium) }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("عبارت جست‌وجو") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Button(
                enabled = query.isNotBlank(),
                onClick = {
                    val rawQuery = query
                    scope.launch {
                        results = runtime.searchRepository.search(courseId, rawQuery)
                        hasSearched = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("جست‌وجو") }
        }

        if (hasSearched && results.isEmpty()) {
            item { Text("نتیجه‌ای پیدا نشد.") }
        } else {
            items(results, key = { "${it.refType}:${it.refId}" }) { result ->
                SearchResultCard(result = result, onOpen = onOpenResult)
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    result: SearchIndexEntity,
    onOpen: (refId: String, refType: String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(result.title, style = MaterialTheme.typography.titleMedium)
            if (result.body.isNotBlank()) Text(result.body.take(220))
            Text("نوع: ${result.refType}", style = MaterialTheme.typography.bodySmall)
            Button(onClick = { onOpen(result.refId, result.refType) }) { Text("باز کردن") }
        }
    }
}

/**
 * Displays achievements already unlocked by Core.
 * Unlock rules intentionally stay outside MainUi so presentation cannot mutate learning policy.
 */
@Composable
fun AcademyAchievementsScreen(
    courseId: String,
    runtime: AcademyMainUiRuntime,
    titleForAchievement: (String) -> String = { it },
    onBack: () -> Unit
) {
    val achievements by runtime.achievementRepository.observeCourse(courseId).collectAsState(initial = emptyList())
    val sorted = achievements.sortedByDescending(AchievementEntity::unlockedAt)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Button(onClick = onBack) { Text("بازگشت") } }
        item {
            Text("دستاوردها", style = MaterialTheme.typography.headlineMedium)
            Text("${sorted.size} دستاورد بازشده")
        }
        if (sorted.isEmpty()) {
            item { Text("هنوز دستاوردی باز نشده است.") }
        } else {
            items(sorted, key = { it.achievementId }) { achievement ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(titleForAchievement(achievement.achievementId), style = MaterialTheme.typography.titleMedium)
                        Text("شناسه: ${achievement.achievementId}")
                    }
                }
            }
        }
    }
}
