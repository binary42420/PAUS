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
package io.paus.android.logic

import android.content.Context
import io.paus.android.data.RoomDatabase
import io.paus.android.integration.platform.ProtectionLevel
import io.paus.android.integration.platform.dummy.DummyIntegration
import io.paus.android.integration.time.DummyTimeApi
import io.paus.android.livedata.liveDataFromNonNullValue

class TestAppLogic(maximumProtectionLevel: ProtectionLevel, context: Context) {
    val platformIntegration = DummyIntegration(maximumProtectionLevel)
    val timeApi = DummyTimeApi(100)
    val database = RoomDatabase.createInMemoryInstance(context)

    val logic = AppLogic(
            platformIntegration = platformIntegration,
            timeApi = timeApi,
            database = database,
            context = context,
            isInitialized = liveDataFromNonNullValue(true)
    )
}
