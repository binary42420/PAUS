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

package io.paus.android.ui.util

import androidx.lifecycle.LifecycleOwner
import io.paus.android.async.Threads
import io.paus.android.data.Database
import io.paus.android.ui.view.SelectTimeSpanView
import io.paus.android.ui.view.SelectTimeSpanViewListener

fun SelectTimeSpanView.bind(database: Database, lifecycleOwner: LifecycleOwner, listener: (Long) -> Unit) {
    database.config().getEnableAlternativeDurationSelectionAsync().observe(lifecycleOwner) {
        enablePickerMode(it)
    }

    this.listener = object: SelectTimeSpanViewListener {
        override fun onTimeSpanChanged(newTimeInMillis: Long) { listener(timeInMillis) }

        override fun setEnablePickerMode(enable: Boolean) {
            Threads.database.execute {
                database.config().setEnableAlternativeDurationSelectionSync(enable)
            }
        }
    }
}