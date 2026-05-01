package net.zygalio.enveloppe.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.zygalio.enveloppe.data.db.entity.ExpenseEntity

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE envelopeId = :envelopeId ORDER BY dateTime DESC")
    fun getByEnvelopeFlow(envelopeId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): ExpenseEntity?

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE envelopeId = :envelopeId")
    suspend fun totalByEnvelope(envelopeId: Long): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0)
        FROM expenses
        WHERE envelopeId = :envelopeId AND dateTime >= :todayStart
    """)
    suspend fun todayTotalByEnvelope(envelopeId: Long, todayStart: Long): Double

    @Query("""
        SELECT categoryId, COALESCE(SUM(amount), 0.0) AS total
        FROM expenses
        WHERE envelopeId = :envelopeId AND categoryId IS NOT NULL
        GROUP BY categoryId
    """)
    suspend fun totalsByCategory(envelopeId: Long): List<CategoryTotal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    data class CategoryTotal(val categoryId: Long, val total: Double)
}
