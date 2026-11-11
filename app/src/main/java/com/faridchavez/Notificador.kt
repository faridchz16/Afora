package com.faridchavez.afora

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object Notificador {

    private const val CANAL_ID = "aforo_alertas"

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                "Alertas de aforo",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos cuando cambia el nivel de aforo"
                enableVibration(false) // La vibración la controlamos nosotros según el nivel
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(canal)
        }
    }

    fun tienePermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun mostrarAforo(context: Context, porcentaje: Int, lugar: String = "Comedor") {
        val (titulo, texto, color) = when {
            porcentaje < 50 -> Triple(
                "🟢 Hay espacio en el $lugar",
                "Aforo al $porcentaje%. Hay pocas personas, ¡es buen momento para ir!",
                0xFF16A34A.toInt()
            )
            porcentaje < 85 -> Triple(
                "🟡 $lugar con aforo medio",
                "Aforo al $porcentaje%. Hay una cantidad moderada de personas.",
                0xFFD97706.toInt()
            )
            else -> Triple(
                "🔴 $lugar lleno",
                "Aforo al $porcentaje%. El lugar está lleno, te recomendamos esperar.",
                0xFFDC2626.toInt()
            )
        }

        if (tienePermiso(context)) {
            val notificacion = NotificationCompat.Builder(context, CANAL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
                .setColor(color)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(1, notificacion)
        }

        vibrar(context, porcentaje)
    }

    fun vibrar(context: Context, porcentaje: Int) {
        val vibrador: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val patron = when {
            porcentaje < 50 -> longArrayOf(0, 150)
            porcentaje < 85 -> longArrayOf(0, 250, 150, 250)
            else -> longArrayOf(0, 600, 200, 600, 200, 600)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrador.vibrate(VibrationEffect.createWaveform(patron, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrador.vibrate(patron, -1)
        }
    }
}