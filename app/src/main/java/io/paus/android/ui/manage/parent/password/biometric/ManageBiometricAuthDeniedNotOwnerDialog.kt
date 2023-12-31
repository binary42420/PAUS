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
package io.paus.android.ui.manage.parent.password.biometric

import android.os.Bundle
import io.paus.android.R

class ManageBiometricAuthDeniedNotOwnerDialog :
    ManageBiometricAuthDialog(ManageBiometricAuthDeniedNotOwnerDialog::class.java.simpleName) {
    override val titleText by lazy { getString(R.string.biometric_manage_not_owner_dialog_title) }
    override val messageText by lazy {
        getString(R.string.biometric_manage_not_owner_dialog_text, requireArguments().getString(ARG_USER_NAME))
    }
    override val positiveButtonText by lazy { getString(R.string.generic_logout) }
    override val negativeButtonText by lazy { getString(R.string.generic_cancel) }

    override fun onPositiveButtonClicked() {
        auth.logOut()
        super.onPositiveButtonClicked()
    }

    companion object {
        private const val ARG_USER_NAME = "userName"

        fun newInstance(userName: String) = ManageBiometricAuthDeniedNotOwnerDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_USER_NAME, userName)
            }
        }
    }
}
