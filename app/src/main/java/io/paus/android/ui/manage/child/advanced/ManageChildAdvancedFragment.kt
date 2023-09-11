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
package io.paus.android.ui.manage.child.advanced

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.paus.android.data.model.User
import io.paus.android.databinding.FragmentManageChildAdvancedBinding
import io.paus.android.livedata.liveDataFromFunction
import io.paus.android.livedata.map
import io.paus.android.livedata.mergeLiveData
import io.paus.android.logic.AppLogic
import io.paus.android.logic.DefaultAppLogic
import io.paus.android.ui.main.ActivityViewModel
import io.paus.android.ui.main.getActivityViewModel
import io.paus.android.ui.manage.child.advanced.duplicate.DuplicateChildDialogFragment
import io.paus.android.ui.manage.child.advanced.limituserviewing.LimitUserViewingView
import io.paus.android.ui.manage.child.advanced.managedisablepauss.ManageDisablePaussViewHelper
import io.paus.android.ui.manage.child.advanced.password.ManageChildPassword
import io.paus.android.ui.manage.child.advanced.selflimitadd.ChildSelfLimitAddView
import io.paus.android.ui.manage.child.advanced.timezone.UserTimezoneView

class ManageChildAdvancedFragment : Fragment() {
    companion object {
        private const val CHILD_ID = "childId"

        fun newInstance(childId: String) = ManageChildAdvancedFragment().apply {
            arguments = Bundle().apply { putString(CHILD_ID, childId) }
        }
    }

    private val childId: String get() = requireArguments().getString(CHILD_ID)!!
    private val auth: ActivityViewModel by lazy { getActivityViewModel(requireActivity()) }
    private val logic: AppLogic by lazy { DefaultAppLogic.with(requireContext()) }
    private val childEntry: LiveData<User?> by lazy { logic.database.user().getChildUserByIdLive(childId) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentManageChildAdvancedBinding.inflate(layoutInflater, container, false)

        run {
            // disable time pauses

            childEntry.observe(viewLifecycleOwner, Observer {
                child ->

                if (child != null) {
                    binding.disablePauss.handlers = ManageDisablePaussViewHelper.createHandlers(
                            childId = childId,
                            childTimezone = child.timeZone,
                            activity = requireActivity()
                    )
                }
            })

            mergeLiveData(childEntry, liveDataFromFunction { logic.timeApi.getCurrentTimeInMillis() }).map {
                (child, time) ->

                if (time == null || child == null) {
                    null
                } else {
                    ManageDisablePaussViewHelper.getDisabledUntilString(child, time, requireContext())
                }
            }.observe(viewLifecycleOwner, Observer {
                binding.disablePauss.disablePaussUntilString = it
            })
        }

        UserTimezoneView.bind(
                userEntry = childEntry,
                view = binding.userTimezone,
                fragmentManager = parentFragmentManager,
                lifecycleOwner = this,
                userId = childId,
                auth = auth
        )

        binding.renameChildButton.setOnClickListener {
            if (auth.requestAuthenticationOrReturnTrue()) {
                UpdateChildNameDialogFragment.newInstance(childId).show(parentFragmentManager)
            }
        }

        binding.duplicateChildButton.setOnClickListener {
            if (auth.requestAuthenticationOrReturnTrue()) {
                DuplicateChildDialogFragment.newInstance(childId).show(parentFragmentManager)
            }
        }

        binding.deleteUserButton.setOnClickListener {
            if (auth.requestAuthenticationOrReturnTrue()) {
                DeleteChildDialogFragment.newInstance(childId).show(parentFragmentManager)
            }
        }

        ManageChildPassword.bind(
                view = binding.password,
                childId = childId,
                childEntry = childEntry,
                lifecycleOwner = this,
                auth = auth,
                fragmentManager = parentFragmentManager
        )

        LimitUserViewingView.bind(
                view = binding.limitViewing,
                auth = auth,
                lifecycleOwner = viewLifecycleOwner,
                fragmentManager = parentFragmentManager,
                userEntry = childEntry,
                userId = childId
        )

        ChildSelfLimitAddView.bind(
                view = binding.selfLimitAdd,
                auth = auth,
                lifecycleOwner = viewLifecycleOwner,
                fragmentManager = parentFragmentManager,
                userEntry = childEntry,
                userId = childId
        )

        return binding.root
    }
}
