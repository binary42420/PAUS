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
package io.paus.android.ui.manage.child.category

import io.paus.android.data.model.Category

sealed class ManageChildCategoriesListItem
object CategoriesIntroductionHeader: ManageChildCategoriesListItem()
object CreateCategoryItem: ManageChildCategoriesListItem()
object ManipulationWarningCategoryItem: ManageChildCategoriesListItem()
data class CategoryItem(
        val category: Category,
        val isBlockedTimeNow: Boolean,
        val remainingTimeToday: Long?,
        val usedTimeToday: Long,
        val usedForNotAssignedApps: Boolean,
        val parentCategoryId: String?,
        val categoryNestingLevel: Int,
        val mode: CategorySpecialMode
): ManageChildCategoriesListItem()

sealed class CategorySpecialMode {
    object None: CategorySpecialMode()
    data class TemporarilyBlocked(val endTime: Long?): CategorySpecialMode()
    data class TemporarilyAllowed(val endTime: Long): CategorySpecialMode()
}