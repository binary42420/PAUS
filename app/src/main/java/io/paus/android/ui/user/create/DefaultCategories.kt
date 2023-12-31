/*
 * Paus Copyright (C) 2023 Ryley Holmes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package io.paus.android.ui.user.create

import android.content.Context
import io.paus.android.R
import io.paus.android.data.IdGenerator
import io.paus.android.data.model.pausRule
import io.paus.android.extensions.MinuteOfDay
import java.util.*

class DefaultCategories private constructor(private val context: Context) {
    companion object {
        private var instance: DefaultCategories? = null

        fun with(context: Context): DefaultCategories {
            if (instance == null) {
                instance = DefaultCategories(context.applicationContext)
            }

            return instance!!
        }
    }

    val allowedAppsTitle get() = context.getString(R.string.setup_category_allowed)
    val allowedGamesTitle get() = context.getString(R.string.setup_category_games)

    fun generateGamesPausRules(categoryId: String): List<PausRule> {
        val rules = mutableListOf<PausRule>()

        // maximum time for each workday
        rules.add(
                PausRule(
                        id = IdGenerator.generateId(),
                        categoryId = categoryId,
                        applyToExtraTimeUsage = false,
                        dayMask = (1 or 2 or 4 or 8 or 16).toByte(),
                        maximumTimeInMillis = 1000 * 60 * 30,    // 30 minutes
                        startMinuteOfDay = PausRule.MIN_START_MINUTE,
                        endMinuteOfDay = PausRule.MAX_END_MINUTE,
                        sessionPauseMilliseconds = 0,
                        sessionDurationMilliseconds = 0,
                        perDay = true
                )
        )

        // maximum time for each weekend day
        rules.add(
                PausRule(
                        id = IdGenerator.generateId(),
                        categoryId = categoryId,
                        applyToExtraTimeUsage = false,
                        dayMask = (32 or 64).toByte(),
                        maximumTimeInMillis = 1000 * 60 * 60 * 3,    // 3 hours
                        startMinuteOfDay = PausRule.MIN_START_MINUTE,
                        endMinuteOfDay = PausRule.MAX_END_MINUTE,
                        sessionPauseMilliseconds = 0,
                        sessionDurationMilliseconds = 0,
                        perDay = true
                )
        )

        // maximum time per total week
        rules.add(
                PausRule(
                        id = IdGenerator.generateId(),
                        categoryId = categoryId,
                        applyToExtraTimeUsage = false,
                        dayMask = (1 + 2 + 4 + 8 + 16 + 32 + 64).toByte(),
                        maximumTimeInMillis = 1000 * 60 * 60 * 6,    // 6 hours
                        startMinuteOfDay = PausRule.MIN_START_MINUTE,
                        endMinuteOfDay = PausRule.MAX_END_MINUTE,
                        sessionPauseMilliseconds = 0,
                        sessionDurationMilliseconds = 0,
                        perDay = false
                )
        )

        // blocked time areas for weekdays
        rules.add(
                PausRule(
                        id = IdGenerator.generateId(),
                        categoryId = categoryId,
                        applyToExtraTimeUsage = true,
                        dayMask = (1 + 2 + 4 + 8 + 16).toByte(),
                        maximumTimeInMillis = 0,
                        startMinuteOfDay = 0,
                        endMinuteOfDay = 6 * 60 - 1,
                        sessionPauseMilliseconds = 0,
                        sessionDurationMilliseconds = 0,
                        perDay = false
                )
        )

        rules.add(
                PausRule(
                        id = IdGenerator.generateId(),
                        categoryId = categoryId,
                        applyToExtraTimeUsage = true,
                        dayMask = (1 + 2 + 4 + 8 + 16).toByte(),
                        maximumTimeInMillis = 0,
                        startMinuteOfDay = 18 * 60,
                        endMinuteOfDay = MinuteOfDay.MAX,
                        sessionPauseMilliseconds = 0,
                        sessionDurationMilliseconds = 0,
                        perDay = false
                )
        )

        // blocked time areas for the weekend
        rules.add(
                PausRule(
                        id = IdGenerator.generateId(),
                        categoryId = categoryId,
                        applyToExtraTimeUsage = true,
                        dayMask = (32 + 64).toByte(),
                        maximumTimeInMillis = 0,
                        startMinuteOfDay = 0,
                        endMinuteOfDay = 9 * 60 - 1,
                        sessionPauseMilliseconds = 0,
                        sessionDurationMilliseconds = 0,
                        perDay = false
                )
        )

        rules.add(
                PausRule(
                        id = IdGenerator.generateId(),
                        categoryId = categoryId,
                        applyToExtraTimeUsage = true,
                        dayMask = (32 + 64).toByte(),
                        maximumTimeInMillis = 0,
                        startMinuteOfDay = 20 * 60,
                        endMinuteOfDay = MinuteOfDay.MAX,
                        sessionPauseMilliseconds = 0,
                        sessionDurationMilliseconds = 0,
                        perDay = false
                )
        )

        return rules.toList()
    }
}
