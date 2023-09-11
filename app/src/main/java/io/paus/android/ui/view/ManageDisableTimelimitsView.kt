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
package io.paus.android.ui.view

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import io.paus.android.R
import io.paus.android.databinding.ViewManageDisablePaussBinding
import kotlin.properties.Delegates

class ManageDisablePaussView(context: Context, attributeSet: AttributeSet): FrameLayout(context, attributeSet) {
    private val binding = ViewManageDisablePaussBinding.inflate(LayoutInflater.from(context), this, false)

    init {
        addView(binding.root)
    }

    var disablePaussUntilString: String? by Delegates.observable(null as String?) {
        _, _, value ->

        if (TextUtils.isEmpty(value)) {
            binding.disabledUntilString = null
        } else {
            binding.disabledUntilString = resources.getString(R.string.manage_disable_time_pauses_info_enabled, value)
        }
    }

    var handlers: ManageDisablePaussViewHandlers? by Delegates.observable(null as ManageDisablePaussViewHandlers?) {
        _, _, value -> binding.handlers = value
    }

    init {
        binding.titleView.setOnClickListener { handlers?.showDisablePaussHelp() }
    }
}

interface ManageDisablePaussViewHandlers {
    fun disablePaussUntilSelectedTimeOfToday()
    fun disablePaussUntilSelectedDate()
    fun disablePaussForDuration(duration: Long)
    fun disablePaussForToday()
    fun enablePauss()
    fun showDisablePaussHelp()
}
