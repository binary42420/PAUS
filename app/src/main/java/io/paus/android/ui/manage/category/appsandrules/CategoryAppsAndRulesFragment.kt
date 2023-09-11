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

package io.paus.android.ui.manage.category.appsandrules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import io.paus.android.R
import io.paus.android.async.Threads
import io.paus.android.data.Database
import io.paus.android.data.model.HintsToShow
import io.paus.android.data.model.pausRule
import io.paus.android.databinding.FragmentCategoryAppsAndRulesBinding
import io.paus.android.logic.DefaultAppLogic
import io.paus.android.sync.actions.AddCategoryAppsAction
import io.paus.android.sync.actions.CreatePausRuleAction
import io.paus.android.sync.actions.RemoveCategoryAppsAction
import io.paus.android.sync.actions.UpdatePausRuleAction
import io.paus.android.ui.main.ActivityViewModel
import io.paus.android.ui.main.getActivityViewModel
import io.paus.android.ui.manage.category.apps.add.AddAppsParams
import io.paus.android.ui.manage.category.apps.add.AddCategoryAppsFragment
import io.paus.android.ui.manage.category.paus_rules.edit.EditPausRuleDialogFragment
import io.paus.android.ui.manage.category.paus_rules.edit.EditPausRuleDialogFragmentListener
import io.paus.android.ui.manage.child.apps.assign.AssignAppCategoryDialogFragment

abstract class CategoryAppsAndRulesFragment: Fragment(), Handlers, EditPausRuleDialogFragmentListener {
    private val adapter = AppAndRuleAdapter().also { it.handlers = this }
    val model: AppsAndRulesModel by viewModels()
    val auth: ActivityViewModel by lazy { getActivityViewModel(requireActivity()) }
    val database: Database by lazy { DefaultAppLogic.with(requireContext()).database }
    abstract val childId: String
    abstract val categoryId: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentCategoryAppsAndRulesBinding.inflate(inflater, container, false)

        model.init(userId = childId, categoryId = categoryId)

        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter

        model.dateAndUsedTimes.observe(viewLifecycleOwner) { (date, usedTimes) ->
            adapter.date = date
            adapter.usedTimes = usedTimes
        }

        ItemTouchHelper(object: ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val index = viewHolder.adapterPosition
                val item = if (index == RecyclerView.NO_POSITION) null else adapter.items[index]

                if (item == AppAndRuleItem.RulesIntro) {
                    return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.END or ItemTouchHelper.START) or
                            makeFlag(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.END or ItemTouchHelper.START)
                } else {
                    return 0
                }
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = throw IllegalStateException()

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val database = database

                Threads.database.submit {
                    database.config().setHintsShownSync(HintsToShow.PAUS_TIME_RULE_INTRODUCTION)
                }
            }
        }).attachToRecyclerView(binding.recycler)

        return binding.root
    }

    fun setListContent(items: List<AppAndRuleItem>) { adapter.items = items }

    override fun notifyRuleCreated() {
        Snackbar.make(requireView(), R.string.category_paus_time_rules_snackbar_created, Snackbar.LENGTH_SHORT)
                .show()
    }

    override fun notifyRuleDeleted(oldRule: PausRule) {
        Snackbar.make(requireView(), R.string.category_paus_time_rules_snackbar_deleted, Snackbar.LENGTH_SHORT)
                .setAction(R.string.generic_undo) {
                    auth.tryDispatchParentAction(
                            CreatePausRuleAction(
                                    rule = oldRule
                            )
                    )
                }
                .show()
    }

    override fun notifyRuleUpdated(oldRule: PausRule, newRule: PausRule) {
        Snackbar.make(requireView(), R.string.category_paus_time_rules_snackbar_updated, Snackbar.LENGTH_SHORT)
            .also {
                if (auth.isParentAuthenticated()) it.setAction(R.string.generic_undo) {
                    auth.tryDispatchParentAction(
                        UpdatePausRuleAction(
                            ruleId = oldRule.id,
                            applyToExtraTimeUsage = oldRule.applyToExtraTimeUsage,
                            maximumTimeInMillis = oldRule.maximumTimeInMillis,
                            dayMask = oldRule.dayMask,
                            start = oldRule.startMinuteOfDay,
                            end = oldRule.endMinuteOfDay,
                            sessionDurationMilliseconds = oldRule.sessionDurationMilliseconds,
                            sessionPauseMilliseconds = oldRule.sessionPauseMilliseconds,
                            perDay = oldRule.perDay
                        )
                    )
                }
            }.show()
    }

    override fun onAppClicked(app: AppAndRuleItem.AppEntry) {
        if (auth.tryDispatchParentAction(
                        RemoveCategoryAppsAction(
                                categoryId = categoryId,
                                packageNames = listOf(app.specifier.encode())
                        )
                )) {
            Snackbar.make(requireView(), getString(R.string.category_apps_item_removed_toast, app.title), Snackbar.LENGTH_SHORT)
                    .setAction(R.string.generic_undo) {
                        auth.tryDispatchParentAction(
                                AddCategoryAppsAction(
                                        categoryId = categoryId,
                                        packageNames = listOf(app.specifier.encode())
                                )
                        )
                    }
                    .show()
        }
    }

    override fun onAppLongClicked(app: AppAndRuleItem.AppEntry): Boolean {
        return if (auth.requestAuthenticationOrReturnTrue()) {
            AssignAppCategoryDialogFragment.newInstance(
                    childId = childId,
                    appPackageName = app.specifier.encode()
            ).show(parentFragmentManager)

            true
        } else false
    }

    override fun onAddAppsClicked() {
        if (auth.requestAuthenticationOrReturnTrueAllowChild(childId = childId)) {
            AddCategoryAppsFragment.newInstance(AddAppsParams(
                    childId = childId,
                    categoryId = categoryId,
                    isSelfLimitAddingMode = !auth.isParentAuthenticated()
            )).show(parentFragmentManager)
        }
    }

    override fun onPausRuleClicked(rule: PausRule) {
        if (auth.isParentAuthenticated()) {
            EditPausRuleDialogFragment.newInstance(rule, false,  this).show(parentFragmentManager)
        } else if (auth.isParentOrChildAuthenticated(childId)) {
            EditPausRuleDialogFragment.newInstance(rule, true, this).show(parentFragmentManager)
        } else auth.requestAuthentication()
    }

    override fun onAddPausRuleClicked() {
        if (auth.requestAuthenticationOrReturnTrueAllowChild(childId = childId)) {
            EditPausRuleDialogFragment.newInstance(categoryId, true, this).show(parentFragmentManager)
        }
    }

    override fun onShowAllApps() { model.showAllApps() }
    override fun onShowAllRules() { model.showAllRules() }
}