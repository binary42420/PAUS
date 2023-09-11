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
package io.paus.android.ui.manage.parent.key

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import io.paus.android.R
import io.paus.android.crypto.Curve25519
import io.paus.android.databinding.ManageUserKeyViewBinding
import io.paus.android.sync.actions.ResetUserKeyAction
import io.paus.android.ui.help.HelpDialogFragment
import io.paus.android.ui.main.ActivityViewModel

object ManageUserKeyView {
    fun bind(
            view: ManageUserKeyViewBinding,
            lifecycleOwner: LifecycleOwner,
            userId: String,
            auth: ActivityViewModel,
            fragmentManager: FragmentManager
    ) {
        val userKey = auth.logic.database.userKey().getUserKeyByUserIdLive(userId)

        userKey.observe(lifecycleOwner, Observer {
            view.isLoaded = true
            view.keyId = it?.publicKey?.let { Curve25519.getPublicKeyId(it) }
        })

        view.addKeyButton.setOnClickListener {
            if (auth.requestAuthenticationOrReturnTrue()) {
                if (auth.authenticatedUser.value?.id == userId) {
                    AddUserKeyDialogFragment.newInstance(userId).show(fragmentManager)
                } else {
                    ParentKeyWrongUserDialogFragment.newInstance().show(fragmentManager)
                }
            }
        }

        view.removeKeyButton.setOnClickListener {
            if (auth.requestAuthenticationOrReturnTrue()) {
                if (auth.authenticatedUser.value?.id == userId) {
                    auth.tryDispatchParentAction(ResetUserKeyAction(userId))
                } else {
                    ParentKeyWrongUserDialogFragment.newInstance().show(fragmentManager)
                }
            }
        }

        view.titleView.setOnClickListener {
            HelpDialogFragment.newInstance(
                    title = R.string.manage_user_key_title,
                    text = R.string.manage_user_key_info
            ).show(fragmentManager)
        }
    }
}