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
package io.paus.android.ui.manage.device.manage.defaultuser

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.paus.android.R
import io.paus.android.coroutines.runAsync
import io.paus.android.data.model.Device
import io.paus.android.data.model.User
import io.paus.android.databinding.ManageDeviceDefaultUserBinding
import io.paus.android.livedata.map
import io.paus.android.livedata.switchMap
import io.paus.android.sync.actions.SignOutAtDeviceAction
import io.paus.android.sync.actions.apply.ApplyActionUtil
import io.paus.android.ui.help.HelpDialogFragment
import io.paus.android.ui.main.ActivityViewModel
import io.paus.android.util.TimeTextUtil

object ManageDeviceDefaultUser {
    fun bind(
            view: ManageDeviceDefaultUserBinding,
            users: LiveData<List<User>>,
            lifecycleOwner: LifecycleOwner,
            device: LiveData<Device?>,
            isThisDevice: LiveData<Boolean>,
            auth: ActivityViewModel,
            fragmentManager: FragmentManager
    ) {
        val context = view.root.context

        view.titleView.setOnClickListener {
            HelpDialogFragment.newInstance(
                    title = R.string.manage_device_default_user_title,
                    text = R.string.manage_device_default_user_info
            ).show(fragmentManager)
        }

        device.switchMap { deviceEntry ->
            users.map { users ->
                deviceEntry to users.find { it.id == deviceEntry?.defaultUser }
            }
        }.observe(lifecycleOwner, Observer { (deviceEntry, defaultUser) ->
            view.hasDefaultUser = defaultUser != null
            view.isAlreadyUsingDefaultUser = defaultUser != null && deviceEntry?.currentUserId == defaultUser.id
            view.defaultUserTitle = defaultUser?.name
        })

        isThisDevice.observe(lifecycleOwner, Observer {
            view.isCurrentDevice = it
        })

        device.observe(lifecycleOwner, Observer { deviceEntry ->
            view.setDefaultUserButton.setOnClickListener {
                if (deviceEntry != null && auth.requestAuthenticationOrReturnTrue()) {
                    SetDeviceDefaultUserDialogFragment.newInstance(
                            deviceId = deviceEntry.id
                    ).show(fragmentManager)
                }
            }

            view.configureAutoLogoutButton.setOnClickListener {
                if (deviceEntry != null && auth.requestAuthenticationOrReturnTrue()) {
                    SetDeviceDefaultUserTimeoutDialogFragment
                            .newInstance(deviceId = deviceEntry.id)
                            .show(fragmentManager)
                }
            }

            val defaultUserTimeout = deviceEntry?.defaultUserTimeout ?: 0

            view.isAutomaticallySwitchingToDefaultUserEnabled = defaultUserTimeout != 0
            view.defaultUserSwitchText = if (defaultUserTimeout == 0)
                context.getString(R.string.manage_device_default_user_timeout_off)
            else
                context.getString(
                        R.string.manage_device_default_user_timeout_on,
                        if (defaultUserTimeout < 1000 * 60)
                            TimeTextUtil.seconds(defaultUserTimeout / 1000, context)
                        else
                            TimeTextUtil.time(defaultUserTimeout, context)
                )
        })

        view.switchToDefaultUserButton.setOnClickListener {
            runAsync {
                ApplyActionUtil.applyAppLogicAction(
                        action = SignOutAtDeviceAction,
                        appLogic = auth.logic,
                        ignoreIfDeviceIsNotConfigured = true
                )
            }
        }
    }
}