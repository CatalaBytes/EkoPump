package com.catalabytes.ekopump.di

import android.content.Context
import androidx.room.Room
import com.catalabytes.ekopump.data.local.dao.RefuelDao
import com.catalabytes.ekopump.data.local.db.EkoPumpDatabase
import com.catalabytes.ekopump.data.local.db.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EkoPumpDatabase =
        Room.databaseBuilder(
            context,
            EkoPumpDatabase::class.java,
            "ekopump_database"
        ).addMigrations(MIGRATION_1_2)
         .fallbackToDestructiveMigration()
         .build()

    @Provides
    fun provideRefuelDao(db: EkoPumpDatabase): RefuelDao = db.refuelDao()
}
