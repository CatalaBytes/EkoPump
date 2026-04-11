package com.catalabytes.ekopump.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.odometroDataStore by preferencesDataStore("odometro_prefs")

@Singleton
class OdometroPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_ULTIMO_ODOMETRO      = intPreferencesKey("ultimo_odometro_km")
        val KEY_FECHA_ULTIMO_REPOST  = longPreferencesKey("fecha_ultimo_repostaje")
    }

    val ultimoOdometroKm: Flow<Int> = context.odometroDataStore.data.map {
        it[KEY_ULTIMO_ODOMETRO] ?: 0
    }

    val fechaUltimoRepostaje: Flow<Long> = context.odometroDataStore.data.map {
        it[KEY_FECHA_ULTIMO_REPOST] ?: 0L
    }

    suspend fun guardar(odometroKm: Int, fechaMs: Long = System.currentTimeMillis()) {
        context.odometroDataStore.edit {
            it[KEY_ULTIMO_ODOMETRO]     = odometroKm
            it[KEY_FECHA_ULTIMO_REPOST] = fechaMs
        }
    }
}
