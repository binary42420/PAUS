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
package io.paus.android.logic

class RealTimeLogic(appLogic: AppLogic) {
    init {
        appLogic.platformIntegration.systemClockChangeListener = Runnable {
            callTimeModificationListeners()
        }
    }

    private val timeModificationListeners = mutableSetOf<() -> Unit>()

    fun registerTimeModificationListener(listener: () -> Unit) = synchronized(timeModificationListeners) {
        timeModificationListeners.add(listener)
    }

    fun unregisterTimeModificationListener(listener: () -> Unit) = synchronized(timeModificationListeners) {
        timeModificationListeners.remove(listener)
    }

    fun callTimeModificationListeners() = synchronized(timeModificationListeners) {
        timeModificationListeners.forEach { it() }
    }
}