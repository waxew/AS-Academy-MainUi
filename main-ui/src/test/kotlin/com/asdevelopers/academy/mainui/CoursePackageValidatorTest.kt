package com.asdevelopers.academy.mainui

import org.junit.Assert.assertTrue
import org.junit.Test

class CoursePackageValidatorTest {
    @Test
    fun validPackageHasNoErrors() {
        val report = CoursePackageValidator.validate(
            CoursePackageValidationInput(
                levelIds = listOf("beginner"),
                chapters = listOf(ChapterReference("chapter-1", "beginner")),
                lessons = listOf(LessonReference("lesson-1", "chapter-1")),
                blockReferences = listOf(BlockReference("block-1", BlockReferenceType.QUIZ, "quiz-1")),
                quizIds = listOf("quiz-1"),
                exerciseIds = emptyList(),
                projectIds = emptyList()
            )
        )

        assertTrue(report.errors.toString(), report.isValid)
    }

    @Test
    fun detectsDuplicateAndDanglingReferences() {
        val report = CoursePackageValidator.validate(
            CoursePackageValidationInput(
                levelIds = listOf("beginner", "beginner"),
                chapters = listOf(ChapterReference("chapter-1", "missing-level")),
                lessons = listOf(LessonReference("lesson-1", "missing-chapter")),
                blockReferences = listOf(
                    BlockReference("block-quiz", BlockReferenceType.QUIZ, null),
                    BlockReference("block-project", BlockReferenceType.PROJECT, "missing-project")
                ),
                quizIds = emptyList(),
                exerciseIds = emptyList(),
                projectIds = emptyList()
            )
        )

        assertTrue(report.errors.any { "duplicate level id" in it })
        assertTrue(report.errors.any { "missing level" in it })
        assertTrue(report.errors.any { "missing chapter" in it })
        assertTrue(report.errors.any { "has no target id" in it })
        assertTrue(report.errors.any { "missing project" in it })
    }
}
