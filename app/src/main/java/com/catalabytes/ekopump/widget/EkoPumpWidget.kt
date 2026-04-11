package com.catalabytes.ekopump.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.catalabytes.ekopump.MainActivity
import com.catalabytes.ekopump.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EkoPumpWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    companion object {
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_ekopump)

            // Tap en el widget abre la app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_price, pendingIntent)

            // Carga datos guardados en SharedPreferences
            val prefs = context.getSharedPreferences("ekopump_widget", Context.MODE_PRIVATE)
            val price = prefs.getString("cheapest_price", "---") ?: "---"
            val station = prefs.getString("cheapest_station", "Buscando...") ?: "Buscando..."
            val distance = prefs.getString("cheapest_distance", "") ?: ""
            val saving = prefs.getString("saving_amount", "") ?: ""
            val updated = prefs.getString("last_updated", "Actualizando...") ?: "Actualizando..."
            val brentDir = prefs.getString("brent_direction", "stable") ?: "stable"

            val voiceText = when (brentDir) {
                "down" -> "Brent baja \u00b7 buen momento"
                "up"   -> "Brent sube \u00b7 considera esperar"
                else   -> "Tu mejor opci\u00f3n ahora"
            }

            views.setTextViewText(R.id.widget_price, "$price \u20ac/L")
            views.setTextViewText(R.id.widget_station,
                if (distance.isNotEmpty()) "$station \u00b7 $distance km" else station)
            views.setTextViewText(R.id.widget_saving,
                if (saving.isNotEmpty()) "Ahorras $saving\u20ac vs tu habitual" else "")
            views.setTextViewText(R.id.widget_voice, voiceText)
            views.setTextViewText(R.id.widget_updated, "Actualizado $updated")

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
