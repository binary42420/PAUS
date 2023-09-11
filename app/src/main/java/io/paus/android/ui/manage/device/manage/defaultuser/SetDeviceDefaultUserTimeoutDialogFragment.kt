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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.paus.android.R
import io.paus.android.data.model.Device
import io.paus.android.data.model.UserType
import io.paus.android.databinding.BottomSheetSelectionListBinding
import io.paus.android.extensions.showSafe
import io.paus.android.logic.DefaultAppLogic
import io.paus.android.sync.actions.SetDeviceDefaultUserTimeoutAction
import io.paus.android.ui.main.ActivityViewModel
import io.paus.android.ui.main.getActivityViewModel
import io.paus.android.util.TimeTextUtil

class SetDeviceDefaultUserTimeoutDialogFragment: BottomSheetDialogFragment() {
    companion object {
        private const val EXTRA_DEVICE_ID = "deviceId"
        private const val DIALOG_TAG = "sddutdf"
        private val OPTIONS = listOf(
                0,
                1000 * 5,
                1000 * 60,
                1000 * 60 * 5,
                1000 * 60 * 15,
                1000 * 60 * 30,
                1000 * 60 * 60
        )

        fun newInstance(deviceId: String) = SetDeviceDefaultUserTimeoutDialogFragment().apply {
            arguments = Bundle().apply {
                putString(EXTRA_DEVICE_ID, deviceId)
            }
        }
    }

    val deviceId: String by lazy { arguments!!.getString(EXTRA_DEVICE_ID)!! }
    val deviceEntry: LiveData<Device?> by lazy {
        DefaultAppLogic.with(context!!).database.device().getDeviceById(deviceId)
    }
    val auth: ActivityViewModel by lazy { getActivityViewModel(activity!!) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth.authenticatedUser.observe(this, Observer {
            if (it?.type != UserType.Parent) {
                dismissAllowingStateLoss()
            }
        })

        deviceEntry.observe(this, Observer {
            if (it == null) {
                dismissAllowingStateLoss()
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = BottomSheetSelectionListBinding.inflate(inflater, container, false)
        binding.title = getString(R.string.manage_device_default_user_timeout_dialog_title)
        val list = binding.list

        deviceEntry.observe(this, Observer { device ->
            val timeout = device?.defaultUserTimeout ?: 0

            fun buildRow(): CheckedTextView = LayoutInflater.from(context!!).inflate(
                    android.R.layout.simple_list_item_single_choice,
                    list,
                    false
            ) as CheckedTextView

            list.removeAllViews()

            OPTIONS.forEach { option ->
                buildRow().let { row ->
                    row.text = if (option == 0)
                        getString(R.string.manage_device_default_user_timeout_dialog_disable)
                    else if (option < 1000 * 60)
                        TimeTextUtil.seconds(option / 1000, context!!)
                    else
                        TimeTextUtil.time(option, context!!)

                    row.isChecked = option == timeout
                    row.setOnClickListener {
                        auth.tryDispatchParentAction(SetDeviceDefaultUserTimeoutAction(
                                deviceId = deviceId,
                                timeout = option
                        ))

                        dismiss()
                    }

                    list.addView(row)
                }
            }
        })

        return binding.root
    }

    fun show(fragmentManager: FragmentManager) = showSafe(fragmentManager, DIALOG_TAG)
}