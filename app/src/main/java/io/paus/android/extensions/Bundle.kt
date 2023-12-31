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
package io.paus.android.extensions

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle

inline fun <reified T> Bundle.getParcelableCompat(key: String): T? =
    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) this.getParcelable(key, T::class.java)
    else this.getParcelable(key) as T?