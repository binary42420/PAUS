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
package io.paus.android.ui.mustread

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.paus.android.coroutines.runAsync
import io.paus.android.livedata.castDown
import kotlinx.coroutines.delay

class MustReadModel: ViewModel() {
    private val timerInternal = MutableLiveData<Int>()
    val timer = timerInternal.castDown()

    init {
        runAsync {
            for (i in 10 downTo 0) {
                timerInternal.value = i
                delay(1000)
            }
        }
    }
}
