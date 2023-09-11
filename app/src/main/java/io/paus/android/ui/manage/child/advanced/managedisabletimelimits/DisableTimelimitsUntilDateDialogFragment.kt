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
package io.paus.android.ui.manage.child.advanced.managedisablepauss

import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.paus.android.R
import io.paus.android.coroutines.runAsync
import io.paus.android.data.model.User
import io.paus.android.date.DateInTimezone
import io.paus.android.extensions.showSafe
import io.paus.android.livedata.waitForNullableValue
import io.paus.android.logic.AppLogic
import io.paus.android.logic.DefaultAppLogic
import io.paus.android.sync.actions.SetUserDisableLimitsUntilAction
import io.paus.android.ui.main.ActivityViewModel
import io.paus.android.ui.main.getActivityViewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import java.util.*

class DisablePaussUntilDateDialogFragment: DialogFragment() {
    companion object {
        fun newInstance(childId: String) = DisablePaussUntilDateDialogFragment().apply {
            arguments = buildArguments(childId)
        }

        fun buildArguments(childId: String) = Bundle().apply {
            putString(CHILD_ID, childId)
        }

        private const val CHILD_ID = "childId"
        private const val DIALOG_TAG = "DisablePaussUntilDateDialogFragment"
    }

    private val childId: String by lazy { arguments!!.getString(CHILD_ID)!! }
    private val auth: ActivityViewModel by lazy { getActivityViewModel(activity!!) }
    private val logic: AppLogic by lazy { DefaultAppLogic.with(activity!!) }
    private val childEntry: LiveData<User?> by lazy { logic.database.user().getChildUserByIdLive(childId) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        childEntry.observe(this, Observer {
            if (it == null) {
                dismissAllowingStateLoss()
            }
        })

        auth.authenticatedUser.observe(this, Observer {
            if (it == null) {
                dismissAllowingStateLoss()
            }
        })
    }

    private fun getCurrentTime() = logic.timeApi.getCurrentTimeInMillis()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = DatePicker(context)
        view.id = R.id.disable_time_pauses

        if (savedInstanceState == null) {
            runAsync {
                val child = childEntry.waitForNullableValue()

                if (child != null) {
                    val dayOfEpoch = DateInTimezone.newInstance(getCurrentTime(), TimeZone.getTimeZone(child.timeZone)).dayOfEpoch.toLong()

                    val nextDayStart = LocalDate.ofEpochDay(dayOfEpoch)
                            .plusDays(1)
                            .atStartOfDay(ZoneId.of(child.timeZone))
                            .toEpochSecond() * 1000

                    view.minDate = nextDayStart
                }
            }
        }

        return AlertDialog.Builder(context!!, theme)
                .setTitle(R.string.manage_disable_time_pauses_dialog_until)
                .setView(view)
                .setPositiveButton(R.string.generic_ok) {
                    _, _ ->

                    val child = childEntry.value
                    val now = getCurrentTime()

                    if (child != null) {
                        val timestamp = LocalDate.of(view.year, view.month + 1, view.dayOfMonth)
                                .atStartOfDay(ZoneId.of(child.timeZone))
                                .toEpochSecond() * 1000

                        if (timestamp <= now) {
                            Toast.makeText(context!!, R.string.manage_disable_time_pauses_toast_time_in_past, Toast.LENGTH_SHORT).show()
                        } else {
                            auth.tryDispatchParentAction(
                                    SetUserDisableLimitsUntilAction(
                                            childId = childId,
                                            timestamp = timestamp
                                    )
                            )
                        }
                    }
                }
                .setNegativeButton(R.string.generic_cancel, null)
                .create()
    }

    fun show(manager: FragmentManager) {
        showSafe(manager, DIALOG_TAG)
    }
}
