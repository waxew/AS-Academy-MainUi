package com.asdevelopers.academy.mainui

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
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
import com.asdevelopers.academy.core.content.AssetCoursePackageSource
import com.asdevelopers.academy.core.content.CourseBundle
import com.asdevelopers.academy.core.content.CourseLoadResult
import com.asdevelopers.academy.core.content.CoursePackageLoader
import com.asdevelopers.academy.core.exercise.ExerciseDraft
import com.asdevelopers.academy.core.progress.LearningCompletion
import com.asdevelopers.academy.core.progress.LearningPathEngine
import com.asdevelopers.academy.core.progress.LearningTargetType
import com.asdevelopers.academy.core.review.Flashcard
import com.asdevelopers.academy.core.review.PlacementEngine
import com.asdevelopers.academy.core.settings.AcademyProfile
import com.asdevelopers.academy.core.settings.AcademySettings
import kotlinx.coroutines.launch

/**
 * Generic host for a compiled AS Academy Course Package.
 *
 * Course applications own identity/configuration only. Core owns runtime and persistence,
 * MainUi owns navigation/presentation/workflow wiring, and MainCourse owns curriculum content.
 */
@Composable
fun AcademyCompiledCourseHost(
    courseId: String,
    assetPath: String,
    appTitle: String,
    appInfo: AcademyAppInfo,
    databaseName: String = "as_academy.db",
    placementQuizId: String? = null,
    modifier: Modifier = Modifier
) {
    require(courseId.isNotBlank()) { "courseId must not be blank" }
    require(assetPath.isNotBlank()) { "assetPath must not be blank" }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val runtime = remember(context, databaseName) { AcademyMainUiRuntime.create(context, databaseName) }
    val settings by runtime.preferencesRepository.settings.collectAsState(initial = AcademySettings())
    val profile by runtime.preferencesRepository.profile.collectAsState(initial = AcademyProfile())

    var courseResult by remember(assetPath) { mutableStateOf<CourseLoadResult?>(null) }
    var routeStack by rememberSaveable(courseId) { mutableStateOf(listOf(CompiledRoute.Home.encode())) }

    LaunchedEffect(courseId, assetPath) {
        runtime.preferencesRepository.setLastCourse(courseId)
        courseResult = CoursePackageLoader().load(AssetCoursePackageSource(context, assetPath))
    }

    val bundle = (courseResult as? CourseLoadResult.Success)?.bundle
    val config = AcademyMainUiConfig(
        courseId = courseId,
        branding = bundle?.branding ?: DefaultMainUiBranding,
        darkTheme = false,
        typedCapabilities = setOf(
            AcademyCapability.SEARCH,
            AcademyCapability.PROGRESS,
            AcademyCapability.SETTINGS,
            AcademyCapability.PROFILE,
            AcademyCapability.SHARE,
            AcademyCapability.ABOUT,
            AcademyCapability.UPDATE
        ),
        appInfo = appInfo
    )

    fun navigate(route: CompiledRoute) {
        val encoded = route.encode()
        if (routeStack.lastOrNull() != encoded) routeStack = routeStack + encoded
    }

    fun navigateBack() {
        if (routeStack.size > 1) routeStack = routeStack.dropLast(1)
    }

    BackHandler(enabled = routeStack.size > 1) { navigateBack() }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            scope.launch { runtime.preferencesRepository.updateProfile(profile.displayName, uri.toString()) }
        }
    }

    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        scope.launch { runtime.preferencesRepository.setNotificationsEnabled(granted) }
        if (granted) {
            runtime.studyReminderScheduler.scheduleEvery(
                days = 1,
                title = appTitle,
                message = "زمان ادامه مسیر یادگیری شما در $appTitle است."
            )
        }
    }

    AcademyPreferenceAwareMainUi(config = config, runtime = runtime) {
        val drawerItems = buildList {
            add(AcademyMainUiDrawerItem("compiled-home", "خانه", Icons.Outlined.Home, routeStack.lastOrNull() == CompiledRoute.Home.encode()) {
                routeStack = listOf(CompiledRoute.Home.encode())
            })
            add(AcademyMainUiDrawerItem("compiled-catalog", "تمرین، آزمون و پروژه", Icons.Outlined.MenuBook, false) {
                navigate(CompiledRoute.Catalog)
            })
            if (placementQuizId != null) {
                add(AcademyMainUiDrawerItem("compiled-placement", "تعیین سطح", Icons.Outlined.MenuBook, false) {
                    navigate(CompiledRoute.Quiz(placementQuizId))
                })
                add(AcademyMainUiDrawerItem("compiled-weak-review", "مرور نقاط ضعف", Icons.Outlined.Refresh, false) {
                    navigate(CompiledRoute.WeakReview)
                })
                add(AcademyMainUiDrawerItem("compiled-flashcards", "مرور فلش‌کارت", Icons.Outlined.Refresh, false) {
                    navigate(CompiledRoute.Flashcards)
                })
            }
            add(AcademyMainUiDrawerItem("compiled-settings", "تنظیمات", Icons.Outlined.Settings, false) {
                navigate(CompiledRoute.Settings)
            })
            add(AcademyMainUiDrawerItem("compiled-about", "درباره برنامه", Icons.Outlined.Info, false) {
                navigate(CompiledRoute.About)
            })
        }

        AcademyMainUiShell(
            title = bundle?.manifest?.titleFa ?: appTitle,
            profile = profile,
            courseItems = drawerItems,
            onProfileImageClick = { imagePicker.launch(arrayOf("image/*")) },
            onSettingsClick = { navigate(CompiledRoute.Settings) },
            onShareClick = {
                val text = appInfo.shareText ?: "$appTitle | AS Academy"
                context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        },
                        "اشتراک‌گذاری"
                    )
                )
            },
            onAboutClick = { navigate(CompiledRoute.About) },
            contentIsRtl = bundle?.manifest?.rtl ?: true
        ) { padding ->
            when {
                courseResult == null -> AcademyMainUiLoading(
                    modifier = modifier.fillMaxSize().padding(padding)
                )
                bundle == null -> AcademyMainUiMessage(
                    message = "محتوای دوره معتبر نیست یا بارگذاری آن ناموفق بود.",
                    modifier = modifier.fillMaxSize().padding(padding)
                )
                else -> CompiledCourseRouter(
                    courseId = courseId,
                    bundle = bundle,
                    placementQuizId = placementQuizId,
                    route = CompiledRoute.decode(routeStack.lastOrNull().orEmpty()),
                    runtime = runtime,
                    settings = settings,
                    appTitle = appTitle,
                    appInfo = appInfo,
                    modifier = modifier.fillMaxSize().padding(padding),
                    onNavigate = ::navigate,
                    onBack = ::navigateBack,
                    onNotificationSettingChanged = { enabled ->
                        scope.launch { runtime.preferencesRepository.setNotificationsEnabled(enabled) }
                        if (!enabled) {
                            runtime.studyReminderScheduler.cancel()
                        } else if (
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !runtime.studyReminderScheduler.canPostNotifications()
                        ) {
                            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            runtime.studyReminderScheduler.scheduleEvery(
                                days = 1,
                                title = appTitle,
                                message = "زمان ادامه مسیر یادگیری شما در $appTitle است."
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CompiledCourseRouter(
    courseId: String,
    bundle: CourseBundle,
    placementQuizId: String?,
    route: CompiledRoute,
    runtime: AcademyMainUiRuntime,
    settings: AcademySettings,
    appTitle: String,
    appInfo: AcademyAppInfo,
    modifier: Modifier,
    onNavigate: (CompiledRoute) -> Unit,
    onBack: () -> Unit,
    onNotificationSettingChanged: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    when (route) {
        CompiledRoute.Home -> AcademyCourseHomeScreen(
            bundle = bundle,
            onOpenLesson = { onNavigate(CompiledRoute.Lesson(it)) },
            onOpenPlacement = placementQuizId?.let { id -> { onNavigate(CompiledRoute.Quiz(id)) } },
            onOpenWeakReview = placementQuizId?.let { { onNavigate(CompiledRoute.WeakReview) } },
            onOpenFlashcards = placementQuizId?.let { { onNavigate(CompiledRoute.Flashcards) } },
            onOpenLearningCatalog = { onNavigate(CompiledRoute.Catalog) },
            modifier = modifier
        )
        CompiledRoute.Catalog -> AcademyCourseLearningCatalog(
            bundle = bundle,
            onQuizClick = { onNavigate(CompiledRoute.Quiz(it)) },
            onExerciseClick = { onNavigate(CompiledRoute.Exercise(it)) },
            onProjectClick = { onNavigate(CompiledRoute.Project(it)) },
            modifier = modifier
        )
        CompiledRoute.Settings -> AcademyMainUiSettingsScreen(
            settings = settings,
            onThemeChanged = { mode -> scope.launch { runtime.preferencesRepository.setThemeMode(mode) } },
            onNotificationsChanged = onNotificationSettingChanged,
            onFontScaleChanged = { scale -> scope.launch { runtime.preferencesRepository.setFontScale(scale) } },
            modifier = modifier
        )
        CompiledRoute.About -> AcademyMainUiAboutScreen(
            appTitle = appTitle,
            description = appInfo.description,
            versionName = appInfo.versionName,
            supportEmail = appInfo.supportEmail,
            modifier = modifier
        )
        CompiledRoute.Placement -> {
            val quizId = placementQuizId
            if (quizId == null) {
                AcademyMainUiMessage("آزمون تعیین سطح برای این دوره تعریف نشده است.", modifier)
            } else {
                val state by remember(courseId, quizId) {
                    runtime.placementResultRepository.observeLatest(
                        courseId = courseId,
                        placementQuizId = quizId,
                        policy = PlacementEngine.fourLevelPolicy()
                    )
                }.collectAsState(initial = null)
                val placement = state
                if (placement == null) {
                    AcademyMainUiMessage("ابتدا آزمون تعیین سطح را کامل کنید.", modifier)
                } else {
                    AcademyMainUiPlacementSummaryScreen(
                        recommendation = placement.recommendation,
                        weakTags = placement.weakTags,
                        onStartLevel = { levelType ->
                            LearningPathEngine.firstLessonIdForLevelType(bundle, levelType)?.let {
                                onNavigate(CompiledRoute.Lesson(it))
                            }
                        },
                        onReviewWeakTopics = { onNavigate(CompiledRoute.WeakReview) },
                        modifier = modifier
                    )
                }
            }
        }
        CompiledRoute.WeakReview -> {
            val recommendations by remember(bundle.manifest.version) {
                runtime.weakTopicReviewRepository.observeRecommendations(bundle)
            }.collectAsState(initial = emptyList())
            AcademyMainUiWeakTopicReviewScreen(
                recommendations = recommendations,
                onLessonClick = { onNavigate(CompiledRoute.Lesson(it)) },
                modifier = modifier
            )
        }
        CompiledRoute.Flashcards -> {
            var cards by remember(bundle.manifest.version) { mutableStateOf<List<Flashcard>>(emptyList()) }
            val epochDay = System.currentTimeMillis() / MILLIS_PER_DAY
            LaunchedEffect(bundle.manifest.version) {
                cards = runtime.flashcardReviewRepository.loadDueCards(bundle, epochDay)
            }
            AcademyMainUiFlashcardReviewScreen(
                cards = cards,
                onRated = { card, rating ->
                    scope.launch {
                        runtime.flashcardReviewRepository.recordReview(
                            courseId = courseId,
                            cardId = card.id,
                            rating = rating,
                            reviewedEpochDay = epochDay,
                            updatedAtEpochMillis = System.currentTimeMillis()
                        )
                        cards = cards.filterNot { it.id == card.id }
                    }
                },
                onSessionFinished = onBack,
                modifier = modifier
            )
        }
        is CompiledRoute.Lesson -> {
            val lesson = bundle.lessons.firstOrNull { it.id == route.id }
            if (lesson == null) {
                AcademyMainUiMessage("درس موردنظر پیدا نشد.", modifier)
            } else {
                AcademyMainUiLessonScreen(
                    lesson = lesson,
                    onExerciseClick = { onNavigate(CompiledRoute.Exercise(it)) },
                    onQuizClick = { onNavigate(CompiledRoute.Quiz(it)) },
                    onProjectClick = { onNavigate(CompiledRoute.Project(it)) },
                    modifier = modifier.padding(16.dp)
                )
            }
        }
        is CompiledRoute.Quiz -> {
            val quiz = bundle.quizzes.firstOrNull { it.id == route.id }
            if (quiz == null) {
                AcademyMainUiMessage("آزمون موردنظر پیدا نشد.", modifier)
            } else {
                AcademyMainUiQuizScreen(
                    quiz = quiz,
                    modifier = modifier,
                    onCompleted = { score ->
                        scope.launch {
                            runtime.quizHistoryRepository.record(quiz, score, System.currentTimeMillis())
                            if (placementQuizId != null && quiz.id == placementQuizId) {
                                onNavigate(CompiledRoute.Placement)
                            }
                        }
                    }
                )
            }
        }
        is CompiledRoute.Exercise -> {
            val exercise = bundle.exercises.firstOrNull { it.id == route.id }
            if (exercise == null) {
                AcademyMainUiMessage("تمرین موردنظر پیدا نشد.", modifier)
            } else {
                val savedDraft by remember(courseId, exercise.id) {
                    runtime.exerciseDraftRepository.observe(courseId, exercise.id)
                }.collectAsState(initial = null)
                AcademyMainUiExerciseScreen(
                    exercise = exercise,
                    initialAnswer = savedDraft?.answer.orEmpty(),
                    modifier = modifier,
                    onDraftChanged = { answer ->
                        scope.launch {
                            runtime.exerciseDraftRepository.save(
                                ExerciseDraft(courseId, exercise.id, answer, System.currentTimeMillis())
                            )
                        }
                    },
                    onCompleted = { answer ->
                        scope.launch {
                            val now = System.currentTimeMillis()
                            runtime.exerciseDraftRepository.save(ExerciseDraft(courseId, exercise.id, answer, now))
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
        }
        is CompiledRoute.Project -> {
            val project = bundle.projects.firstOrNull { it.id == route.id }
            if (project == null) {
                AcademyMainUiMessage("پروژه موردنظر پیدا نشد.", modifier)
            } else {
                val progress by remember(courseId, project.id) {
                    runtime.projectProgressRepository.observe(courseId, project.id)
                }.collectAsState(initial = null)
                AcademyMainUiProjectScreen(
                    project = project,
                    progress = progress,
                    modifier = modifier,
                    onProgressChanged = { next ->
                        scope.launch {
                            runtime.projectProgressRepository.save(next)
                            next.completedAtEpochMillis?.let { completedAt ->
                                runtime.learningCompletionRepository.save(
                                    LearningCompletion(
                                        courseId = courseId,
                                        targetType = LearningTargetType.PROJECT,
                                        targetId = project.id,
                                        completed = true,
                                        completedAtEpochMillis = completedAt
                                    )
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

private sealed interface CompiledRoute {
    data object Home : CompiledRoute
    data object Catalog : CompiledRoute
    data object Settings : CompiledRoute
    data object About : CompiledRoute
    data object Placement : CompiledRoute
    data object WeakReview : CompiledRoute
    data object Flashcards : CompiledRoute
    data class Lesson(val id: String) : CompiledRoute
    data class Quiz(val id: String) : CompiledRoute
    data class Exercise(val id: String) : CompiledRoute
    data class Project(val id: String) : CompiledRoute

    fun encode(): String = when (this) {
        Home -> "home"
        Catalog -> "catalog"
        Settings -> "settings"
        About -> "about"
        Placement -> "placement"
        WeakReview -> "weak-review"
        Flashcards -> "flashcards"
        is Lesson -> "lesson:$id"
        is Quiz -> "quiz:$id"
        is Exercise -> "exercise:$id"
        is Project -> "project:$id"
    }

    companion object {
        fun decode(value: String): CompiledRoute {
            val separator = value.indexOf(':')
            val key = if (separator >= 0) value.substring(0, separator) else value
            val id = if (separator >= 0) value.substring(separator + 1) else ""
            return when (key) {
                "catalog" -> Catalog
                "settings" -> Settings
                "about" -> About
                "placement" -> Placement
                "weak-review" -> WeakReview
                "flashcards" -> Flashcards
                "lesson" -> Lesson(id)
                "quiz" -> Quiz(id)
                "exercise" -> Exercise(id)
                "project" -> Project(id)
                else -> Home
            }
        }
    }
}

private const val MILLIS_PER_DAY = 86_400_000L
