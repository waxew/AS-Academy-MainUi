package com.asdevelopers.academy.mainui

import android.content.res.AssetManager
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.asdevelopers.academy.core.content.LearningExtras
import com.asdevelopers.academy.core.content.LearningExtrasLoader
import com.asdevelopers.academy.core.progress.LessonProgress
import com.asdevelopers.academy.core.progress.LessonStatus
import com.asdevelopers.academy.core.progress.ProgressEngine
import com.asdevelopers.academy.core.search.SearchDocument
import com.asdevelopers.academy.core.ui.screens.AcademyExerciseScreen
import com.asdevelopers.academy.core.ui.screens.AcademyProjectScreen
import com.asdevelopers.academy.core.ui.screens.AcademyQuizScreen
import com.asdevelopers.academy.course.model.Chapter
import com.asdevelopers.academy.course.model.CourseLevel
import com.asdevelopers.academy.course.model.CourseLevelType
import com.asdevelopers.academy.course.model.Lesson
import com.asdevelopers.academy.course.model.LessonBlock
import com.asdevelopers.academy.course.model.LessonBlockType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private data class FolderCourseData(
    val levels: List<CourseLevel>,
    val chapters: List<Chapter>,
    val lessons: List<Lesson>,
    val extras: LearningExtras
)

private sealed interface CourseRoute {
    data object Home : CourseRoute
    data object Catalog : CourseRoute
    data object Search : CourseRoute
    data object Bookmarks : CourseRoute
    data object Achievements : CourseRoute
    data class Chapters(val levelId: String) : CourseRoute
    data class Lessons(val chapterId: String) : CourseRoute
    data class LessonDetail(val lessonId: String) : CourseRoute
    data class LessonNotes(val lessonId: String) : CourseRoute
    data class QuizDetail(val quizId: String) : CourseRoute
    data class ExerciseDetail(val exerciseId: String) : CourseRoute
    data class ProjectDetail(val projectId: String) : CourseRoute
}

private fun CourseRoute.encode(): String = when (this) {
    CourseRoute.Home -> "home"
    CourseRoute.Catalog -> "catalog"
    CourseRoute.Search -> "search"
    CourseRoute.Bookmarks -> "bookmarks"
    CourseRoute.Achievements -> "achievements"
    is CourseRoute.Chapters -> "chapters:$levelId"
    is CourseRoute.Lessons -> "lessons:$chapterId"
    is CourseRoute.LessonDetail -> "lesson:$lessonId"
    is CourseRoute.LessonNotes -> "notes:$lessonId"
    is CourseRoute.QuizDetail -> "quiz:$quizId"
    is CourseRoute.ExerciseDetail -> "exercise:$exerciseId"
    is CourseRoute.ProjectDetail -> "project:$projectId"
}

private fun decodeRoute(value: String): CourseRoute {
    val separator = value.indexOf(':')
    val key = if (separator >= 0) value.substring(0, separator) else value
    val id = if (separator >= 0) value.substring(separator + 1) else ""
    return when (key) {
        "catalog" -> CourseRoute.Catalog
        "search" -> CourseRoute.Search
        "bookmarks" -> CourseRoute.Bookmarks
        "achievements" -> CourseRoute.Achievements
        "chapters" -> CourseRoute.Chapters(id)
        "lessons" -> CourseRoute.Lessons(id)
        "lesson" -> CourseRoute.LessonDetail(id)
        "notes" -> CourseRoute.LessonNotes(id)
        "quiz" -> CourseRoute.QuizDetail(id)
        "exercise" -> CourseRoute.ExerciseDetail(id)
        "project" -> CourseRoute.ProjectDetail(id)
        else -> CourseRoute.Home
    }
}

/**
 * MainUi host for folder-based Course Packages copied from MainCourse into Android assets.
 * MainCourse remains the content source of truth; Core owns persisted user state and search.
 */
