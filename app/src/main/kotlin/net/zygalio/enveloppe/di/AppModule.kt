package net.zygalio.enveloppe.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zygalio.enveloppe.data.db.AppDatabase
import net.zygalio.enveloppe.data.db.dao.CategoryDao
import net.zygalio.enveloppe.data.db.dao.EnvelopeDao
import net.zygalio.enveloppe.data.db.dao.ExpenseDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "enveloppe.db").build()

    @Provides
    fun provideEnvelopeDao(db: AppDatabase): EnvelopeDao = db.envelopeDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideExpenseDao(db: AppDatabase): ExpenseDao = db.expenseDao()
}
