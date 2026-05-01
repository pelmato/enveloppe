package net.zygalio.enveloppe.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import net.zygalio.enveloppe.data.db.dao.CategoryDao
import net.zygalio.enveloppe.data.db.dao.EnvelopeDao
import net.zygalio.enveloppe.data.db.dao.ExpenseDao
import net.zygalio.enveloppe.data.db.entity.CategoryEntity
import net.zygalio.enveloppe.data.db.entity.EnvelopeEntity
import net.zygalio.enveloppe.data.db.entity.ExpenseEntity

@Database(
    entities = [EnvelopeEntity::class, CategoryEntity::class, ExpenseEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun envelopeDao(): EnvelopeDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
}