@Composable
fun AcademyFolderCourseHost(
    courseId: String,
    title: String,
    branding: com.asdevelopers.academy.course.model.CourseBranding,
    darkTheme: Boolean = false,
    runtime: AcademyMainUiRuntime? = null
) {
    val context = LocalContext.current
    val resolvedRuntime = runtime ?: remember(context) { AcademyMainUiRuntime.create(context) }
    val progress by resolvedRuntime.progressRepository.observeCourse(courseId).collectAsState(initial = emptyList())
    var data by remember(courseId) { mutableStateOf<FolderCourseData?>(null) }
    var error by rememberSaveable(courseId) { mutableStateOf<String?>(null) }
    var routeStack by rememberSaveable(courseId) { mutableStateOf(listOf(CourseRoute.Home.encode())) }

    fun navigate(route: CourseRoute) {
        val encoded = route.encode()
        if (routeStack.lastOrNull() != encoded) routeStack = routeStack + encoded
    }

    fun navigateBack() {
        if (routeStack.size > 1) routeStack = routeStack.dropLast(1)
    }

    BackHandler(enabled = routeStack.size > 1) { navigateBack() }

    LaunchedEffect(courseId) {
        data = null
        error = null
        routeStack = listOf(CourseRoute.Home.encode())
        val courseData = try {
            loadFolderCourse(context.assets, courseId)
        } catch (throwable: Throwable) {
            error = throwable.message ?: throwable.toString()
            return@LaunchedEffect
        }
        data = courseData
        runCatching {
            resolvedRuntime.searchRepository.replaceCourse(
                courseId = courseId,
                documents = courseData.searchDocuments(courseId)
            )
        }
    }

    AcademyMainUi(
        config = AcademyMainUiConfig(
            courseId = courseId,
            branding = branding,
            darkTheme = darkTheme
        )
    ) {
        when {
            error != null -> AcademyMainUiMessage("خطا در بارگذاری محتوای دوره: ${error.orEmpty()}")
            data == null -> AcademyMainUiLoading()
            else -> FolderCourseRouter(
                courseId = courseId,
                title = title,
                data = requireNotNull(data),
                progress = progress,
                runtime = resolvedRuntime,
                route = decodeRoute(routeStack.lastOrNull().orEmpty()),
                onNavigate = { navigate(it) },
                onBack = { navigateBack() }
            )
        }
    }
}

@Composable
private fun FolderCourseRouter(
    courseId: String,
    title: String,
    data: FolderCourseData,
    progress: List<LessonProgress>,
    runtime: AcademyMainUiRuntime,
    route: CourseRoute,
    onNavigate: (CourseRoute) -> Unit,
    onBack: () -> Unit
) {
    when (route) {
        CourseRoute.Home -> CourseHome(title, data, progress, onNavigate)
        CourseRoute.Catalog -> Catalog(data, onNavigate, onBack)
        CourseRoute.Search -> AcademySearchScreen(
            courseId = courseId,
            runtime = runtime,
            onOpenResult = { refId, refType ->
                if (refType.equals("lesson", ignoreCase = true)) {
                    onNavigate(CourseRoute.LessonDetail(refId))
                }
            },
            onBack = onBack
        )
        CourseRoute.Bookmarks -> AcademyBookmarksScreen(
            courseId = courseId,
            runtime = runtime,
            lessonTitle = { lessonId -> data.lessons.firstOrNull { it.id == lessonId }?.title },
            onOpenLesson = { onNavigate(CourseRoute.LessonDetail(it)) },
            onBack = onBack
        )
        CourseRoute.Achievements -> AcademyAchievementsScreen(
            courseId = courseId,
            runtime = runtime,
            onBack = onBack
        )
        is CourseRoute.Chapters -> ChapterList(data, route.levelId, onNavigate, onBack)
        is CourseRoute.Lessons -> LessonList(data, progress, route.chapterId, onNavigate, onBack)
        is CourseRoute.LessonDetail -> {
            val lesson = data.lessons.firstOrNull { it.id == route.lessonId }
            if (lesson == null) AcademyMainUiMessage("درس پیدا نشد")
            else LessonReader(courseId, lesson, runtime, onNavigate, onBack)
        }
        is CourseRoute.LessonNotes -> {
            val lesson = data.lessons.firstOrNull { it.id == route.lessonId }
            if (lesson == null) AcademyMainUiMessage("درس پیدا نشد")
            else AcademyLessonNotesScreen(
                courseId = courseId,
                lessonId = lesson.id,
                lessonTitle = lesson.title,
                runtime = runtime,
                onBack = onBack
            )
        }
        is CourseRoute.QuizDetail -> {
            val quiz = data.extras.quizzes.firstOrNull { it.id == route.quizId }
            if (quiz == null) AcademyMainUiMessage("آزمون پیدا نشد")
            else DetailContainer("بازگشت", onBack) { AcademyQuizScreen(quiz = quiz) }
        }
        is CourseRoute.ExerciseDetail -> {
            val exercise = data.extras.exercises.firstOrNull { it.id == route.exerciseId }
            if (exercise == null) AcademyMainUiMessage("تمرین پیدا نشد")
            else DetailContainer("بازگشت", onBack) { AcademyExerciseScreen(exercise = exercise) }
        }
        is CourseRoute.ProjectDetail -> {
            val project = data.extras.projects.firstOrNull { it.id == route.projectId }
            if (project == null) AcademyMainUiMessage("پروژه پیدا نشد")
            else DetailContainer("بازگشت", onBack) { AcademyProjectScreen(project = project) }
        }
    }
}

