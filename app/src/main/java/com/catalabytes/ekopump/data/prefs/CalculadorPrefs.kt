package com.catalabytes.ekopump.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.calculadorDataStore by preferencesDataStore("calculador_prefs")

@Singleton
class CalculadorPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_CONSUMO      = floatPreferencesKey("consumo_l100km")
        val KEY_LITROS       = floatPreferencesKey("litros_repostar")
        val KEY_VEHICLE_TYPE = stringPreferencesKey("vehicle_type")
        val KEY_ENERGY_TYPE  = stringPreferencesKey("energy_type")
    }

    val consumo: Flow<Float> = context.calculadorDataStore.data.map {
        it[KEY_CONSUMO] ?: 7f
    }

    val litros: Flow<Float> = context.calculadorDataStore.data.map {
        it[KEY_LITROS] ?: 40f
    }

    val vehicleType: Flow<String> = context.calculadorDataStore.data.map {
        it[KEY_VEHICLE_TYPE] ?: "TURISMO"
    }

    val energyType: Flow<String?> = context.calculadorDataStore.data.map {
        it[KEY_ENERGY_TYPE]
    }

    suspend fun setConsumo(v: Float) {
        context.calculadorDataStore.edit { it[KEY_CONSUMO] = v }
    }

    suspend fun setLitros(v: Float) {
        context.calculadorDataStore.edit { it[KEY_LITROS] = v }
    }

    suspend fun setVehicleType(name: String) {
        context.calculadorDataStore.edit { it[KEY_VEHICLE_TYPE] = name }
    }

    suspend fun setEnergyType(name: String?) {
        context.calculadorDataStore.edit {
            if (name == null) it.remove(KEY_ENERGY_TYPE) else it[KEY_ENERGY_TYPE] = name
        }
    }
}
