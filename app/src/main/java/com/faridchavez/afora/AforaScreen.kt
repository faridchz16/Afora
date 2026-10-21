package com.faridchavez.afora

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val VerdeFuerte = Color(0xFF16A34A)
val VerdeClaro = Color(0xFFDCFCE7)
val VerdeTexto = Color(0xFF15803D)
val AmarilloClaro = Color(0xFFFEF3C7)
val AmarilloTexto = Color(0xFF92400E)
val RojoClaro = Color(0xFFFEE2E2)
val RojoFuerte = Color(0xFFDC2626)
val GrisTexto = Color(0xFF4B5563)
val TextoOscuro = Color(0xFF111827)
val Fondo = Color(0xFFF1F5F3)

@Composable
fun Etiqueta(texto: String, fondo: Color, colorTexto: Color) {
    Text(
        text = texto,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = colorTexto,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(fondo)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun EtiquetaPreview() {
    Etiqueta("AFORO MEDIO", AmarilloClaro, AmarilloTexto)
}