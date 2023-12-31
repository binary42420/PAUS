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

import io.paus.android.async.Threads
import io.paus.android.data.Database
import io.paus.android.date.DateInTimezone

object UsedTimeDeleter {
    fun deleteOldUsedTimeItems(database: Database, date: DateInTimezone, timestamp: Long) {
        Threads.database.execute {
            database.runInTransaction {
                if (database.config().getOwnDeviceIdSync() == null) {
                    // not configured
                    // => no need to delete anything
                    return@runInTransaction
                }

                database.usedTimes().deleteOldUsedTimeItems(lastDayToKeep = date.dayOfEpoch - date.dayOfWeek)

                database.sessionDuration().deleteOldSessionDurationItemsSync(trustedTimestamp = timestamp)
            }
        }
    }
}