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
package io.paus.android.ui.overview.about

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import io.paus.android.async.Threads
import io.paus.android.data.Database
import io.paus.android.databinding.ResetShownHintsBinding

object ResetShownHints {
    fun bind(
            binding: ResetShownHintsBinding,
            database: Database,
            lifecycleOwner: LifecycleOwner
    ) {
        database.config().wereAnyHintsShown().observe(lifecycleOwner, Observer {
            binding.canReshowHints = it
        })

        binding.reshowHintsBtn.setOnClickListener {
            Threads.database.submit {
                database.config().resetShownHintsSync()
            }
        }
    }
}
