package com.catalabytes.ekopump.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.catalabytes.ekopump.data.local.dao.EvChargerDao
import com.catalabytes.ekopump.data.local.dao.GasolineraDao
import com.catalabytes.ekopump.data.local.dao.RefuelDao
import com.catalabytes.ekopump.data.local.entity.EvChargerConverters
import com.catalabytes.ekopump.data.local.entity.EvChargerEntity
import com.catalabytes.ekopump.data.local.entity.GasolineraEntity
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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS gasolineras_cache (
                id TEXT NOT NULL PRIMARY KEY,
                nombre TEXT NOT NULL,
                direccion TEXT NOT NULL,
                localidad TEXT NOT NULL,
                provincia TEXT NOT NULL,
                latitud REAL NOT NULL,
                longitud REAL NOT NULL,
                horario TEXT NOT NULL,
                gasolina95 REAL,
                gasolina98 REAL,
                gasoleoA REAL,
                gasoleoB REAL,
                gasoleoPremium REAL,
                glp REAL,
                gnc REAL,
                gnl REAL,
                cachedAt INTEGER NOT NULL
            )
        """.trimIndent())
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS ev_charger_cache (
                id INTEGER NOT NULL PRIMARY KEY,
                nombre TEXT NOT NULL,
                direccion TEXT,
                localidad TEXT,
                latitud REAL NOT NULL,
                longitud REAL NOT NULL,
                distanciaKm REAL,
                operador TEXT,
                totalPuntos INTEGER,
                esOperacional INTEGER NOT NULL,
                esPublico INTEGER NOT NULL,
                coste TEXT,
                conexiones TEXT NOT NULL,
                cachedAt INTEGER NOT NULL
            )
        """.trimIndent())
    }
}

@TypeConverters(EvChargerConverters::class)
@Database(
    entities = [RefuelEntity::class, GasolineraEntity::class, EvChargerEntity::class],
    version = 4,
    exportSchema = false
)
abstract class EkoPumpDatabase : RoomDatabase() {
    abstract fun refuelDao(): RefuelDao
    abstract fun gasolineraDao(): GasolineraDao
    abstract fun evChargerDao(): EvChargerDao
}