@Composable
private fun CourseHome(
    title: String,
    data: FolderCourseData,
    progress: List<LessonProgress>,
    onNavigate: (CourseRoute) -> Unit
) {
    val lessonIds = data.lessons.mapTo(mutableSetOf()) { it.id }
    val courseProgress = progress.filter { it.lessonId in lessonIds }
    val completedCount = courseProgress.count { it.status == LessonStatus.COMPLETED }
    val averagePercent = if (data.lessons.isEmpty()) 0 else {
        courseProgress.sumOf { it.progressPercent }.div(data.lessons.size).coerceIn(0, 100)
    }
    val continueLesson = courseProgress
        .filter { it.status != LessonStatus.COMPLETED }
        .maxByOrNull { it.lastOpenedAtEpochMillis ?: Long.MIN_VALUE }
        ?.let { item -> data.lessons.firstOrNull { it.id == item.lessonId } }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Text("${data.levels.size} سطح • ${data.chapters.size} فصل • ${data.lessons.size} درس")
            Text("پیشرفت دوره: $averagePercent٪ • $completedCount درس تکمیل‌شده")
        }
        continueLesson?.let { lesson ->
            item {
                Button(
                    onClick = { onNavigate(CourseRoute.LessonDetail(lesson.id)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ادامه یادگیری: ${lesson.title}")
                }
            }
        }
        item {
            Button(onClick = { onNavigate(CourseRoute.Search) }, modifier = Modifier.fillMaxWidth()) {
                Text("جست‌وجو در دوره")
            }
        }
        item {
            Button(onClick = { onNavigate(CourseRoute.Bookmarks) }, modifier = Modifier.fillMaxWidth()) {
                Text("نشان‌شده‌ها")
            }
        }
        item {
            Button(onClick = { onNavigate(CourseRoute.Achievements) }, modifier = Modifier.fillMaxWidth()) {
                Text("دستاوردها")
            }
        }
        item {
            Button(onClick = { onNavigate(CourseRoute.Catalog) }, modifier = Modifier.fillMaxWidth()) {
                Text("تمرین‌ها، آزمون‌ها و پروژه‌ها")
            }
        }
        items(data.levels.sortedBy { it.order }, key = { it.id }) { level ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(level.title, style = MaterialTheme.typography.titleLarge)
                    if (level.description.isNotBlank()) Text(level.description)
                    Text("${data.chapters.count { it.levelId == level.id }} فصل")
                    Button(onClick = { onNavigate(CourseRoute.Chapters(level.id)) }) { Text("مشاهده فصل‌ها") }
                }
            }
        }
    }
}

@Composable
private fun ChapterList(
    data: FolderCourseData,
    levelId: String,
    onNavigate: (CourseRoute) -> Unit,
    onBack: () -> Unit
) {
    val chapters = data.chapters.filter { it.levelId == levelId }.sortedBy { it.order }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { BackButton(onBack) }
        items(chapters, key = { it.id }) { chapter ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(chapter.title, style = MaterialTheme.typography.titleLarge)
                    Text(chapter.description)
                    Text("${data.lessons.count { it.chapterId == chapter.id }} درس")
                    Button(onClick = { onNavigate(CourseRoute.Lessons(chapter.id)) }) { Text("ورود به فصل") }
                }
            }
        }
    }
}

