package com.catalabytes.ekopump.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.catalabytes.ekopump.data.local.dao.RefuelDao
import com.catalabytes.ekopump.data.local.entity.RefuelEntity

@Database(
    entities = [RefuelEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EkoPumpDatabase : RoomDatabase() {
    abstract fun refuelDao(): RefuelDao
}
