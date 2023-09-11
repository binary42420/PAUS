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

import android.content.Context
import android.text.format.DateUtils
import androidx.fragment.app.FragmentActivity
import io.paus.android.R
import io.paus.android.data.model.User
import io.paus.android.data.model.UserType
import io.paus.android.date.DateInTimezone
import io.paus.android.logic.DefaultAppLogic
import io.paus.android.sync.actions.SetUserDisableLimitsUntilAction
import io.paus.android.ui.help.HelpDialogFragment
import io.paus.android.ui.main.getActivityViewModel
import io.paus.android.ui.view.ManageDisablePaussViewHandlers
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import java.util.*

object ManageDisablePaussViewHelper {
    fun createHandlers(childId: String, childTimezone: String, activity: FragmentActivity): ManageDisablePaussViewHandlers {
        val auth = getActivityViewModel(activity)
        val logic = DefaultAppLogic.with(activity)

        fun getCurrentTime() = logic.timeApi.getCurrentTimeInMillis()

        return object : ManageDisablePaussViewHandlers {
            override fun disablePaussForDuration(duration: Long) {
                auth.tryDispatchParentAction(
                        SetUserDisableLimitsUntilAction(
                                childId = childId,
                                timestamp = getCurrentTime() + duration
                        )
                )
            }

            override fun disablePaussForToday() {
                val dayOfEpoch = DateInTimezone.newInstance(getCurrentTime(), TimeZone.getTimeZone(childTimezone)).dayOfEpoch.toLong()

                val nextDayStart = LocalDate.ofEpochDay(dayOfEpoch)
                        .plusDays(1)
                        .atStartOfDay(ZoneId.of(childTimezone))
                        .toEpochSecond() * 1000

                auth.tryDispatchParentAction(
                        SetUserDisableLimitsUntilAction(
                                childId = childId,
                                timestamp = nextDayStart
                        )
                )
            }

            override fun disablePaussUntilSelectedDate() {
                if (auth.requestAuthenticationOrReturnTrue()) {
                    DisablePaussUntilDateDialogFragment.newInstance(childId).show(activity.supportFragmentManager)
                }
            }

            override fun disablePaussUntilSelectedTimeOfToday() {
                if (auth.requestAuthenticationOrReturnTrue()) {
                    DisablePaussUntilTimeDialogFragment.newInstance(childId).show(activity.supportFragmentManager)
                }
            }

            override fun enablePauss() {
                auth.tryDispatchParentAction(
                        SetUserDisableLimitsUntilAction(
                                childId = childId,
                                timestamp = 0
                        )
                )
            }

            override fun showDisablePaussHelp() {
                HelpDialogFragment.newInstance(
                        title = R.string.manage_disable_time_pauses_title,
                        text = R.string.manage_disable_time_pauses_text
                ).show(activity.supportFragmentManager)
            }
        }
    }

    fun getDisabledUntilString(child: User?, currentTime: Long, context: Context): String? {
        if (child == null || child.type != UserType.Child || child.disableLimitsUntil == 0L || child.disableLimitsUntil < currentTime) {
            return null
        } else {
            return DateUtils.formatDateTime(
                    context,
                    child.disableLimitsUntil,
                    DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or
                            DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_WEEKDAY
            )
        }
    }
}
