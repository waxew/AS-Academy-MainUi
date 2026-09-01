package com.asdevelopers.academy.viewer

import android.content.Context
import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

/**
 * Logical content groups exposed by the unified viewer.
 * The labels are deliberately Persian because MainCourse is currently authored primarily for Persian learners.
 */
internal enum class ContentSection(val labelFa: String) {
    LESSONS("درس‌ها"),
    EXERCISES("تمرین‌ها"),
    QUIZZES("آزمون‌ها"),
    PROJECTS("پروژه‌ها"),
    GLOSSARY("واژه‌نامه"),
    LABS("آزمایشگاه‌ها"),
    CHAPTERS("فصل‌ها"),
    LEVELS("سطح‌ها"),
    LEARNING_MAP("نقشه یادگیری"),
    SOURCES("منابع"),
    OTHER("سایر محتوا")
}

/** A normalized item that the UI can search and render regardless of the source JSON schema. */
internal data class ContentEntry(
    val courseSlug: String,
    val courseTitle: String,
    val section: ContentSection,
    val title: String,
    val subtitle: String,
    val sourcePath: String,
    val body: String
) {
    /** Search is intentionally text based so newly added MainCourse fields become searchable automatically. */
    val searchableText: String = "$courseTitle $title $subtitle $body $sourcePath".lowercase()
}

/** Summary of one folder under MainCourse/courses plus all normalized content extracted from it. */
internal data class CourseContent(
    val slug: String,
    val titleFa: String,
    val titleEn: String,
    val version: String,
    val entries: List<ContentEntry>
) {
    fun count(section: ContentSection): Int = entries.count { it.section == section }
}

/**
 * Reads the MainCourse snapshot copied into the APK by GitHub Actions.
 * No network call is required at runtime, so the viewer remains useful without connectivity.
 */
internal object MainCourseRepository {
    private const val ROOT = "maincourse/courses"

    /** Heavy asset traversal and JSON parsing run off the main thread. */
    suspend fun load(context: Context): List<CourseContent> = withContext(Dispatchers.IO) {
        val assets = context.assets
        assets.list(ROOT).orEmpty()
            .sorted()
            .mapNotNull { slug -> loadCourse(assets, slug) }
            .sortedBy { it.titleFa }
    }

    /** Loads one course only when its canonical course/manifest.json is present and readable. */
    private fun loadCourse(assets: AssetManager, slug: String): CourseContent? {
        val base = "$ROOT/$slug/course"
        val manifestText = readText(assets, "$base/manifest.json") ?: return null
        val manifest = runCatching { JSONObject(manifestText) }.getOrNull() ?: return null
        val titleFa = manifest.optString("titleFa").ifBlank { slug }
        val titleEn = manifest.optString("titleEn").ifBlank { slug }
        val version = manifest.optString("version").ifBlank { "-" }

        val files = walkFiles(assets, base)
            .filter { path ->
                path.endsWith(".json", ignoreCase = true) ||
                    path.endsWith(".md", ignoreCase = true) ||
                    path.endsWith(".txt", ignoreCase = true)
            }
            .sorted()

        val entries = files.flatMap { absolutePath ->
            val relativePath = absolutePath.removePrefix("$base/")
            val raw = readText(assets, absolutePath).orEmpty()
            normalizeFile(
                slug = slug,
                courseTitle = titleFa,
                relativePath = relativePath,
                raw = raw
            )
        }

        return CourseContent(
            slug = slug,
            titleFa = titleFa,
            titleEn = titleEn,
            version = version,
            entries = entries
        )
    }

    /** Recursively enumerates AssetManager directories without assuming a fixed MainCourse depth. */
    private fun walkFiles(assets: AssetManager, path: String): List<String> {
        val children = assets.list(path).orEmpty()
        if (children.isEmpty()) return listOf(path)
        return children.flatMap { child -> walkFiles(assets, "$path/$child") }
    }

    /** Converts a JSON/Markdown/Text file into one or more searchable entries. */
    private fun normalizeFile(
        slug: String,
        courseTitle: String,
        relativePath: String,
        raw: String
    ): List<ContentEntry> {
        val section = sectionFor(relativePath)
        if (!relativePath.endsWith(".json", ignoreCase = true)) {
            val title = raw.lineSequence()
                .map { it.trim() }
                .firstOrNull { it.startsWith("#") }
                ?.trimStart('#')
                ?.trim()
                .orEmpty()
                .ifBlank { fileTitle(relativePath) }
            return listOf(
                ContentEntry(
                    courseSlug = slug,
                    courseTitle = courseTitle,
                    section = section,
                    title = title,
                    subtitle = relativePath,
                    sourcePath = relativePath,
                    body = raw
                )
            )
        }

        val parsed = runCatching { JSONTokener(raw).nextValue() }.getOrNull()
            ?: return listOf(fallbackEntry(slug, courseTitle, section, relativePath, raw))

        val records = extractRecords(parsed)
        if (records.isEmpty()) {
            return listOf(fallbackEntry(slug, courseTitle, section, relativePath, raw))
        }

        return records.mapIndexed { index, record ->
            when (record) {
                is JSONObject -> {
                    val title = firstString(
                        record,
                        "title",
                        "titleFa",
                        "term",
                        "name",
                        "question",
                        "prompt",
                        "id"
                    ).ifBlank { "${fileTitle(relativePath)} ${index + 1}" }
                    val subtitle = firstString(
                        record,
                        "summary",
                        "description",
                        "definition",
                        "goal",
                        "difficulty",
                        "level"
                    )
                    ContentEntry(
                        courseSlug = slug,
                        courseTitle = courseTitle,
                        section = section,
                        title = title,
                        subtitle = subtitle,
                        sourcePath = relativePath,
                        body = renderJson(record)
                    )
                }

                else -> ContentEntry(
                    courseSlug = slug,
                    courseTitle = courseTitle,
                    section = section,
                    title = "${fileTitle(relativePath)} ${index + 1}",
                    subtitle = relativePath,
                    sourcePath = relativePath,
                    body = record?.toString().orEmpty()
                )
            }
        }
    }

