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
package io.paus.android.ui.diagnose

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import io.paus.android.R
import io.paus.android.async.Threads
import io.paus.android.coroutines.runAsync
import io.paus.android.data.model.UserType
import io.paus.android.extensions.showSafe
import io.paus.android.integration.platform.ProtectionLevel
import io.paus.android.logic.AppLogic
import io.paus.android.logic.DefaultAppLogic
import io.paus.android.ui.main.ActivityViewModel
import io.paus.android.ui.main.getActivityViewModel
import io.paus.android.ui.util.EditTextBottomSheetDialog

class DiagnoseOrganizationNameDialogFragment: EditTextBottomSheetDialog() {
    companion object {
        private const val DIALOG_TAG = "DiagnoseOrganizationNameDialogFragment"

        fun newInstance() = DiagnoseOrganizationNameDialogFragment()
    }

    private val appLogic: AppLogic by lazy { DefaultAppLogic.with(requireContext()) }
    private val auth: ActivityViewModel by lazy { getActivityViewModel(requireActivity()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth.authenticatedUser.observe(this) {
            if (it?.type != UserType.Parent) {
                dismissAllowingStateLoss()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.title = getString(R.string.diagnose_don_title)

        if (appLogic.platformIntegration.getCurrentProtectionLevel() != ProtectionLevel.DeviceOwner) {
            Toast.makeText(
                requireContext(),
                R.string.diagnose_don_no_owner_toast,
                Toast.LENGTH_SHORT
            ).show()

            dismissAllowingStateLoss()
        } else if (!appLogic.platformIntegration.canSetOrganizationName()) {
            Toast.makeText(requireContext(), R.string.diagnose_don_not_supported_toast, Toast.LENGTH_SHORT).show()

            dismissAllowingStateLoss()
        } else {
            if (savedInstanceState == null) {
                runAsync {
                    val name = appLogic.database.config().getCustomOrganizationName()

                    binding.editText.setText(name)
                }
            }
        }
    }

    override fun go() {
        val value = binding.editText.text.toString()

        if (appLogic.platformIntegration.setOrganizationName(value)) {
            val database = appLogic.database

            Threads.database.execute { database.config().setCustomOrganizationName(value) }
        } else {
            Toast.makeText(requireContext(), R.string.diagnose_don_not_supported_toast, Toast.LENGTH_SHORT).show()
        }

        dismissAllowingStateLoss()
    }

    fun show(fragmentManager: FragmentManager) = showSafe(fragmentManager, DIALOG_TAG)
}