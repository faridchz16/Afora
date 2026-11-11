package com.faridchavez.afora

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.faridchavez.afora.ui.theme.AforaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Crea el canal y pide permiso de notificaciones (Android 13+)
        Notificador.crearCanal(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Notificador.tienePermiso(this)) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }

        setContent {
            AforaTheme {
                AforaApp()
            }
        }
    }
}