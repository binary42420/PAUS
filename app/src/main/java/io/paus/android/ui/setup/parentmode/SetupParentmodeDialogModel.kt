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

package io.paus.android.ui.setup.parentmode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.paus.android.async.Threads
import io.paus.android.coroutines.executeAndWait
import io.paus.android.coroutines.runAsync
import io.paus.android.crypto.Curve25519
import io.paus.android.data.Database

class SetupParentmodeDialogModel: ViewModel() {
    private var didInit = false
    private val hadSuccess = MutableLiveData<Boolean>()

    fun init(database: Database): LiveData<Boolean> {
        if (!didInit) {
            didInit = true

            runAsync {
                val keys = Threads.crypto.executeAndWait { Curve25519.generateKeyPair() }
                val ok = Threads.database.executeAndWait {
                    database.runInTransaction {
                        if (database.config().getOwnDeviceIdSync() != null) {
                            false
                        } else if (database.config().getParentModeKeySync() != null) {
                            true
                        } else {
                            database.config().setParentModeKeySync(keys)

                            true
                        }
                    }
                }

                hadSuccess.value = ok
            }
        }

        return hadSuccess
    }
}