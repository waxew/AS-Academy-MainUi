package com.asdevelopers.academy.mainui

import android.content.Context
import com.asdevelopers.academy.core.database.AcademyDatabase
import com.asdevelopers.academy.core.notification.StudyReminderScheduler
import com.asdevelopers.academy.core.repository.AchievementRepository
import com.asdevelopers.academy.core.repository.BookmarkRepository
import com.asdevelopers.academy.core.repository.ExerciseDraftRepository
import com.asdevelopers.academy.core.repository.LearningCompletionRepository
import com.asdevelopers.academy.core.repository.ProgressRepository
import com.asdevelopers.academy.core.repository.ProjectProgressRepository
import com.asdevelopers.academy.core.repository.QuizHistoryRepository
import com.asdevelopers.academy.core.repository.SearchRepository
import com.asdevelopers.academy.core.repository.UserNoteRepository
import com.asdevelopers.academy.core.runtime.AcademyRuntime
import com.asdevelopers.academy.core.settings.AcademyPreferencesRepository

/**
 * MainUi view of the canonical Core runtime.
 *
 * Database, repository and service construction belongs to AS-Academy-Core. MainUi only exposes
 * the dependencies it consumes so Course hosts cannot accidentally create a parallel persistence
 * graph that drifts from the Core schema or repository contracts.
 */
class AcademyMainUiRuntime private constructor(
    private val coreRuntime: AcademyRuntime
) {
    val database: AcademyDatabase get() = coreRuntime.database
    val progressRepository: ProgressRepository get() = coreRuntime.progressRepository
    val bookmarkRepository: BookmarkRepository get() = coreRuntime.bookmarkRepository
    val userNoteRepository: UserNoteRepository get() = coreRuntime.userNoteRepository
    val searchRepository: SearchRepository get() = coreRuntime.searchRepository
    val achievementRepository: AchievementRepository get() = coreRuntime.achievementRepository
    val quizHistoryRepository: QuizHistoryRepository get() = coreRuntime.quizHistoryRepository
    val exerciseDraftRepository: ExerciseDraftRepository get() = coreRuntime.exerciseDraftRepository
    val projectProgressRepository: ProjectProgressRepository get() = coreRuntime.projectProgressRepository
    val learningCompletionRepository: LearningCompletionRepository get() = coreRuntime.learningCompletionRepository
    val preferencesRepository: AcademyPreferencesRepository get() = coreRuntime.preferencesRepository
    val studyReminderScheduler: StudyReminderScheduler get() = coreRuntime.studyReminderScheduler

    /** Allows advanced hosts/tests to share an already-created Core runtime with MainUi. */
    fun asCoreRuntime(): AcademyRuntime = coreRuntime

    companion object {
        fun create(
            context: Context,
            databaseName: String = AcademyRuntime.DEFAULT_DATABASE_NAME
        ): AcademyMainUiRuntime = AcademyMainUiRuntime(
            AcademyRuntime.create(context, databaseName)
        )

        /** Reuses an existing Core runtime instead of opening a second database graph. */
        fun fromCore(runtime: AcademyRuntime): AcademyMainUiRuntime = AcademyMainUiRuntime(runtime)
    }
}