    /**
     * MainCourse commonly stores arrays directly, or wraps arrays under domain keys such as lessons/quizzes.
     * This method recognizes those containers while leaving unknown future objects intact as one entry.
     */
    private fun extractRecords(value: Any?): List<Any?> {
        if (value is JSONArray) {
            return (0 until value.length()).map { index -> value.opt(index) }
        }
        if (value !is JSONObject) return listOf(value)

        val collectionKeys = listOf(
            "lessons",
            "exercises",
            "quizzes",
            "projects",
            "entries",
            "items",
            "terms",
            "chapters",
            "levels",
            "labs",
            "questions"
        )
        collectionKeys.forEach { key ->
            val array = value.optJSONArray(key)
            if (array != null) {
                return (0 until array.length()).map { index -> array.opt(index) }
            }
        }
        return listOf(value)
    }

    /** Produces a readable Persian-oriented text view instead of exposing compact one-line JSON. */
    private fun renderJson(value: Any?, depth: Int = 0): String {
        val indent = "  ".repeat(depth)
        return when (value) {
            is JSONObject -> {
                val keys = value.keys().asSequence().toList()
                keys.joinToString("\n") { key ->
                    val child = value.opt(key)
                    val label = keyLabel(key)
                    when (child) {
                        is JSONObject, is JSONArray -> "$indent$label:\n${renderJson(child, depth + 1)}"
                        JSONObject.NULL, null -> "$indent$label: -"
                        else -> "$indent$label: $child"
                    }
                }
            }

            is JSONArray -> {
                (0 until value.length()).joinToString("\n") { index ->
                    val child = value.opt(index)
                    when (child) {
                        is JSONObject, is JSONArray -> "$indent•\n${renderJson(child, depth + 1)}"
                        JSONObject.NULL, null -> "$indent• -"
                        else -> "$indent• $child"
                    }
                }
            }

            JSONObject.NULL, null -> "$indent-"
            else -> "$indent$value"
        }
    }

    /** Persian labels for common schema fields improve readability while unknown keys remain visible verbatim. */
    private fun keyLabel(key: String): String = when (key) {
        "id" -> "شناسه"
        "courseId" -> "شناسه دوره"
        "chapterId" -> "شناسه فصل"
        "title", "titleFa" -> "عنوان"
        "titleEn" -> "عنوان انگلیسی"
        "summary" -> "خلاصه"
        "description" -> "توضیحات"
        "content" -> "محتوا"
        "definition" -> "تعریف"
        "question" -> "سؤال"
        "prompt" -> "صورت تمرین"
        "answer" -> "پاسخ"
        "explanation" -> "توضیح پاسخ"
        "blocks" -> "بخش‌های درس"
        "type" -> "نوع"
        "tags" -> "برچسب‌ها"
        "prerequisites" -> "پیش‌نیازها"
        "estimatedMinutes" -> "زمان تقریبی (دقیقه)"
        "difficulty" -> "درجه سختی"
        "level" -> "سطح"
        "order" -> "ترتیب"
        "objectives" -> "اهداف"
        "steps" -> "مراحل"
        "choices", "options" -> "گزینه‌ها"
        else -> key
    }

    /** Maps current and future MainCourse paths into stable viewer sections. */
    private fun sectionFor(relativePath: String): ContentSection = when {
        relativePath.startsWith("lessons/") -> ContentSection.LESSONS
        relativePath.startsWith("exercises/") -> ContentSection.EXERCISES
        relativePath.startsWith("quizzes/") -> ContentSection.QUIZZES
        relativePath.startsWith("projects/") -> ContentSection.PROJECTS
        relativePath == "glossary.json" || relativePath.startsWith("glossary/") -> ContentSection.GLOSSARY
        relativePath.startsWith("labs/") -> ContentSection.LABS
        relativePath == "chapters.json" -> ContentSection.CHAPTERS
        relativePath == "levels.json" -> ContentSection.LEVELS
        relativePath == "learning-map.json" -> ContentSection.LEARNING_MAP
        relativePath.contains("source", ignoreCase = true) || relativePath.endsWith(".md", ignoreCase = true) -> ContentSection.SOURCES
        else -> ContentSection.OTHER
    }

    /** Safely reads a UTF-8 asset; a broken optional file should not prevent the rest of the Academy from opening. */
    private fun readText(assets: AssetManager, path: String): String? =
        runCatching { assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() } }.getOrNull()

    /** Picks the first non-empty string from a list of schema keys. */
    private fun firstString(objectValue: JSONObject, vararg keys: String): String {
        keys.forEach { key ->
            val value = objectValue.optString(key).trim()
            if (value.isNotBlank() && value != "null") return value
        }
        return ""
    }

    /** Converts a path into a compact display fallback when source data does not contain a title. */
    private fun fileTitle(relativePath: String): String = relativePath
        .substringAfterLast('/')
        .substringBeforeLast('.')
        .replace('-', ' ')
        .replace('_', ' ')
        .trim()

    /** Preserves invalid or unusual content as visible text rather than silently dropping it. */
    private fun fallbackEntry(
        slug: String,
        courseTitle: String,
        section: ContentSection,
        relativePath: String,
        raw: String
    ): ContentEntry = ContentEntry(
        courseSlug = slug,
        courseTitle = courseTitle,
        section = section,
        title = fileTitle(relativePath),
        subtitle = "نمایش مستقیم فایل",
        sourcePath = relativePath,
        body = raw
    )
}
