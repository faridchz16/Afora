package com.faridchavez.afora

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------- COLORES ----------
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

// ---------- PANTALLA PRINCIPAL ----------
@Composable
fun AforaScreen(
    actual: Int = 78,
    capacidad: Int = 100,
    pm25: Int = 12,
    temperatura: Int = 24
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
    ) {
        Encabezado(campus = "Campus Lima")

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TarjetaAforo(actual, capacidad)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TarjetaSensor(
                    modifier = Modifier.weight(1f),
                    titulo = "Calidad del aire (PM2.5)",
                    icono = Icons.Filled.Eco,
                    colorIcono = VerdeFuerte,
                    fondoIcono = VerdeClaro,
                    valor = "$pm25",
                    unidad = "µg/m³",
                    estado = "Buena"
                )
                TarjetaSensor(
                    modifier = Modifier.weight(1f),
                    titulo = "Temperatura",
                    icono = Icons.Filled.Thermostat,
                    colorIcono = RojoFuerte,
                    fondoIcono = RojoClaro,
                    valor = "$temperatura",
                    unidad = "°C",
                    estado = "Confortable"
                )
            }
        }
    }
}

// ---------- ENCABEZADO ----------
@Composable
fun Encabezado(campus: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(
                Brush.verticalGradient(listOf(Color(0xFF064E3B), Color(0xFF065F46)))
            )
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        Text(
            text = "AFORA",
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            modifier = Modifier.align(Alignment.TopStart)
        )
        Row(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "Ubicación",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(campus, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// ---------- TARJETA DE AFORO ----------
@Composable
fun TarjetaAforo(actual: Int, capacidad: Int) {
    val progreso = actual.toFloat() / capacidad
    val porcentaje = (progreso * 100).toInt()

    // El aviso cambia de color según qué tan lleno está el lugar
    val (nivel, fondoChip, textoChip) = when {
        porcentaje < 50 -> Triple("AFORO BAJO", VerdeClaro, VerdeTexto)
        porcentaje < 85 -> Triple("AFORO MEDIO", AmarilloClaro, AmarilloTexto)
        else -> Triple("AFORO ALTO", RojoClaro, RojoFuerte)
    }
    val colorBarra = if (porcentaje < 85) VerdeFuerte else RojoFuerte

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Aforo actual",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GrisTexto
                )
                Etiqueta(nivel, fondoChip, textoChip)
            }

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoGrande(
                    icono = Icons.Filled.Groups,
                    color = VerdeFuerte,
                    fondo = VerdeClaro,
                    descripcion = "Personas en el lugar",
                    tamanoCaja = 88,
                    tamanoIcono = 56
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontSize = 64.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextoOscuro
                            )
                        ) { append("$actual") }
                        withStyle(SpanStyle(fontSize = 22.sp, color = GrisTexto)) {
                            append(" / $capacidad\npersonas")
                        }
                    },
                    lineHeight = 26.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(50)),
                    color = colorBarra,
                    trackColor = Color(0xFFE5E7EB),
                    strokeCap = StrokeCap.Round
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "$porcentaje%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorBarra
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "Capacidad máxima: $capacidad personas",
                fontSize = 18.sp,
                color = GrisTexto
            )
        }
    }
}

// ---------- TARJETA DE SENSOR (aire / temperatura) ----------
@Composable
fun TarjetaSensor(
    modifier: Modifier = Modifier,
    titulo: String,
    icono: ImageVector,
    colorIcono: Color,
    fondoIcono: Color,
    valor: String,
    unidad: String,
    estado: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = GrisTexto,
                lineHeight = 22.sp,
                minLines = 2
            )
            Spacer(Modifier.height(12.dp))
            IconoGrande(icono, colorIcono, fondoIcono, titulo)
            Spacer(Modifier.height(12.dp))
            Text(
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextoOscuro
                        )
                    ) { append(valor) }
                    withStyle(
                        SpanStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoOscuro
                        )
                    ) { append(" $unidad") }
                }
            )
            Spacer(Modifier.height(8.dp))
            Etiqueta(estado, VerdeClaro, VerdeTexto)
        }
    }
}

// ---------- COMPONENTES REUTILIZABLES ----------
@Composable
fun IconoGrande(
    icono: ImageVector,
    color: Color,
    fondo: Color,
    descripcion: String,
    tamanoCaja: Int = 80,
    tamanoIcono: Int = 52
) {
    Box(
        modifier = Modifier
            .size(tamanoCaja.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(fondo),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = descripcion,
            tint = color,
            modifier = Modifier.size(tamanoIcono.dp)
        )
    }
}

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

// ---------- VISTA PREVIA ----------
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun AforaPreview() {
    AforaScreen()
}