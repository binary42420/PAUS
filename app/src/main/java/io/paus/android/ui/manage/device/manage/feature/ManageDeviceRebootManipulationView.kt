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
package io.paus.android.ui.manage.device.manage.feature

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.paus.android.R
import io.paus.android.data.model.Device
import io.paus.android.databinding.ManageDeviceRebootManipulationViewBinding
import io.paus.android.sync.actions.SetConsiderRebootManipulationAction
import io.paus.android.ui.help.HelpDialogFragment
import io.paus.android.ui.main.ActivityViewModel

object ManageDeviceRebootManipulationView {
    fun bind(
            view: ManageDeviceRebootManipulationViewBinding,
            deviceEntry: LiveData<Device?>,
            lifecycleOwner: LifecycleOwner,
            auth: ActivityViewModel,
            fragmentManager: FragmentManager
    ) {
        view.titleView.setOnClickListener {
            HelpDialogFragment.newInstance(
                    title = R.string.manage_device_reboot_manipulation_title,
                    text = R.string.manage_device_reboot_manipulation_text
            ).show(fragmentManager)
        }

        deviceEntry.observe(lifecycleOwner, Observer { device ->
            val checked = device?.considerRebootManipulation ?: false

            view.checkbox.setOnCheckedChangeListener { _, _ ->  }
            view.checkbox.isChecked = checked
            view.checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked != checked) {
                    if (
                            device != null &&
                            !auth.tryDispatchParentAction(
                                    SetConsiderRebootManipulationAction(
                                            deviceId = device.id,
                                            considerRebootManipulation = isChecked
                                    )
                            )
                    ) {
                        view.checkbox.isChecked = checked
                    }
                }
            }
        })
    }
}