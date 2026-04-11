package com.catalabytes.ekopump.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.catalabytes.ekopump.MainActivity
import com.catalabytes.ekopump.R
import com.catalabytes.ekopump.data.prefs.CalculadorPrefs
import com.catalabytes.ekopump.domain.ConsumoCalculator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutonomiaChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val calculadorPrefs: CalculadorPrefs,
    private val consumoCalculator: ConsumoCalculator
) {
    private val channelId = "ekopump_autonomia"

    suspend fun check(litrosActuales: Float, consumoL100: Float) {
        val umbral = calculadorPrefs.umbralAutonomiaKm.first()
        val autonomia = consumoCalculator.calcularAutonomiaRestante(litrosActuales, consumoL100)
        if (autonomia < umbral) {
            dispararNotificacion(autonomia)
        }
    }

    private fun dispararNotificacion(autonomiaKm: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.createNotificationChannel(
            NotificationChannel(channelId, "Autonomia combustible", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Avisos de autonomia baja" }
        )

        val intent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context, 9001, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val body = "Busca la gasolinera mas barata antes de quedarte sin combustible"
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Autonomia baja — ~$autonomiaKm km restantes")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(9001, notification)
    }
}
