package com.faridchavez.afora

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.filled.LooksTwo
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =====================================================
// MAPA DEL COMEDOR POR ZONAS
// =====================================================

data class Zona(
    val nombre: String,
    val icono: ImageVector,
    val ocupados: Int,
    val capacidad: Int
) {
    val porcentaje: Int get() = if (capacidad == 0) 0 else ocupados * 100 / capacidad
    val libres: Int get() = capacidad - ocupados
}

// Reparte a las personas del comedor entre sus dos pisos (100 sillas en total).
// El primer piso se llena primero, como pasa en la realidad.
fun zonasComedor(aforo: Int): List<Zona> {
    fun ocupar(peso: Float, capacidad: Int) = (aforo * peso).toInt().coerceIn(0, capacidad)
    return listOf(
        Zona("Segundo piso", Icons.Filled.LooksTwo, ocupar(0.35f, 40), 40),
        Zona("Primer piso", Icons.Filled.LooksOne, ocupar(0.65f, 60), 60)
    )
}

// Colores según el nivel: borde fuerte, fondo claro y color del texto
fun coloresNivel(porcentaje: Int): Triple<Color, Color, Color> = when (indiceNivel(porcentaje)) {
    0 -> Triple(VerdeFuerte, VerdeClaro, VerdeTexto)
    1 -> Triple(Amarillo, AmarilloClaro, AmarilloTexto)
    else -> Triple(RojoFuerte, RojoClaro, RojoFuerte)
}

@Composable
fun PantallaMapa(aforoComedor: Int) {
    val zonas = zonasComedor(aforoComedor)
    var seleccionada by rememberSaveable { mutableStateOf(zonas[1].nombre) }
    val elegida = zonas.first { it.nombre == seleccionada }

    // Si la zona elegida está llena, recomendamos la que tenga más sillas libres
    val alternativa = if (indiceNivel(elegida.porcentaje) == 2) {
        zonas.filter { it != elegida && it.libres > 0 }.maxByOrNull { it.libres }
    } else null

    // Fila para servirse: depende de cuánta gente hay en el comedor
    val fila = aforoComedor / 6
    val minutos = fila / 2 + 1
    val nivelFila = (fila * 100 / 16).coerceAtMost(100)

    ContenidoPantalla {
        LineaEstado("Actualización en vivo", "Toca un piso")
        LeyendaCompacta()

        TarjetaBlanca(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Comedor", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
                Text("100 sillas", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = GrisTexto)
            }
            Spacer(Modifier.height(12.dp))

            // Arriba: segundo piso (ancho completo)
            ZonaMapa(zonas[0], seleccionada == zonas[0].nombre, Modifier.fillMaxWidth()) { seleccionada = it }

            Escaleras()

            // Abajo: primer piso y la fila para servirse
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ZonaMapa(zonas[1], seleccionada == zonas[1].nombre, Modifier.weight(1.3f)) { seleccionada = it }
                BloqueFila(fila, minutos, nivelFila, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))

            Entrada()
        }

        DetalleZona(elegida, alternativa) { seleccionada = it }
    }
}

// Un bloque del mapa (una zona de mesas)
@Composable
fun ZonaMapa(z: Zona, seleccionada: Boolean, modifier: Modifier, onClick: (String) -> Unit) {
    val (borde, fondo, texto) = coloresNivel(z.porcentaje)
    val forma = RoundedCornerShape(18.dp)
    Column(
        modifier
            .height(140.dp)
            .clip(forma)
            .background(fondo)
            .border(if (seleccionada) 4.dp else 2.dp, borde, forma)
            .clickable { onClick(z.nombre) }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(z.icono, z.nombre, tint = texto, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(4.dp))
        Text(z.nombre, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextoOscuro, maxLines = 1)
        Text("${z.porcentaje}%", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = texto)
        Text("${z.ocupados}/${z.capacidad} sillas", fontSize = 14.sp, color = GrisTexto)
    }
}

// Bloque con la fila para servirse
@Composable
fun BloqueFila(personas: Int, minutos: Int, nivel: Int, modifier: Modifier) {
    val (borde, fondo, texto) = coloresNivel(nivel)
    val forma = RoundedCornerShape(18.dp)
    Column(
        modifier
            .height(140.dp)
            .clip(forma)
            .background(fondo)
            .border(2.dp, borde, forma)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Groups, "Fila para servirse", tint = texto, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(4.dp))
        Text("Fila", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
        Text("$personas personas", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = texto)
        Text("Espera ~$minutos min", fontSize = 14.sp, color = GrisTexto)
    }
}

// Franja que separa los pisos, como unas escaleras
@Composable
fun Escaleras() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(GrisClaro)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.ArrowUpward, null, tint = GrisTexto, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(8.dp))
        Text("Escaleras", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = GrisTexto)
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Filled.ArrowDownward, null, tint = GrisTexto, modifier = Modifier.size(24.dp))
    }
}

// Franja de la entrada del comedor
@Composable
fun Entrada() {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GrisClaro)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.AutoMirrored.Filled.Login, null, tint = GrisTexto, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(8.dp))
        Text("Entrada", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = GrisTexto)
    }
}

// Tarjeta con el detalle de la zona tocada
@Composable
fun DetalleZona(z: Zona, alternativa: Zona?, onElegir: (String) -> Unit) {
    val (borde, fondo, texto) = coloresNivel(z.porcentaje)
    val (nivel, fondoChip, textoChip) = nivelAforo(z.porcentaje)
    val mensaje = when (indiceNivel(z.porcentaje)) {
        0 -> "Hay muchas sillas libres. ¡Buen momento para sentarte aquí!"
        1 -> "Hay una cantidad moderada de personas en este piso."
        else -> "Este piso está lleno o casi lleno."
    }

    TarjetaBlanca(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconoGrande(z.icono, texto, fondo, z.nombre, 80, 52)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(z.nombre, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
                Spacer(Modifier.height(4.dp))
                Etiqueta(nivel, fondoChip, textoChip)
            }
            Text("${z.porcentaje}%", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = texto)
        }

        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { z.porcentaje / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(50)),
            color = borde,
            trackColor = GrisClaro,
            strokeCap = StrokeCap.Round
        )
        Spacer(Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.EventSeat, null, tint = texto, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "${z.libres} sillas libres de ${z.capacidad}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextoOscuro
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(mensaje, fontSize = 18.sp, color = GrisTexto)

        // Recomendación si la zona está llena
        alternativa?.let { alt ->
            val (_, fondoAlt, textoAlt) = coloresNivel(alt.porcentaje)
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(fondoAlt)
                    .clickable { onElegir(alt.nombre) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(alt.icono, null, tint = textoAlt, modifier = Modifier.size(34.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Te recomendamos sentarte en:", fontSize = 16.sp, color = GrisTexto)
                    Text(
                        "${alt.nombre} (${alt.libres} sillas libres)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textoAlt
                    )
                }
            }
        }
    }
}

// Leyenda corta de los tres colores
@Composable
fun LeyendaCompacta() {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf(
            VerdeFuerte to "Pocas",
            Amarillo to "Media",
            RojoFuerte to "Lleno"
        ).forEach { (color, texto) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(Modifier.width(6.dp))
                Text(texto, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextoOscuro)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1100)
@Composable
fun MapaPreview() {
    PantallaMapa(aforoComedor = 82)
}