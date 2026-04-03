package com.catalabytes.ekopump

import android.app.Application
import android.content.Context
import com.catalabytes.ekopump.data.prefs.LocaleHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EkoPumpApplication : Application() {
    override fun attachBaseContext(base: Context) {
        val prefs = base.getSharedPreferences("ekopump_lang", Context.MODE_PRIVATE)
        val langCode = prefs.getString("language", "system") ?: "system"
        super.attachBaseContext(LocaleHelper.applyLanguage(base, langCode))
    }
}
