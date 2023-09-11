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
package io.paus.android.ui.overview.uninstall

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import io.paus.android.R
import io.paus.android.databinding.FragmentUninstallBinding
import io.paus.android.livedata.liveDataFromNonNullValue
import io.paus.android.livedata.liveDataFromNullableValue
import io.paus.android.ui.backdoor.BackdoorDialogFragment
import io.paus.android.ui.main.ActivityViewModel
import io.paus.android.ui.main.ActivityViewModelHolder
import io.paus.android.ui.main.AuthenticationFab
import io.paus.android.ui.main.FragmentWithCustomTitle

class UninstallFragment : Fragment(), FragmentWithCustomTitle {
    companion object {
        private const val STATUS_SHOW_BACKDOOR_BUTTON = "show_backdoor_button"
    }

    private val activity: ActivityViewModelHolder by lazy { getActivity() as ActivityViewModelHolder }
    private val auth: ActivityViewModel by lazy { activity.getActivityViewModel() }
    private var showBackdoorButton = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showBackdoorButton = savedInstanceState?.getBoolean(STATUS_SHOW_BACKDOOR_BUTTON) ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentUninstallBinding.inflate(inflater, container, false)

        binding.uninstall.isEnabled = binding.checkConfirm.isChecked
        binding.checkConfirm.setOnCheckedChangeListener { _, isChecked -> binding.uninstall.isEnabled = isChecked }

        binding.uninstall.setOnClickListener {
            if (auth.requestAuthenticationOrReturnTrue()) {
                auth.logic.appSetupLogic.resetAppCompletely()
            } else {
                showBackdoorButton = true
                binding.showBackdoorButton = true
            }
        }

        binding.backdoorButton.setOnClickListener {
            BackdoorDialogFragment().show(parentFragmentManager)
        }

        binding.showBackdoorButton = showBackdoorButton

        AuthenticationFab.manageAuthenticationFab(
                fab = binding.fab,
                fragment = this,
                shouldHighlight = activity.getActivityViewModel().shouldHighlightAuthenticationButton,
                authenticatedUser = activity.getActivityViewModel().authenticatedUser,
                doesSupportAuth = liveDataFromNonNullValue(true)
        )

        binding.fab.setOnClickListener { activity.showAuthenticationScreen() }

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(STATUS_SHOW_BACKDOOR_BUTTON, showBackdoorButton)
    }

    override fun getCustomTitle(): LiveData<String?> = liveDataFromNullableValue(getString(R.string.uninstall_reset_title))
}
