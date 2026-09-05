package com.asdevelopers.academy.mainui

/** Lightweight validation model kept independent from Core so it can be unit-tested in isolation. */
internal data class CoursePackageValidationInput(
    val levelIds: List<String>,
    val chapters: List<ChapterReference>,
    val lessons: List<LessonReference>,
    val blockReferences: List<BlockReference>,
    val quizIds: List<String>,
    val exerciseIds: List<String>,
    val projectIds: List<String>
)

internal data class ChapterReference(
    val id: String,
    val levelId: String
)

internal data class LessonReference(
    val id: String,
    val chapterId: String
)

internal enum class BlockReferenceType {
    QUIZ,
    EXERCISE,
    PROJECT
}

internal data class BlockReference(
    val blockId: String,
    val type: BlockReferenceType,
    val targetId: String?
)

internal data class CoursePackageValidationReport(
    val errors: List<String>
) {
    val isValid: Boolean get() = errors.isEmpty()
}

internal object CoursePackageValidator {
    fun validate(input: CoursePackageValidationInput): CoursePackageValidationReport {
        val errors = mutableListOf<String>()

        validateIds("level", input.levelIds, errors)
        validateIds("chapter", input.chapters.map { it.id }, errors)
        validateIds("lesson", input.lessons.map { it.id }, errors)
        validateIds("quiz", input.quizIds, errors)
        validateIds("exercise", input.exerciseIds, errors)
        validateIds("project", input.projectIds, errors)

        val levelIds = input.levelIds.toSet()
        input.chapters.forEach { chapter ->
            if (chapter.levelId.isBlank()) {
                errors += "chapter '${chapter.id}' has a blank levelId"
            } else if (chapter.levelId !in levelIds) {
                errors += "chapter '${chapter.id}' points to missing level '${chapter.levelId}'"
            }
        }

        val chapterIds = input.chapters.map { it.id }.toSet()
        input.lessons.forEach { lesson ->
            if (lesson.chapterId.isBlank()) {
                errors += "lesson '${lesson.id}' has a blank chapterId"
            } else if (lesson.chapterId !in chapterIds) {
                errors += "lesson '${lesson.id}' points to missing chapter '${lesson.chapterId}'"
            }
        }

        val validTargets = mapOf(
            BlockReferenceType.QUIZ to input.quizIds.toSet(),
            BlockReferenceType.EXERCISE to input.exerciseIds.toSet(),
            BlockReferenceType.PROJECT to input.projectIds.toSet()
        )
        input.blockReferences.forEach { reference ->
            val targetId = reference.targetId?.trim().orEmpty()
            if (targetId.isBlank()) {
                errors += "block '${reference.blockId}' is ${reference.type.name.lowercase()} but has no target id"
            } else if (targetId !in validTargets.getValue(reference.type)) {
                errors += "block '${reference.blockId}' points to missing ${reference.type.name.lowercase()} '$targetId'"
            }
        }

        return CoursePackageValidationReport(errors.distinct())
    }

    private fun validateIds(kind: String, ids: List<String>, errors: MutableList<String>) {
        ids.filter { it.isBlank() }.forEach { errors += "$kind id must not be blank" }
        ids.filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .filterValues { it > 1 }
            .keys
            .forEach { id -> errors += "duplicate $kind id '$id'" }
    }
}
