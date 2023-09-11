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
package io.paus.android.ui.manage.parent.u2fkey.remove

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import io.paus.android.R
import io.paus.android.extensions.showSafe

class U2FRequiresPasswordForRemovalDialogFragment: DialogFragment() {
    companion object {
        private const val DIALOG_TAG = "U2FRequiresPasswordForRemovalDialogFragment"

        fun newInstance() = U2FRequiresPasswordForRemovalDialogFragment()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = AlertDialog.Builder(requireContext(), theme)
        .setMessage(R.string.manage_parent_u2f_remove_key_requires_password)
        .setPositiveButton(R.string.generic_ok, null)
        .create()

    fun show(fragmentManager: FragmentManager) = showSafe(fragmentManager, DIALOG_TAG)
}