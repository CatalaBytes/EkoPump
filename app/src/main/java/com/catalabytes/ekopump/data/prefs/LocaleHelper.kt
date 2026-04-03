package com.catalabytes.ekopump.data.prefs

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    fun applyLanguage(context: Context, langCode: String): Context {
        if (langCode == "system") return context
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}

data class Idioma(
    val codigo: String,
    val nombre: String,
    val bandera: String
)

val IDIOMAS = listOf(
    Idioma("system", "Auto", "🌐"),
    Idioma("es", "Español", "🇪🇸"),
    Idioma("ca", "Català", "🟨🟥"),
    Idioma("eu", "Euskera", "🟩🟥"),
    Idioma("gl", "Galego", "🇪🇸"),
    Idioma("en", "English", "🇬🇧")
)
