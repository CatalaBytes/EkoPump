package com.catalabytes.ekopump.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.catalabytes.ekopump.data.local.dao.RefuelDao
import com.catalabytes.ekopump.data.local.entity.RefuelEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE refuel_history ADD COLUMN odometroKm INTEGER")
        db.execSQL("ALTER TABLE refuel_history ADD COLUMN consumoRealL100 REAL")
        db.execSQL("ALTER TABLE refuel_history ADD COLUMN ahorroEstimadoEur REAL")
        db.execSQL("ALTER TABLE refuel_history ADD COLUMN gasolineraId TEXT")
        db.execSQL("ALTER TABLE refuel_history ADD COLUMN gasolineraName TEXT")
    }
}

@Database(
    entities = [RefuelEntity::class],
    version = 2,
    exportSchema = false
)
abstract class EkoPumpDatabase : RoomDatabase() {
    abstract fun refuelDao(): RefuelDao
}
