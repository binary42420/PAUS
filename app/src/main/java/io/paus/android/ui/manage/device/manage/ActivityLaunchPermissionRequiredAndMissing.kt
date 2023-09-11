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
package io.paus.android.ui.manage.device.manage

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.paus.android.R
import io.paus.android.data.model.Device
import io.paus.android.data.model.User
import io.paus.android.data.model.UserType
import io.paus.android.databinding.MissingPermissionViewBinding
import io.paus.android.livedata.mergeLiveData

object ActivityLaunchPermissionRequiredAndMissing {
    fun bind(
            view: MissingPermissionViewBinding,
            user: LiveData<User?>,
            device: LiveData<Device?>,
            lifecycleOwner: LifecycleOwner
    ) {
        view.title = view.root.context.getString(R.string.activity_launch_permission_required_and_missing_title)

        mergeLiveData(user, device).observe(lifecycleOwner, Observer { (user, device) ->
            view.showMessage = user?.type == UserType.Child && device?.missingPermissionAtQOrLater ?: false
        })
    }
}
