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
package io.paus.android.sync.actions.apply

import io.paus.android.async.Threads
import io.paus.android.coroutines.executeAndWait
import io.paus.android.data.Database
import io.paus.android.integration.platform.PlatformIntegration
import io.paus.android.logic.AppLogic
import io.paus.android.logic.ManipulationLogic
import io.paus.android.sync.actions.AppLogicAction
import io.paus.android.sync.actions.ChildAction
import io.paus.android.sync.actions.ParentAction
import io.paus.android.sync.actions.dispatch.LocalDatabaseAppLogicActionDispatcher
import io.paus.android.sync.actions.dispatch.LocalDatabaseChildActionDispatcher
import io.paus.android.sync.actions.dispatch.LocalDatabaseParentActionDispatcher

object ApplyActionUtil {
    suspend fun applyAppLogicAction(
            action: AppLogicAction,
            appLogic: AppLogic,
            ignoreIfDeviceIsNotConfigured: Boolean
    ) {
        applyAppLogicAction(action, appLogic.database, appLogic.manipulationLogic, ignoreIfDeviceIsNotConfigured)
    }

    private suspend fun applyAppLogicAction(
            action: AppLogicAction,
            database: Database,
            manipulationLogic: ManipulationLogic,
            ignoreIfDeviceIsNotConfigured: Boolean
    ) {
        // uncomment this if you need to know what's dispatching an action
        /*
        if (BuildConfig.DEBUG) {
            try {
                throw Exception()
            } catch (ex: Exception) {
                Log.d(LOG_TAG, "handling action: $action", ex)
            }
        }
        */

        Threads.database.executeAndWait {
            database.runInTransaction {
                val ownDeviceId = database.config().getOwnDeviceIdSync()

                if (ownDeviceId == null && ignoreIfDeviceIsNotConfigured) {
                    return@runInTransaction
                }

                LocalDatabaseAppLogicActionDispatcher.dispatchAppLogicActionSync(action, ownDeviceId!!, database, manipulationLogic)
            }
        }
    }

    suspend fun applyParentAction(action: ParentAction, database: Database, platformIntegration: PlatformIntegration, fromChildSelfLimitAddChildUserId: String?, parentUserId: String?) {
        Threads.database.executeAndWait {
            database.runInTransaction {
                LocalDatabaseParentActionDispatcher.dispatchParentActionSync(action, database, fromChildSelfLimitAddChildUserId, parentUserId)
            }
        }
    }

    suspend fun applyChildAction(action: ChildAction, childUserId: String, database: Database) {
        Threads.database.executeAndWait {
            database.runInTransaction {
                LocalDatabaseChildActionDispatcher.dispatchChildActionSync(action, childUserId, database)
            }
        }
    }
}
