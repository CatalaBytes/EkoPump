package com.catalabytes.ekopump.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.catalabytes.ekopump.MainActivity
import com.catalabytes.ekopump.R
import com.catalabytes.ekopump.data.prefs.PriceAlertPrefs
import com.catalabytes.ekopump.data.repository.Combustible
import com.catalabytes.ekopump.data.repository.GasolineraConDistancia
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceAlertChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val priceAlertPrefs: PriceAlertPrefs
) {
    fun checkAlerts(gasolineras: List<GasolineraConDistancia>) {
        val alerts = priceAlertPrefs.getPriceAlerts()
        if (alerts.isEmpty()) return

        val porId = gasolineras.associateBy { it.gasolinera.id }

        alerts.values.forEach { alert ->
            val item = porId[alert.gasolineraId] ?: return@forEach
            val combustible = try {
                Combustible.valueOf(alert.combustible)
            } catch (e: IllegalArgumentException) { return@forEach }

            val precioActual = combustible.precio(item.gasolinera) ?: return@forEach

            if (precioActual <= alert.precioUmbral) {
                dispararNotificacion(
                    id          = alert.gasolineraId.hashCode(),
                    nombre      = alert.nombre,
                    precioActual = precioActual,
                    umbral      = alert.precioUmbral
                )
            }
        }
    }

    private fun dispararNotificacion(id: Int, nombre: String, precioActual: Double, umbral: Double) {
        val channelId = "ekopump_precios"
        val manager   = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.createNotificationChannel(
            NotificationChannel(channelId, "Alertas de precio", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Avisos cuando el precio baja" }
        )

        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val precio = String.format(Locale.US, "%.3f", precioActual)
        val body   = "⛽ $nombre bajó a ${precio}€ — ¡Es tu momento!"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⛽ Alerta de precio")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(id, notification)
    }
}
