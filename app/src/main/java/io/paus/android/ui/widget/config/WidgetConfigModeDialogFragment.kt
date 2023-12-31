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
package io.paus.android.ui.widget.config

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import io.paus.android.R

class WidgetConfigModeDialogFragment: DialogFragment() {
    companion object {
        private const val STATE_SELECTION = "selection"

        const val DIALOG_TAG = "WidgetConfigModeDialogFragment"
    }

    private val model: WidgetConfigModel by activityViewModels()
    private var selection = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model.state.value?.also {
            if (it is WidgetConfigModel.State.ShowModeSelection) {
                selection = if (it.selectedFilterCategories.isEmpty()) 0 else 1
            }
        }

        savedInstanceState?.also { selection = it.getInt(STATE_SELECTION) }

        model.state.observe(this) {
            if (!(it is WidgetConfigModel.State.ShowModeSelection)) dismissAllowingStateLoss()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(STATE_SELECTION, selection)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = AlertDialog.Builder(requireContext(), theme)
        .setSingleChoiceItems(
            arrayOf(
                getString(R.string.widget_config_mode_all),
                getString(R.string.widget_config_mode_filter)
            ),
            selection
        ) { _, selectedItemIndex -> selection = selectedItemIndex }
        .setPositiveButton(R.string.wiazrd_next) { _, _ ->
            if (selection == 0) model.selectModeAll()
            else model.selectModeFilter()
        }
        .create()

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        model.userCancel()
    }
}