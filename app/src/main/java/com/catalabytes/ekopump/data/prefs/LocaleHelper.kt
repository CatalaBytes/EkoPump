package com.catalabytes.ekopump.data.prefs

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.DrawableRes
import com.catalabytes.ekopump.R
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
    val bandera: String,
    @DrawableRes val banderaRes: Int? = null
)

val IDIOMAS = listOf(
    Idioma("system", "Auto",    "\uD83C\uDF10"),
    Idioma("es",     "Español", "\uD83C\uDDEA\uD83C\uDDF8"),
    Idioma("ca",     "Català",  "",  R.drawable.flag_ca),
    Idioma("eu",     "Euskera", "",  R.drawable.flag_eu),
    Idioma("gl",     "Galego",  "",  R.drawable.flag_gl),
    Idioma("en",     "English", "\uD83C\uDDEC\uD83C\uDDE7")
)
