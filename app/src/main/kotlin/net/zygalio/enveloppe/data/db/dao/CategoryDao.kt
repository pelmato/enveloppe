package net.zygalio.enveloppe.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.zygalio.enveloppe.data.db.entity.CategoryEntity

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE envelopeId = :envelopeId")
    fun getByEnvelopeFlow(envelopeId: Long): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE envelopeId = :envelopeId")
    suspend fun getByEnvelope(envelopeId: Long): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM expenses WHERE categoryId = :categoryId")
    suspend fun expenseCount(categoryId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE envelopeId = :envelopeId")
    suspend fun deleteByEnvelope(envelopeId: Long)
}
