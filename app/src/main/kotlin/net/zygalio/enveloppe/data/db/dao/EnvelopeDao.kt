package net.zygalio.enveloppe.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.zygalio.enveloppe.data.db.entity.EnvelopeEntity
import net.zygalio.enveloppe.data.db.model.EnvelopeSummaryDb

@Dao
interface EnvelopeDao {

    @Query("""
        SELECT e.id, e.name, e.budget, e.endDate,
               COALESCE(SUM(ex.amount), 0.0) AS totalConsumed,
               COALESCE(SUM(CASE WHEN ex.dateTime >= :todayStart THEN ex.amount ELSE 0 END), 0.0) AS todayConsumed,
               MAX(ex.dateTime) AS lastExpenseDate
        FROM envelopes e
        LEFT JOIN expenses ex ON e.id = ex.envelopeId
        GROUP BY e.id
        ORDER BY COALESCE(MAX(ex.dateTime), 0) DESC
    """)
    fun getSummariesFlow(todayStart: Long): Flow<List<EnvelopeSummaryDb>>

    @Query("SELECT * FROM envelopes WHERE id = :id")
    suspend fun getById(id: Long): EnvelopeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(envelope: EnvelopeEntity): Long

    @Update
    suspend fun update(envelope: EnvelopeEntity)

    @Delete
    suspend fun delete(envelope: EnvelopeEntity)
}
