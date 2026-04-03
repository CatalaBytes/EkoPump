package com.catalabytes.ekopump.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ekopump_prefs")

@Singleton
class LanguagePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val LANGUAGE_KEY = stringPreferencesKey("language")

    val language: Flow<String> = context.dataStore.data
        .map { it[LANGUAGE_KEY] ?: "system" }

    suspend fun setLanguage(lang: String) {
        // Guardar en DataStore
        context.dataStore.edit { it[LANGUAGE_KEY] = lang }
        // Guardar también en SharedPreferences para attachBaseContext
        context.getSharedPreferences("ekopump_lang", Context.MODE_PRIVATE)
            .edit()
            .putString("language", lang)
            .apply()
    }
}
