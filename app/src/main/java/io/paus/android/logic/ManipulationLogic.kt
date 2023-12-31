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

import android.content.Intent
import android.os.Build
import io.paus.android.async.Threads
import io.paus.android.coroutines.executeAndWait
import io.paus.android.coroutines.runAsync
import io.paus.android.data.model.ExperimentalFlags
import io.paus.android.ui.MainActivity
import io.paus.android.ui.manipulation.UnlockAfterManipulationActivity

class ManipulationLogic(val appLogic: AppLogic) {
    companion object {
        private const val LOG_TAG = "ManipulationLogic"
    }

    init {
        runAsync {
            Threads.database.executeAndWait {
                if (appLogic.database.config().wasDeviceLockedSync()) {
                    showManipulationScreen()
                }
            }
        }
    }

    fun lockDeviceSync() {
        if (!AnnoyLogic.ENABLE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (appLogic.platformIntegration.setLockTaskPackages(listOf(appLogic.context.packageName))) {
                    appLogic.database.config().setWasDeviceLockedSync(true)

                    showManipulationScreen()
                }
            } else {
                if (lockDeviceSync("paus1234")) {
                    appLogic.database.config().setWasDeviceLockedSync(true)

                    showManipulationScreen()
                }
            }
        }
    }

    private fun lockDeviceSync(password: String) = appLogic.platformIntegration.trySetLockScreenPassword(password)

    private fun showManipulationScreen() {
        appLogic.context.startActivity(
                Intent(appLogic.context, UnlockAfterManipulationActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun showManipulationUnlockedScreen() {
        appLogic.context.startActivity(
                Intent(appLogic.context, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun unlockDeviceSync() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appLogic.database.config().setWasDeviceLockedSync(false)
        } else {
            if (lockDeviceSync("")) {
                appLogic.database.config().setWasDeviceLockedSync(false)
            }
        }
    }

    fun reportManualUnlock() {
        Threads.database.execute {
            appLogic.database.runInTransaction {
                if (appLogic.database.config().getOwnDeviceIdSync() != null) {
                    if (appLogic.database.config().wasDeviceLockedSync()) {
                        appLogic.database.config().setWasDeviceLockedSync(false)

                        showManipulationUnlockedScreen()
                    }
                }
            }
        }
    }
}
