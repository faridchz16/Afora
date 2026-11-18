package com.faridchavez.afora

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

data class FranjaHoraria(val hora: Int, val porcentaje: Int) {
    val texto: String get() = "${formatoHora(hora)} – ${formatoHora(hora + 1)}"
}

fun formatoHora(hora: Int): String {
    val h = if (hora % 12 == 0) 12 else hora % 12
    val sufijo = if (hora % 24 < 12) "a.m." else "p.m."
    return "$h:00 $sufijo"
}

fun franjasDelDia(): List<FranjaHoraria> =
    datosAforoDia.mapIndexed { i, valor -> FranjaHoraria(8 + i, valor.toInt()) }

fun mejorYPeorHora(horaActual: Int): Triple<FranjaHoraria, FranjaHoraria?, Boolean> {
    val franjas = franjasDelDia()
    val restantes = franjas.filter { it.hora >= horaActual }
    val esManana = restantes.isEmpty()
    val candidatas = if (esManana) franjas else restantes
    val mejor = candidatas.minBy { it.porcentaje }
    val peor = candidatas.maxBy { it.porcentaje }.takeIf { it != mejor }
    return Triple(mejor, peor, esManana)
}

@Composable
fun TarjetaMejorHora() {
    val horaActual = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val (mejor, peor, esManana) = remember(horaActual) { mejorYPeorHora(horaActual) }

    TarjetaBlanca(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconoGrande(Icons.Filled.Schedule, VerdeFuerte, VerdeClaro, "Mejor hora para ir", 72, 44)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (esManana) "Mejor hora para ir mañana" else "Mejor hora para ir hoy",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )
                Text("Según el historial del comedor", fontSize = 16.sp, color = GrisTexto)
            }
        }

        Spacer(Modifier.height(16.dp))
        FilaHora(Icons.Filled.ThumbUp, "Ve a esta hora", mejor)

        if (peor != null) {
            Spacer(Modifier.height(10.dp))
            FilaHora(Icons.Filled.Block, "Evita esta hora", peor)
        }
    }
}

@Composable
fun FilaHora(icono: ImageVector, titulo: String, franja: FranjaHoraria) {
    val (_, fondo, texto) = coloresNivel(franja.porcentaje)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(fondo)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, titulo, tint = texto, modifier = Modifier.size(34.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(titulo, fontSize = 16.sp, color = GrisTexto)
            Text(franja.texto, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = texto)
        }
        Text("${franja.porcentaje}%", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = texto)
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun MejorHoraPreview() {
    TarjetaMejorHora()
}