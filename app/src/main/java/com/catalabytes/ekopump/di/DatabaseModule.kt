package com.catalabytes.ekopump.di

import android.content.Context
import androidx.room.Room
import com.catalabytes.ekopump.data.local.dao.EvChargerDao
import com.catalabytes.ekopump.data.local.dao.GasolineraDao
import com.catalabytes.ekopump.data.local.dao.RefuelDao
import com.catalabytes.ekopump.data.local.db.EkoPumpDatabase
import com.catalabytes.ekopump.data.local.db.MIGRATION_1_2
import com.catalabytes.ekopump.data.local.db.MIGRATION_2_3
import com.catalabytes.ekopump.data.local.db.MIGRATION_3_4
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
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
         .fallbackToDestructiveMigration()
         .build()

    @Provides
    fun provideRefuelDao(db: EkoPumpDatabase): RefuelDao = db.refuelDao()

    @Provides
    fun provideGasolineraDao(db: EkoPumpDatabase): GasolineraDao = db.gasolineraDao()

    @Provides
    fun provideEvChargerDao(db: EkoPumpDatabase): EvChargerDao = db.evChargerDao()
}
