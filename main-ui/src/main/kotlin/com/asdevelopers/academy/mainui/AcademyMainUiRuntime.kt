package com.asdevelopers.academy.mainui

import android.content.Context
import com.asdevelopers.academy.core.runtime.AcademyRuntime

/**
 * MainUi adapter over Core's single composition root.
 *
 * MainUi owns presentation only. It never creates a database, DAO, repository, scheduler, backend
 * client or storage implementation. All stateful services come from [AcademyRuntime].
 */
class AcademyMainUiRuntime private constructor(
    val core: AcademyRuntime
) {
    val progressRepository get() = core.progressRepository
    val bookmarkRepository get() = core.bookmarkRepository
    val userNoteRepository get() = core.userNoteRepository
    val searchRepository get() = core.searchRepository
    val achievementRepository get() = core.achievementRepository
    val quizHistoryRepository get() = core.quizHistoryRepository
    val exerciseDraftRepository get() = core.exerciseDraftRepository
    val projectProgressRepository get() = core.projectProgressRepository
    val learningCompletionRepository get() = core.learningCompletionRepository
    val placementResultRepository get() = core.placementResultRepository
    val weakTopicReviewRepository get() = core.weakTopicReviewRepository
    val flashcardReviewRepository get() = core.flashcardReviewRepository
    val preferencesRepository get() = core.preferencesRepository
    val studyReminderScheduler get() = core.studyReminderScheduler

    companion object {
        /** Preferred entry point for thin Course Apps that already own the Core runtime. */
        fun from(core: AcademyRuntime): AcademyMainUiRuntime = AcademyMainUiRuntime(core)

        /** Convenience entry point; composition still happens exclusively inside Core. */
        fun create(context: Context, databaseName: String = "as_academy.db"): AcademyMainUiRuntime =
            from(AcademyRuntime.create(context, databaseName))
    }
}
