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
package io.paus.android.ui.manage.category.settings

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import io.paus.android.R
import io.paus.android.data.Database
import io.paus.android.databinding.ManageCategoryForUnassignedAppsBinding
import io.paus.android.livedata.ignoreUnchanged
import io.paus.android.livedata.map
import io.paus.android.sync.actions.SetCategoryForUnassignedApps
import io.paus.android.ui.help.HelpDialogFragment
import io.paus.android.ui.main.ActivityViewModel

object ManageCategoryForUnassignedApps {
    fun bind(
            binding: ManageCategoryForUnassignedAppsBinding,
            categoryId: String,
            childId: String,
            auth: ActivityViewModel,
            database: Database,
            lifecycleOwner: LifecycleOwner,
            fragmentManager: FragmentManager
    ) {
        binding.titleView.setOnClickListener {
            HelpDialogFragment.newInstance(
                    title = R.string.manage_category_for_unassigned_apps_title,
                    text = R.string.manage_category_for_unassigned_apps_intro
            ).show(fragmentManager)
        }

        val userEntry = database.user().getUserByIdLive(childId)
        val isCurrentlyChosen = userEntry.map { it?.categoryForNotAssignedApps == categoryId }.ignoreUnchanged()

        isCurrentlyChosen.observe(lifecycleOwner, Observer { binding.isThisCategoryUsed = it })

        binding.changeModeButton.setOnClickListener {
            val chosen = isCurrentlyChosen.value

            if (chosen == true) {
                auth.tryDispatchParentAction(
                        SetCategoryForUnassignedApps(
                                childId = childId,
                                categoryId = ""
                        )
                )
            } else if (chosen == false) {
                auth.tryDispatchParentAction(
                        SetCategoryForUnassignedApps(
                                childId = childId,
                                categoryId = categoryId
                        )
                )
            }
        }
    }
}