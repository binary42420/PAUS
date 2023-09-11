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
package io.paus.android.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.paus.android.data.model.pausRule

@Dao
abstract class PausRuleDao {
    @Query("SELECT * FROM paus_time_rule WHERE category_id = :categoryId")
    abstract fun getPausRulesByCategory(categoryId: String): LiveData<List<PausRule>>

    @Query("SELECT * FROM paus_time_rule WHERE category_id = :categoryId")
    abstract suspend fun getPausRulesByCategoryCoroutine(categoryId: String): List<PausRule>

    @Query("SELECT * FROM paus_time_rule WHERE category_id IN (:categoryIds)")
    abstract fun getPausRulesByCategories(categoryIds: List<String>): LiveData<List<PausRule>>

    @Query("SELECT * FROM paus_time_rule WHERE category_id = :categoryId")
    abstract fun getPausRulesByCategorySync(categoryId: String): List<PausRule>

    @Query("DELETE FROM paus_time_rule WHERE category_id = :categoryId")
    abstract fun deletePausRulesByCategory(categoryId: String)

    @Insert
    abstract fun addPausRule(rule: PausRule)

    @Update
    abstract fun updatePausRule(rule: PausRule)

    @Query("SELECT * FROM paus_time_rule WHERE id = :PausRuleId")
    abstract fun getPausRuleByIdSync(PausRuleId: String): PausRule?

    @Query("SELECT * FROM paus_time_rule WHERE id = :PausRuleId")
    abstract fun getPausRuleByIdLive(PausRuleId: String): LiveData<PausRule?>

    @Query("DELETE FROM paus_time_rule WHERE id = :PausRuleId")
    abstract fun deletePausRuleByIdSync(PausRuleId: String)

    @Query("SELECT * FROM paus_time_rule LIMIT :pageSize OFFSET :offset")
    abstract fun getRulePageSync(offset: Int, pageSize: Int): List<PausRule>
}
