package com.asdevelopers.academy.viewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.SelectionContainer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asdevelopers.academy.course.model.CourseBranding
import com.asdevelopers.academy.mainui.AcademyMainUiTheme
import kotlinx.coroutines.launch

/**
 * Installable integrated browser for AS-Academy-MainCourse.
 * Course data is copied into assets at build time; this activity only owns presentation and navigation.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainCourseViewerApp()
        }
    }
}

/** Root application state kept intentionally small so the viewer remains a diagnostic/inspection APK. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainCourseViewerApp() {
    val context = LocalContext.current
    var courses by remember { mutableStateOf<List<CourseContent>?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var selectedCourseSlug by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedEntryPath by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedEntryTitle by rememberSaveable { mutableStateOf<String?>(null) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showAbout by rememberSaveable { mutableStateOf(false) }
    var darkTheme by rememberSaveable { mutableStateOf(false) }
    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }

    // Load the full embedded MainCourse snapshot exactly once for this activity instance.
    LaunchedEffect(Unit) {
        runCatching { MainCourseRepository.load(context) }
            .onSuccess { courses = it }
            .onFailure { error ->
                loadError = error.message ?: "خطای ناشناخته هنگام خواندن MainCourse"
                courses = emptyList()
            }
    }

    val selectedCourse = courses?.firstOrNull { it.slug == selectedCourseSlug }
    val selectedEntry = selectedCourse?.entries?.firstOrNull { entry ->
        entry.sourcePath == selectedEntryPath && entry.title == selectedEntryTitle
    }
    val branding = remember {
        CourseBranding(
            primaryColorHex = "#1D4ED8",
            secondaryColorHex = "#0F766E",
            accentColorHex = "#7C3AED",
            logoAssetId = null,
            heroAssetId = null,
            iconAssetId = null
        )
    }

    // The MainUi shared theme is the visual boundary used by individual Academy course apps as well.
    AcademyMainUiTheme(
        branding = branding,
        darkTheme = darkTheme
    ) {
        CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Rtl) {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            // Android back moves through Reader -> Course -> Home instead of exiting unexpectedly.
            BackHandler(enabled = selectedEntry != null || selectedCourse != null || showSettings) {
                when {
                    selectedEntry != null -> {
                        selectedEntryPath = null
                        selectedEntryTitle = null
                    }
                    selectedCourse != null -> selectedCourseSlug = null
                    showSettings -> showSettings = false
                }
            }

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    AcademyDrawer(
                        onHome = {
                            selectedEntryPath = null
                            selectedEntryTitle = null
                            selectedCourseSlug = null
                            showSettings = false
                            scope.launch { drawerState.close() }
                        },
                        onSearch = {
                            selectedEntryPath = null
                            selectedEntryTitle = null
                            selectedCourseSlug = null
                            showSettings = false
                            scope.launch { drawerState.close() }
                        },
                        onSettings = {
                            selectedEntryPath = null
                            selectedEntryTitle = null
                            selectedCourseSlug = null
                            showSettings = true
                            scope.launch { drawerState.close() }
                        },
                        onShare = {
                            shareViewer(context)
                            scope.launch { drawerState.close() }
                        },
                        onAbout = {
                            showAbout = true
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            ) {
                val canGoBack = selectedEntry != null || selectedCourse != null || showSettings
                val title = when {
                    selectedEntry != null -> selectedEntry.title
                    selectedCourse != null -> selectedCourse.titleFa
                    showSettings -> "تنظیمات"
                    else -> "AS Academy MainCourse"
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        if (canGoBack) {
                                            when {
                                                selectedEntry != null -> {
                                                    selectedEntryPath = null
                                                    selectedEntryTitle = null
                                                }
                                                selectedCourse != null -> selectedCourseSlug = null
                                                showSettings -> showSettings = false
                                            }
                                        } else {
                                            scope.launch { drawerState.open() }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (canGoBack) Icons.Default.ArrowBack else Icons.Default.Menu,
                                        contentDescription = if (canGoBack) "بازگشت" else "منو"
                                    )
                                }
                            }
                        )
                    }
                ) { padding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        when {
                            courses == null -> LoadingScreen()
                            loadError != null -> MessageScreen("بارگذاری کامل نشد:\n$loadError")
                            showSettings -> SettingsScreen(
                                darkTheme = darkTheme,
                                notificationsEnabled = notificationsEnabled,
                                onDarkThemeChange = { darkTheme = it },
                                onNotificationsChange = { notificationsEnabled = it }
                            )
                            selectedEntry != null -> ReaderScreen(selectedEntry)
                            selectedCourse != null -> CourseScreen(
                                course = selectedCourse,
                                onOpenEntry = { entry ->
                                    selectedEntryPath = entry.sourcePath
                                    selectedEntryTitle = entry.title
                                }
                            )
                            else -> DashboardScreen(
                                courses = courses.orEmpty(),
                                onOpenCourse = { course -> selectedCourseSlug = course.slug },
                                onOpenEntry = { entry ->
                                    selectedCourseSlug = entry.courseSlug
                                    selectedEntryPath = entry.sourcePath
                                    selectedEntryTitle = entry.title
                                }
                            )
                        }
                    }
                }
            }

            if (showAbout) {
                AboutDialog(onDismiss = { showAbout = false })
            }
        }
    }
}

/** Standard right-side Academy drawer with profile, settings, sharing and About Software actions. */
@Composable
private fun AcademyDrawer(
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onShare: () -> Unit,
    onAbout: () -> Unit
) {
    var profileSelected by rememberSaveable { mutableStateOf(false) }
    val profilePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        profileSelected = uri != null
    }

    ModalDrawerSheet(modifier = Modifier.width(320.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = { profilePicker.launch("image/*") },
                modifier = Modifier.size(76.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "انتخاب تصویر پروفایل",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text("AS Academy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                text = if (profileSelected) "تصویر پروفایل انتخاب شد" else "برای انتخاب تصویر پروفایل لمس کنید",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Divider()
        NavigationDrawerItem(
            label = { Text("خانه") },
            selected = false,
            onClick = onHome,
            icon = { Icon(Icons.Default.Home, contentDescription = null) }
        )
        NavigationDrawerItem(
            label = { Text("جست‌وجوی سراسری") },
            selected = false,
            onClick = onSearch,
            icon = { Icon(Icons.Default.Search, contentDescription = null) }
        )
        NavigationDrawerItem(
            label = { Text("تنظیمات") },
            selected = false,
            onClick = onSettings,
            icon = { Icon(Icons.Default.Settings, contentDescription = null) }
        )
        NavigationDrawerItem(
            label = { Text("اشتراک‌گذاری") },
            selected = false,
            onClick = onShare,
            icon = { Icon(Icons.Default.Share, contentDescription = null) }
        )
        NavigationDrawerItem(
            label = { Text("درباره نرم‌افزار") },
            selected = false,
            onClick = onAbout,
            icon = { Icon(Icons.Default.Info, contentDescription = null) }
        )

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Develop by AS Team Group",
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/** Home screen: course catalog when idle and cross-course content search when a query is entered. */
@Composable
private fun DashboardScreen(
    courses: List<CourseContent>,
    onOpenCourse: (CourseContent) -> Unit,
    onOpenEntry: (ContentEntry) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedSectionName by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedSection = ContentSection.entries.firstOrNull { it.name == selectedSectionName }
    val allEntries = remember(courses) { courses.flatMap { it.entries } }
    val normalizedQuery = query.trim().lowercase()
    val results = remember(allEntries, normalizedQuery, selectedSection) {
        if (normalizedQuery.isBlank()) emptyList()
        else allEntries.asSequence()
            .filter { selectedSection == null || it.section == selectedSection }
            .filter { it.searchableText.contains(normalizedQuery) }
            .take(300)
            .toList()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "نمای یکپارچه همه آموزش‌ها",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${courses.size} دوره • ${allEntries.size} محتوای قابل مرور و جست‌وجو",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("جست‌وجو در همه دوره‌ها، درس‌ها و فعالیت‌ها") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
        }

        item {
            SectionFilters(
                selectedSection = selectedSection,
                onSelected = { section -> selectedSectionName = section?.name }
            )
        }

        if (normalizedQuery.isBlank()) {
            items(courses, key = { it.slug }) { course ->
                CourseCard(course = course, onClick = { onOpenCourse(course) })
            }
        } else {
            item {
                Text(
                    text = "${results.size} نتیجه نمایش داده می‌شود",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            items(
                items = results,
                key = { entry -> "${entry.courseSlug}:${entry.sourcePath}:${entry.title}" }
            ) { entry ->
                EntryCard(entry = entry, showCourse = true, onClick = { onOpenEntry(entry) })
            }
        }
    }
}

/** Course details present all learning assets from lessons through glossary and lab/source files. */
@Composable
private fun CourseScreen(
    course: CourseContent,
    onOpenEntry: (ContentEntry) -> Unit
) {
    var query by rememberSaveable(course.slug) { mutableStateOf("") }
    var selectedSectionName by rememberSaveable(course.slug) { mutableStateOf<String?>(null) }
    val selectedSection = ContentSection.entries.firstOrNull { it.name == selectedSectionName }
    val normalizedQuery = query.trim().lowercase()
    val visible = remember(course, normalizedQuery, selectedSection) {
        course.entries.filter { entry ->
            (selectedSection == null || entry.section == selectedSection) &&
                (normalizedQuery.isBlank() || entry.searchableText.contains(normalizedQuery))
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(course.titleFa, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(course.titleEn, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("نسخه محتوا: ${course.version}")
                    Text("مجموع آیتم‌های قابل نمایش: ${course.entries.size}")
                }
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("جست‌وجو داخل این دوره") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
        }
        item {
            SectionFilters(
                selectedSection = selectedSection,
                onSelected = { section -> selectedSectionName = section?.name },
                course = course
            )
        }
        item {
            Text("${visible.size} مورد", style = MaterialTheme.typography.titleMedium)
        }
        items(
            items = visible,
            key = { entry -> "${entry.sourcePath}:${entry.title}" }
        ) { entry ->
            EntryCard(entry = entry, showCourse = false, onClick = { onOpenEntry(entry) })
        }
    }
}

/** Shared horizontal filters used both by global search and individual course browsing. */
@Composable
private fun SectionFilters(
    selectedSection: ContentSection?,
    onSelected: (ContentSection?) -> Unit,
    course: CourseContent? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedSection == null,
            onClick = { onSelected(null) },
            label = { Text("همه") }
        )
        ContentSection.entries.forEach { section ->
            val count = course?.count(section)
            if (course == null || count != 0) {
                FilterChip(
                    selected = selectedSection == section,
                    onClick = { onSelected(section) },
                    label = {
                        Text(if (count == null) section.labelFa else "${section.labelFa} ($count)")
                    }
                )
            }
        }
    }
}

/** Compact course summary with the core learning activity counts visible before opening it. */
@Composable
private fun CourseCard(course: CourseContent, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(34.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(course.titleFa, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(course.titleEn, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(onClick = onClick, label = { Text("نسخه ${course.version}") })
                AssistChip(onClick = onClick, label = { Text("درس ${course.count(ContentSection.LESSONS)}") })
                AssistChip(onClick = onClick, label = { Text("تمرین ${course.count(ContentSection.EXERCISES)}") })
                AssistChip(onClick = onClick, label = { Text("آزمون ${course.count(ContentSection.QUIZZES)}") })
                AssistChip(onClick = onClick, label = { Text("پروژه ${course.count(ContentSection.PROJECTS)}") })
            }
        }
    }
}

/** One normalized content item; source path stays visible for debugging the underlying Course repository. */
@Composable
private fun EntryCard(entry: ContentEntry, showCourse: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(entry.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (entry.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(entry.section.labelFa, style = MaterialTheme.typography.labelMedium)
                if (showCourse) Text("• ${entry.courseTitle}", style = MaterialTheme.typography.labelMedium)
            }
            Text(
                text = entry.sourcePath,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Reader shows normalized full content and keeps it selectable for inspection/copying during course QA. */
@Composable
private fun ReaderScreen(entry: ContentEntry) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(entry.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (entry.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(entry.subtitle, style = MaterialTheme.typography.bodyLarge)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(entry.section.labelFa) })
                AssistChip(onClick = {}, label = { Text(entry.courseTitle) })
            }
            Text("مسیر منبع: ${entry.sourcePath}", style = MaterialTheme.typography.labelSmall)
        }
        item {
            Divider()
        }
        item {
            SelectionContainer {
                Text(
                    text = entry.body,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp)
                )
            }
        }
    }
}

/** Local preview settings; production course apps can later persist the same choices through Core settings. */
@Composable
private fun SettingsScreen(
    darkTheme: Boolean,
    notificationsEnabled: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    onNotificationsChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("تنظیمات Viewer", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        SettingSwitchRow(
            title = "حالت تیره",
            description = "تغییر ظاهر همین APK یکپارچه",
            checked = darkTheme,
            onCheckedChange = onDarkThemeChange
        )
        SettingSwitchRow(
            title = "اعلان‌ها",
            description = "کلید آماده برای اتصال به Reminderهای Core در نسخه بعد",
            checked = notificationsEnabled,
            onCheckedChange = onNotificationsChange
        )
    }
}

/** Reusable settings row keeps labels and toggles aligned in RTL. */
@Composable
private fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

/** About Software follows the shared AS Team contact/footer convention. */
@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("درباره نرم‌افزار") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("AS Academy MainCourse Viewer برای مشاهده یکپارچه محتوای همه دوره‌های Academy ساخته شده است.")
                Divider()
                Text("راه‌های ارتباطی با ما:", fontWeight = FontWeight.Bold)
                Text("AS.Developers.Support@Gmail.Com")
                Text("نسخه برنامه: 0.1.0")
                Text("Develop by AS Team Group")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("بستن") }
        }
    )
}

/** Standard loading state while the embedded course snapshot is indexed. */
@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator()
            Text("در حال فهرست‌کردن MainCourse…")
        }
    }
}

/** Error/empty state is explicit so missing CI assets are immediately diagnosable. */
@Composable
private fun MessageScreen(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, modifier = Modifier.padding(24.dp))
    }
}

/** Shares a short description of the viewer through Android's normal share sheet. */
private fun shareViewer(context: Context) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "AS Academy MainCourse — نمایش یکپارچه دوره‌ها، درس‌ها، تمرین‌ها، آزمون‌ها و پروژه‌های AS Academy"
        )
    }
    context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری AS Academy"))
}
