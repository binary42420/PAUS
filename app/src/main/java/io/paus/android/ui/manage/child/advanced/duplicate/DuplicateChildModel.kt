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

package io.paus.android.ui.manage.child.advanced.duplicate

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import io.paus.android.BuildConfig
import io.paus.android.R
import io.paus.android.coroutines.runAsync
import io.paus.android.livedata.castDown
import io.paus.android.logic.DefaultAppLogic
import io.paus.android.sync.actions.ParentAction

class DuplicateChildModel (application: Application): AndroidViewModel(application) {
    companion object {
        private const val LOG_TAG = "DuplicateChildModel"
    }

    private val logic = DefaultAppLogic.with(application)
    private val statusInternal = MutableLiveData<Status>().apply { value = Status.WaitingForConfirmation }
    val status = statusInternal.castDown()

    fun start(userId: String) {
        if (statusInternal.value != Status.WaitingForConfirmation) return
        statusInternal.value = Status.Preparing

        runAsync {
            try {
                val result = DuplicateChildActions.calculateDuplicateChildActions(
                        userId = userId,
                        database = logic.database,
                        newUserName = getApplication<Application>().getString(R.string.duplicate_child_user_name)
                )

                statusInternal.value = Status.HasAction(result)
            } catch (ex: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.w(LOG_TAG, "could not clone user $userId", ex)
                }

                statusInternal.value = Status.Failure
            }
        }
    }

    sealed class Status {
        object WaitingForConfirmation: Status()
        object Preparing: Status()
        object Failure: Status()
        class HasAction(val actions: List<ParentAction>): Status()
    }
}