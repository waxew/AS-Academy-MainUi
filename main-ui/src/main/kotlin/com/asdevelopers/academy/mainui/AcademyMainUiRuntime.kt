package com.asdevelopers.academy.mainui

import android.content.Context
import com.asdevelopers.academy.core.database.AcademyDatabase
import com.asdevelopers.academy.core.repository.AchievementRepository
import com.asdevelopers.academy.core.repository.BookmarkRepository
import com.asdevelopers.academy.core.repository.ProgressRepository
import com.asdevelopers.academy.core.repository.SearchRepository
import com.asdevelopers.academy.core.repository.UserNoteRepository

/**
 * Core-backed state dependencies consumed by MainUi.
 *
 * Keeping these repositories in one runtime object prevents Course Apps from creating their own
 * persistence layer while still allowing tests or specialized hosts to inject controlled instances.
 */
class AcademyMainUiRuntime private constructor(
    val database: AcademyDatabase,
    val progressRepository: ProgressRepository,
    val bookmarkRepository: BookmarkRepository,
    val userNoteRepository: UserNoteRepository,
    val searchRepository: SearchRepository,
    val achievementRepository: AchievementRepository
) {
    companion object {
        fun create(context: Context, databaseName: String = "as_academy.db"): AcademyMainUiRuntime {
            val database = AcademyDatabase.create(context.applicationContext, databaseName)
            return AcademyMainUiRuntime(
                database = database,
                progressRepository = ProgressRepository(database.progressDao()),
                bookmarkRepository = BookmarkRepository(database.bookmarkDao()),
                userNoteRepository = UserNoteRepository(database.userNoteDao()),
                searchRepository = SearchRepository(database.searchDao()),
                achievementRepository = AchievementRepository(database.achievementDao())
            )
        }
    }
}
