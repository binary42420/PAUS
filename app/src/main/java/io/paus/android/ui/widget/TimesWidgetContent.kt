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
package io.paus.android.ui.widget

sealed class TimesWidgetContent {
        object UnconfiguredDevice: TimesWidgetContent()
        data class NoChildUser(val canSwitchToDefaultUser: Boolean): TimesWidgetContent()
        data class Categories(
                val categories: List<Item>,
                val canSwitchToDefaultUser: Boolean
        ): TimesWidgetContent() {
                data class Item(
                        val categoryId: String,
                        val categoryName: String,
                        val level: Int,
                        val remainingTimeToday: Long?
                )
        }
}