@Composable
private fun LessonList(
    data: FolderCourseData,
    progress: List<LessonProgress>,
    chapterId: String,
    onNavigate: (CourseRoute) -> Unit,
    onBack: () -> Unit
) {
    val lessons = data.lessons.filter { it.chapterId == chapterId }.sortedBy { it.order }
    val progressByLesson = progress.associateBy { it.lessonId }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { BackButton(onBack) }
        items(lessons, key = { it.id }) { lesson ->
            val itemProgress = progressByLesson[lesson.id]
            Button(onClick = { onNavigate(CourseRoute.LessonDetail(lesson.id)) }, modifier = Modifier.fillMaxWidth()) {
                val suffix = when (itemProgress?.status) {
                    LessonStatus.COMPLETED -> " • تکمیل‌شده"
                    LessonStatus.IN_PROGRESS, LessonStatus.NEEDS_REVIEW -> " • ${itemProgress.progressPercent}٪"
                    else -> ""
                }
                Text("${lesson.title} • ${lesson.estimatedMinutes} دقیقه$suffix")
            }
        }
    }
}

@Composable
private fun LessonReader(
    courseId: String,
    lesson: Lesson,
    runtime: AcademyMainUiRuntime,
    onNavigate: (CourseRoute) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val progress by runtime.progressRepository.observeLesson(courseId, lesson.id).collectAsState(initial = null)

    LaunchedEffect(courseId, lesson.id) {
        if (lesson.blocks.isEmpty()) return@LaunchedEffect
        val now = System.currentTimeMillis()
        val current = runtime.progressRepository.observeLesson(courseId, lesson.id).first()
            ?: LessonProgress(courseId = courseId, lessonId = lesson.id)
        val updated = if (current.status == LessonStatus.NOT_STARTED) {
            ProgressEngine.updateFromBlock(
                current = current,
                viewedBlockIndex = 0,
                totalBlocks = lesson.blocks.size,
                additionalStudySeconds = 0,
                openedAtEpochMillis = now
            )
        } else {
            current.copy(lastOpenedAtEpochMillis = now)
        }
        runtime.progressRepository.save(updated)
    }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { BackButton(onBack) }
        item {
            Text(lesson.title, style = MaterialTheme.typography.headlineMedium)
            if (lesson.summary.isNotBlank()) Text(lesson.summary)
            Text("زمان تقریبی: ${lesson.estimatedMinutes} دقیقه")
            progress?.let { Text("پیشرفت این درس: ${it.progressPercent}٪") }
        }
        item {
            LessonUserStatePanel(
                courseId = courseId,
                lessonId = lesson.id,
                runtime = runtime
            )
        }
        item {
            Button(
                onClick = { onNavigate(CourseRoute.LessonNotes(lesson.id)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("همه یادداشت‌های این درس")
            }
        }
        items(lesson.blocks, key = { it.id }) { block ->
            LessonBlockView(block = block, onNavigate = onNavigate)
        }
        item {
            Button(
                enabled = lesson.blocks.isNotEmpty() && progress?.status != LessonStatus.COMPLETED,
                onClick = {
                    scope.launch {
                        val current = runtime.progressRepository.observeLesson(courseId, lesson.id).first()
                            ?: LessonProgress(courseId = courseId, lessonId = lesson.id)
                        val completed = ProgressEngine.updateFromBlock(
                            current = current,
                            viewedBlockIndex = lesson.blocks.lastIndex,
                            totalBlocks = lesson.blocks.size,
                            additionalStudySeconds = 0,
                            openedAtEpochMillis = System.currentTimeMillis()
                        )
                        runtime.progressRepository.save(completed)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (progress?.status == LessonStatus.COMPLETED) "درس تکمیل شده" else "تکمیل درس")
            }
        }
    }
}

@Composable
private fun LessonBlockView(block: LessonBlock, onNavigate: (CourseRoute) -> Unit) {
    val style = when (block.type) {
        LessonBlockType.TITLE -> MaterialTheme.typography.headlineSmall
        LessonBlockType.SUBTITLE -> MaterialTheme.typography.titleLarge
        else -> MaterialTheme.typography.bodyLarge
    }
    when (block.type) {
        LessonBlockType.CODE, LessonBlockType.OUTPUT, LessonBlockType.TIP, LessonBlockType.WARNING,
        LessonBlockType.NOTE, LessonBlockType.IMPORTANT, LessonBlockType.DIAGRAM, LessonBlockType.TABLE -> {
            Card(Modifier.fillMaxWidth()) {
                Text(block.content, modifier = Modifier.padding(14.dp), style = style)
            }
        }
        LessonBlockType.EXERCISE, LessonBlockType.EXERCISE_LINK -> ReferenceButton(
            label = block.content.ifBlank { "باز کردن تمرین" },
            targetId = block.metadata["exerciseId"],
            onOpen = { onNavigate(CourseRoute.ExerciseDetail(it)) }
        )
        LessonBlockType.QUIZ -> ReferenceButton(
            label = block.content.ifBlank { "باز کردن آزمون" },
            targetId = block.metadata["quizId"],
            onOpen = { onNavigate(CourseRoute.QuizDetail(it)) }
        )
        LessonBlockType.PROJECT_LINK, LessonBlockType.PROJECT -> ReferenceButton(
            label = block.content.ifBlank { "باز کردن پروژه" },
            targetId = block.metadata["projectId"],
            onOpen = { onNavigate(CourseRoute.ProjectDetail(it)) }
        )
        else -> Text(block.content, style = style)
    }
}

@Composable
private fun ReferenceButton(label: String, targetId: String?, onOpen: (String) -> Unit) {
    val validTargetId = targetId?.trim().orEmpty()
    Button(
        onClick = { onOpen(validTargetId) },
        enabled = validTargetId.isNotBlank(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (validTargetId.isBlank()) "$label — ارجاع نامعتبر" else label)
    }
}

@Composable
private fun Catalog(data: FolderCourseData, onNavigate: (CourseRoute) -> Unit, onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { BackButton(onBack) }
        item { Text("آزمون‌ها", style = MaterialTheme.typography.headlineSmall) }
        items(data.extras.quizzes, key = { it.id }) { quiz ->
            Button(onClick = { onNavigate(CourseRoute.QuizDetail(quiz.id)) }, modifier = Modifier.fillMaxWidth()) { Text(quiz.title) }
        }
        item { Text("تمرین‌ها", style = MaterialTheme.typography.headlineSmall) }
        items(data.extras.exercises, key = { it.id }) { exercise ->
            Button(onClick = { onNavigate(CourseRoute.ExerciseDetail(exercise.id)) }, modifier = Modifier.fillMaxWidth()) { Text(exercise.title) }
        }
        item { Text("پروژه‌ها", style = MaterialTheme.typography.headlineSmall) }
        items(data.extras.projects, key = { it.id }) { project ->
            Button(onClick = { onNavigate(CourseRoute.ProjectDetail(project.id)) }, modifier = Modifier.fillMaxWidth()) { Text(project.title) }
        }
    }
}

@Composable
private fun DetailContainer(label: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Button(onClick = onBack, modifier = Modifier.padding(12.dp)) { Text(label) }
        Column(Modifier.weight(1f)) { content() }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    Button(onClick = onBack) { Text("بازگشت") }
}

private fun FolderCourseData.searchDocuments(courseId: String): List<SearchDocument> =
    lessons.map { lesson ->
        SearchDocument(
            courseId = courseId,
            refId = lesson.id,
            refType = "lesson",
            title = lesson.title,
            body = buildString {
                appendLine(lesson.summary)
                lesson.blocks.forEach { appendLine(it.content) }
                append(lesson.tags.joinToString(" "))
            }
        )
    }

private suspend fun loadFolderCourse(assets: AssetManager, courseId: String): FolderCourseData = withContext(Dispatchers.IO) {
    val root = "course/$courseId"
    val levels = readObjects(assets, "$root/levels.json").map { json ->
        CourseLevel(
            id = json.getString("id"),
            courseId = json.optString("courseId", courseId).ifBlank { courseId },
            type = enumValue(json.optString("type", inferLevelType(json.getString("id"))), CourseLevelType.BEGINNER),
            title = json.getString("title"),
            order = json.optInt("order", 0),
            description = json.optString("description")
        )
    }
    val chapters = readObjects(assets, "$root/chapters.json").map { json ->
        Chapter(
            id = json.getString("id"),
            levelId = json.getString("levelId"),
            title = json.getString("title"),
            description = json.optString("description"),
            order = json.optInt("order", 0),
            prerequisites = json.optJSONArray("prerequisites").toStrings()
        )
    }
    val lessons = assets.list("$root/lessons").orEmpty()
        .filter { it.endsWith(".json") }
        .sorted()
        .flatMap { readObjects(assets, "$root/lessons/$it") }
        .map(::parseLesson)
    val extras = LearningExtrasLoader(assets).load(courseId)
    val courseData = FolderCourseData(levels, chapters, lessons, extras)
    val report = CoursePackageValidator.validate(courseData.validationInput())
    require(report.isValid) {
        "Course package '$courseId' is invalid:\n${report.errors.joinToString(separator = "\n") { "- $it" }}"
    }
    courseData
}

private fun FolderCourseData.validationInput(): CoursePackageValidationInput {
    val references = lessons.flatMap { lesson ->
        lesson.blocks.mapNotNull { block ->
            when (block.type) {
                LessonBlockType.EXERCISE, LessonBlockType.EXERCISE_LINK -> BlockReference(
                    blockId = block.id,
                    type = BlockReferenceType.EXERCISE,
                    targetId = block.metadata["exerciseId"]
                )
                LessonBlockType.QUIZ -> BlockReference(
                    blockId = block.id,
                    type = BlockReferenceType.QUIZ,
                    targetId = block.metadata["quizId"]
                )
                LessonBlockType.PROJECT, LessonBlockType.PROJECT_LINK -> BlockReference(
                    blockId = block.id,
                    type = BlockReferenceType.PROJECT,
                    targetId = block.metadata["projectId"]
                )
                else -> null
            }
        }
    }
    return CoursePackageValidationInput(
        levelIds = levels.map { it.id },
        chapters = chapters.map { ChapterReference(it.id, it.levelId) },
        lessons = lessons.map { LessonReference(it.id, it.chapterId) },
        blockReferences = references,
        quizIds = extras.quizzes.map { it.id },
        exerciseIds = extras.exercises.map { it.id },
        projectIds = extras.projects.map { it.id }
    )
}

private fun parseLesson(json: JSONObject): Lesson = Lesson(
    id = json.getString("id"),
    chapterId = json.getString("chapterId"),
    title = json.getString("title"),
    summary = json.optString("summary"),
    order = json.optInt("order", 0),
    estimatedMinutes = json.optInt("estimatedMinutes", 1).coerceAtLeast(1),
    blocks = json.optJSONArray("blocks").toObjects().map { block ->
        LessonBlock(
            id = block.getString("id"),
            type = enumValue(block.getString("type"), LessonBlockType.PARAGRAPH),
            content = block.optString("content"),
            metadata = block.optJSONObject("metadata").toStringMap(),
            accessibilityLabel = block.optString("accessibilityLabel").takeIf { it.isNotBlank() }
        )
    },
    tags = json.optJSONArray("tags").toStrings().toSet(),
    prerequisites = json.optJSONArray("prerequisites").toStrings()
)

private fun readObjects(assets: AssetManager, path: String): List<JSONObject> {
    val text = assets.open(path).bufferedReader().use { it.readText() }.trim()
    return when {
        text.startsWith("[") -> JSONArray(text).toObjects()
        text.startsWith("{") -> listOf(JSONObject(text))
        else -> emptyList()
    }
}

private fun JSONArray?.toObjects(): List<JSONObject> =
    if (this == null) emptyList() else (0 until length()).map { getJSONObject(it) }

private fun JSONArray?.toStrings(): List<String> =
    if (this == null) emptyList() else (0 until length()).map { optString(it) }

private fun JSONObject?.toStringMap(): Map<String, String> {
    if (this == null) return emptyMap()
    return keys().asSequence().associateWith { key -> optString(key) }
}

private inline fun <reified T : Enum<T>> enumValue(raw: String, fallback: T): T =
    runCatching { enumValueOf<T>(raw.trim().uppercase()) }.getOrDefault(fallback)

private fun inferLevelType(id: String): String = when {
    "fund" in id -> "FUNDAMENTALS"
    "beg" in id -> "BEGINNER"
    "adv" in id -> "ADVANCED"
    "pro" in id || "spec" in id -> "SPECIALIST"
    else -> "BEGINNER"
}
