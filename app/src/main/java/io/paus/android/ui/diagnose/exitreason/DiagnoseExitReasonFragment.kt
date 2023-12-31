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
package io.paus.android.ui.diagnose.exitreason

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import io.paus.android.R
import io.paus.android.databinding.DiagnoseExitReasonFragmentBinding
import io.paus.android.livedata.liveDataFromNullableValue
import io.paus.android.logic.DefaultAppLogic
import io.paus.android.ui.main.FragmentWithCustomTitle

class DiagnoseExitReasonFragment: Fragment(), FragmentWithCustomTitle {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DiagnoseExitReasonFragmentBinding.inflate(inflater, container, false)
        val data = DefaultAppLogic.with(requireContext()).platformIntegration.getExitLog(0)
        val recycler = binding.recycler
        val adapter = DiagnoseExitReasonAdapter()

        adapter.content = data
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter
        binding.isEmpty = data.isEmpty()

        return binding.root
    }

    override fun getCustomTitle(): LiveData<String?> = liveDataFromNullableValue("${getString(R.string.diagnose_er_title)} < ${getString(
        R.string.about_diagnose_title)} < ${getString(R.string.main_tab_overview)}")
}