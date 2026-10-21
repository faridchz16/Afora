package com.faridchavez.afora

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ---------- COLORES EXTRA ----------
val VerdeOscuro = Color(0xFF064E3B)
val Amarillo = Color(0xFFFBBF24)
val GrisClaro = Color(0xFFE5E7EB)
val AzulClaro = Color(0xFFE0F2FE)
val AzulFuerte = Color(0xFF0284C7)

// ---------- DATOS DE EJEMPLO ----------
val datosAforoDia = listOf(5f, 15f, 20f, 35f, 48f, 45f, 60f, 65f, 80f, 75f, 78f, 62f, 50f, 58f)
val datosAire = listOf(11f, 10.5f, 11.5f, 12f, 11.5f, 13f, 13.5f, 12.5f, 12f, 13f, 13.5f, 13f, 14f)

enum class Pantalla(val titulo: String) {
    INICIO("Inicio"),
    AFORO("Aforo en tiempo real"),
    AMBIENTE("Ambiente"),
    HISTORIAL("Historial"),
    NOTIFICACIONES("Notificaciones")
}

// =====================================================
// APP: decide si mostrar el login o la app principal
// =====================================================
@Composable
fun AforaApp() {
    var usuario by rememberSaveable { mutableStateOf<String?>(null) }
    val u = usuario
    if (u == null) {
        PantallaLogin(onIngresar = { usuario = it })
    } else {
        AppPrincipal(usuario = u, onCerrarSesion = { usuario = null })
    }
}

// =====================================================
// INICIO DE SESIÓN
// =====================================================
@Composable
fun PantallaLogin(onIngresar: (String) -> Unit) {
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var verClave by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(VerdeOscuro, Color(0xFF065F46))))
                .statusBarsPadding()
                .padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Groups, "Logo AFORA", tint = Color.White, modifier = Modifier.size(64.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("AFORA", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(
                "Aforo y ambiente en tiempo real",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.padding(24.dp)) {
                Text("Iniciar sesión", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
                Text("Ingresa con tu correo institucional", fontSize = 18.sp, color = GrisTexto)
                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it; error = null },
                    label = { Text("Correo", fontSize = 18.sp) },
                    leadingIcon = { Icon(Icons.Filled.Email, null, Modifier.size(28.dp)) },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 20.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = clave,
                    onValueChange = { clave = it; error = null },
                    label = { Text("Contraseña", fontSize = 18.sp) },
                    leadingIcon = { Icon(Icons.Filled.Lock, null, Modifier.size(28.dp)) },
                    trailingIcon = {
                        IconButton(onClick = { verClave = !verClave }) {
                            Icon(
                                if (verClave) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                if (verClave) "Ocultar contraseña" else "Mostrar contraseña",
                                Modifier.size(28.dp)
                            )
                        }
                    },
                    visualTransformation = if (verClave) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 20.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = RojoFuerte, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        when {
                            correo.isBlank() || clave.isBlank() -> error = "Completa tu correo y contraseña"
                            !correo.contains("@") -> error = "Ingresa un correo válido"
                            clave.length < 4 -> error = "La contraseña debe tener al menos 4 caracteres"
                            else -> onIngresar(correo)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeFuerte)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Login, null, Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ingresar", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =====================================================
// APP PRINCIPAL: menú lateral + barra superior + barra inferior
// =====================================================
@Composable
fun AppPrincipal(usuario: String, onCerrarSesion: () -> Unit) {
    var pantalla by rememberSaveable { mutableStateOf(Pantalla.INICIO) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Botón "atrás" del celular: cierra el menú o vuelve a Inicio
    BackHandler(enabled = drawerState.isOpen || pantalla != Pantalla.INICIO) {
        if (drawerState.isOpen) scope.launch { drawerState.close() }
        else pantalla = Pantalla.INICIO
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MenuLateral(
                usuario = usuario,
                actual = pantalla,
                onElegir = {
                    pantalla = it
                    scope.launch { drawerState.close() }
                },
                onCerrarSesion = {
                    scope.launch { drawerState.close() }
                    onCerrarSesion()
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                BarraSuperior(
                    titulo = pantalla.titulo,
                    onMenu = { scope.launch { drawerState.open() } },
                    onNotificaciones = { pantalla = Pantalla.NOTIFICACIONES }
                )
            },
            bottomBar = { BarraInferior(pantalla) { pantalla = it } },
            containerColor = Fondo
        ) { padding ->
            Box(Modifier.padding(padding)) {
                when (pantalla) {
                    Pantalla.INICIO -> PantallaInicio()
                    Pantalla.AFORO -> PantallaAforo()
                    Pantalla.AMBIENTE -> PantallaAmbiente()
                    Pantalla.HISTORIAL -> PantallaHistorial()
                    Pantalla.NOTIFICACIONES -> PantallaNotificaciones()
                }
            }
        }
    }
}

@Composable
fun BarraSuperior(titulo: String, onMenu: () -> Unit, onNotificaciones: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(VerdeOscuro)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenu, modifier = Modifier.size(56.dp)) {
            Icon(Icons.Filled.Menu, "Abrir menú", tint = Color.White, modifier = Modifier.size(36.dp))
        }
        Text(
            titulo,
            modifier = Modifier.weight(1f),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onNotificaciones, modifier = Modifier.size(56.dp)) {
            Icon(Icons.Filled.Notifications, "Notificaciones", tint = Color.White, modifier = Modifier.size(34.dp))
        }
    }
}

@Composable
fun BarraInferior(actual: Pantalla, onElegir: (Pantalla) -> Unit) {
    val opciones = listOf(
        Triple(Pantalla.INICIO, "Inicio", Icons.Filled.Home),
        Triple(Pantalla.AFORO, "Aforo", Icons.Filled.Groups),
        Triple(Pantalla.AMBIENTE, "Ambiente", Icons.Filled.Eco),
        Triple(Pantalla.HISTORIAL, "Historial", Icons.AutoMirrored.Filled.ShowChart)
    )
    NavigationBar(containerColor = Color.White) {
        opciones.forEach { (p, nombre, icono) ->
            NavigationBarItem(
                selected = actual == p,
                onClick = { onElegir(p) },
                icon = { Icon(icono, nombre, Modifier.size(32.dp)) },
                label = { Text(nombre, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VerdeFuerte,
                    selectedTextColor = VerdeFuerte,
                    indicatorColor = VerdeClaro,
                    unselectedIconColor = GrisTexto,
                    unselectedTextColor = GrisTexto
                )
            )
        }
    }
}

@Composable
fun MenuLateral(
    usuario: String,
    actual: Pantalla,
    onElegir: (Pantalla) -> Unit,
    onCerrarSesion: () -> Unit
) {
    ModalDrawerSheet(drawerContainerColor = Color.White) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(VerdeOscuro)
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            Box(
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, null, tint = Color.White, modifier = Modifier.size(44.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text("AFORA", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(usuario, fontSize = 16.sp, color = Color.White.copy(alpha = 0.85f))
        }

        Spacer(Modifier.height(12.dp))

        val opciones = listOf(
            Triple(Pantalla.INICIO, "Inicio", Icons.Filled.Home),
            Triple(Pantalla.AFORO, "Aforo", Icons.Filled.Groups),
            Triple(Pantalla.AMBIENTE, "Ambiente", Icons.Filled.Eco),
            Triple(Pantalla.HISTORIAL, "Historial", Icons.AutoMirrored.Filled.ShowChart),
            Triple(Pantalla.NOTIFICACIONES, "Notificaciones", Icons.Filled.Notifications)
        )
        opciones.forEach { (p, nombre, icono) ->
            NavigationDrawerItem(
                label = { Text(nombre, fontSize = 20.sp, fontWeight = FontWeight.SemiBold) },
                selected = actual == p,
                onClick = { onElegir(p) },
                icon = { Icon(icono, null, Modifier.size(30.dp)) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = VerdeClaro,
                    selectedIconColor = VerdeFuerte,
                    selectedTextColor = VerdeTexto
                )
            )
        }

        Spacer(Modifier.weight(1f))
        HorizontalDivider(color = GrisClaro)

        NavigationDrawerItem(
            label = {
                Text("Cerrar sesión", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = RojoFuerte)
            },
            selected = false,
            onClick = onCerrarSesion,
            icon = {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = RojoFuerte, modifier = Modifier.size(30.dp))
            },
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
                .padding(bottom = 16.dp)
        )
    }
}

// =====================================================
// PANTALLAS
// =====================================================
@Composable
fun PantallaInicio() {
    ContenidoPantalla {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocationOn, null, tint = VerdeFuerte, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(6.dp))
            Text("Campus Lima", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
        }
        TarjetaAforo(actual = 78, capacidad = 100)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TarjetaSensor(
                modifier = Modifier.weight(1f),
                titulo = "Calidad del aire (PM2.5)",
                icono = Icons.Filled.Eco,
                colorIcono = VerdeFuerte,
                fondoIcono = VerdeClaro,
                valor = "12",
                unidad = "µg/m³",
                estado = "Buena"
            )
            TarjetaSensor(
                modifier = Modifier.weight(1f),
                titulo = "Temperatura",
                icono = Icons.Filled.Thermostat,
                colorIcono = RojoFuerte,
                fondoIcono = RojoClaro,
                valor = "24",
                unidad = "°C",
                estado = "Confortable"
            )
        }
    }
}

@Composable
fun PantallaAforo(actual: Int = 78, capacidad: Int = 100) {
    val progreso = actual.toFloat() / capacidad
    val porcentaje = (progreso * 100).toInt()
    val (nivel, fondoChip, textoChip) = nivelAforo(porcentaje)
    val colorArco = when {
        porcentaje < 50 -> VerdeFuerte
        porcentaje < 85 -> Amarillo
        else -> RojoFuerte
    }

    ContenidoPantalla {
        LineaEstado("Actualización en vivo", "Hoy")

        TarjetaBlanca(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Medidor(progreso, colorArco, "$actual", "personas")
                Spacer(Modifier.height(12.dp))
                Etiqueta(nivel, fondoChip, textoChip)
                Spacer(Modifier.height(12.dp))
                Text(
                    "$porcentaje%  •  Capacidad: $capacidad",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GrisTexto
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TarjetaContador(
                Modifier.weight(1f), Icons.AutoMirrored.Filled.Login,
                VerdeFuerte, VerdeClaro, "250", "Entradas hoy"
            )
            TarjetaContador(
                Modifier.weight(1f), Icons.AutoMirrored.Filled.Logout,
                VerdeOscuro, Color(0xFFE2E8F0), "240", "Salidas hoy"
            )
        }

        TarjetaContador(
            Modifier.fillMaxWidth(), Icons.Filled.Groups,
            AmarilloTexto, AmarilloClaro, "$actual", "Personas dentro", horizontal = true
        )

        TarjetaBlanca(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Flujo de hoy", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
                    Text("Ocupación por hora", fontSize = 18.sp, color = GrisTexto)
                }
                Text("$porcentaje%", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = VerdeFuerte)
            }
            Spacer(Modifier.height(16.dp))
            GraficoLinea(
                datos = datosAforoDia,
                minY = 0f, maxY = 100f,
                colorLinea = VerdeFuerte, colorRelleno = VerdeFuerte,
                etiquetasY = listOf("100", "75", "50", "25", "0"),
                etiquetasX = listOf("08:00", "12:00", "16:00", "20:00")
            )
        }
    }
}

@Composable
fun PantallaAmbiente(pm25: Int = 12) {
    ContenidoPantalla {
        LineaEstado("Sensor activo", "PM2.5 · Comedor")

        TarjetaBlanca(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Anillo(progreso = 1f - pm25 / 50f, color = VerdeFuerte) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconoGrande(Icons.Filled.Eco, VerdeFuerte, VerdeClaro, "Calidad del aire", 72, 46)
                        Text(
                            "$pm25",
                            fontSize = 56.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextoOscuro,
                            lineHeight = 58.sp
                        )
                        Text("µg/m³", fontSize = 20.sp, color = GrisTexto)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Etiqueta("Buena", VerdeClaro, VerdeTexto)
                Spacer(Modifier.height(12.dp))
                Text(
                    "El ambiente es saludable",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "La calidad del aire se encuentra dentro de los niveles recomendados.",
                    fontSize = 18.sp,
                    color = GrisTexto,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TarjetaDato(
                Modifier.weight(1f), Icons.Filled.Thermostat, RojoFuerte, RojoClaro,
                "Temperatura", "24 °C", "Confortable"
            )
            TarjetaDato(
                Modifier.weight(1f), Icons.Filled.WaterDrop, AzulFuerte, AzulClaro,
                "Humedad", "56%", "Adecuada"
            )
        }

        TarjetaBlanca(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoGrande(Icons.Filled.Eco, VerdeFuerte, VerdeClaro, "Condiciones", 72, 44)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Condiciones óptimas", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
                    Text(
                        "Puedes permanecer en el ambiente con total tranquilidad.",
                        fontSize = 17.sp,
                        color = GrisTexto
                    )
                }
            }
        }
    }
}

@Composable
fun PantallaHistorial() {
    var periodo by rememberSaveable { mutableStateOf(0) }
    var indiceFecha by rememberSaveable { mutableStateOf(0) }

    val periodos = listOf("Día", "Semana", "Mes")
    val fechas = when (periodo) {
        0 -> listOf("Lun, 12 de may.", "Mar, 13 de may.", "Mié, 14 de may.")
        1 -> listOf("5 al 11 de mayo", "12 al 18 de mayo")
        else -> listOf("Abril 2025", "Mayo 2025")
    }
    val etiquetasX = when (periodo) {
        0 -> listOf("08:00", "12:00", "16:00", "20:00")
        1 -> listOf("Lun", "Mié", "Vie", "Dom")
        else -> listOf("Sem 1", "Sem 2", "Sem 3", "Sem 4")
    }
    val ajuste = indiceFecha * 6f + periodo * 4f
    val aforo = datosAforoDia.map { (it + ajuste).coerceAtMost(100f) }
    val aire = datosAire.map { (it + ajuste / 6f).coerceAtMost(25f) }

    ContenidoPantalla {
        // Selector Día / Semana / Mes
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(GrisClaro)
                .padding(6.dp)
        ) {
            periodos.forEachIndexed { i, nombre ->
                val elegido = periodo == i
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (elegido) Color.White else Color.Transparent)
                        .clickable { periodo = i; indiceFecha = 0 }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        nombre,
                        fontSize = 19.sp,
                        fontWeight = if (elegido) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (elegido) TextoOscuro else GrisTexto
                    )
                }
            }
        }

        // Selector de fecha
        TarjetaBlanca(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                BotonFlecha(Icons.Filled.ChevronLeft, "Anterior") {
                    if (indiceFecha > 0) indiceFecha--
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Fecha seleccionada", fontSize = 16.sp, color = GrisTexto)
                    Text(fechas[indiceFecha], fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
                }
                BotonFlecha(Icons.Filled.ChevronRight, "Siguiente") {
                    if (indiceFecha < fechas.lastIndex) indiceFecha++
                }
            }
        }

        TarjetaGrafico("Aforo", "Personas dentro del comedor", "Promedio ${aforo.average().toInt()}", VerdeFuerte) {
            GraficoLinea(
                aforo, 0f, 100f, VerdeFuerte, VerdeFuerte,
                listOf("100", "75", "50", "25", "0"), etiquetasX
            )
        }

        TarjetaGrafico("Calidad del aire", "Partículas PM2.5 · µg/m³", "Promedio ${aire.average().toInt()}", VerdeOscuro) {
            GraficoLinea(
                aire, 0f, 25f, VerdeOscuro, Color(0xFF94A3B8),
                listOf("25", "20", "15", "10", "5", "0"), etiquetasX
            )
        }
    }
}

data class Notificacion(
    val titulo: String,
    val hora: String,
    val detalle: String,
    val icono: ImageVector,
    val color: Color,
    val fondo: Color,
    val nueva: Boolean
)

@Composable
fun PantallaNotificaciones() {
    var leidas by rememberSaveable { mutableStateOf(false) }
    val lista = listOf(
        Notificacion("Aforo en nivel medio", "11:30 a.m.", "78% de capacidad", Icons.Filled.Groups, AmarilloTexto, AmarilloClaro, true),
        Notificacion("Calidad del aire buena", "10:15 a.m.", "12 µg/m³", Icons.Filled.Eco, VerdeFuerte, VerdeClaro, true),
        Notificacion("Nuevo registro de entrada", "09:05 a.m.", "+1 persona", Icons.Filled.Add, VerdeOscuro, Color(0xFFE2E8F0), true),
        Notificacion("Alerta aforo alto", "12 may, 12:00 p.m.", "95% de capacidad", Icons.Filled.Groups, RojoFuerte, RojoClaro, false),
        Notificacion("Calidad del aire buena", "12 may, 11:00 a.m.", "Nivel saludable", Icons.Filled.Eco, VerdeFuerte, VerdeClaro, false)
    )

    ContenidoPantalla {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Hoy", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
            Text(
                "Marcar todo como leído",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = VerdeFuerte,
                modifier = Modifier
                    .clickable { leidas = true }
                    .padding(8.dp)
            )
        }

        TarjetaBlanca(Modifier.fillMaxWidth()) {
            lista.forEachIndexed { i, n ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    IconoGrande(n.icono, n.color, n.fondo, n.titulo, 68, 42)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(n.titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
                        Text(n.hora, fontSize = 16.sp, color = GrisTexto)
                        Spacer(Modifier.height(4.dp))
                        Text(n.detalle, fontSize = 18.sp, color = GrisTexto)
                    }
                    if (n.nueva && !leidas) {
                        Box(
                            Modifier
                                .padding(top = 6.dp)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(VerdeFuerte)
                        )
                    }
                }
                if (i < lista.lastIndex) HorizontalDivider(color = GrisClaro)
            }
        }

        Text(
            "No hay más notificaciones",
            fontSize = 16.sp,
            color = GrisTexto,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// =====================================================
// COMPONENTES
// =====================================================
fun nivelAforo(porcentaje: Int): Triple<String, Color, Color> = when {
    porcentaje < 50 -> Triple("AFORO BAJO", VerdeClaro, VerdeTexto)
    porcentaje < 85 -> Triple("AFORO MEDIO", AmarilloClaro, AmarilloTexto)
    else -> Triple("AFORO ALTO", RojoClaro, RojoFuerte)
}

@Composable
fun ContenidoPantalla(contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = contenido
    )
}

@Composable
fun TarjetaBlanca(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(20.dp), content = contenido)
    }
}

@Composable
fun LineaEstado(izquierda: String, derecha: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(VerdeFuerte)
            )
            Spacer(Modifier.width(8.dp))
            Text(izquierda, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = VerdeFuerte)
        }
        Text(derecha, fontSize = 17.sp, color = GrisTexto)
    }
}

@Composable
fun TarjetaContador(
    modifier: Modifier,
    icono: ImageVector,
    color: Color,
    fondo: Color,
    valor: String,
    texto: String,
    horizontal: Boolean = false
) {
    TarjetaBlanca(modifier) {
        if (horizontal) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoGrande(icono, color, fondo, texto, 72, 44)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(valor, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = TextoOscuro)
                    Text(texto, fontSize = 18.sp, color = GrisTexto)
                }
            }
        } else {
            IconoGrande(icono, color, fondo, texto, 72, 44)
            Spacer(Modifier.height(12.dp))
            Text(valor, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = TextoOscuro)
            Text(texto, fontSize = 18.sp, color = GrisTexto)
        }
    }
}

@Composable
fun TarjetaDato(
    modifier: Modifier,
    icono: ImageVector,
    color: Color,
    fondo: Color,
    titulo: String,
    valor: String,
    estado: String
) {
    TarjetaBlanca(modifier) {
        IconoGrande(icono, color, fondo, titulo, 72, 44)
        Spacer(Modifier.height(12.dp))
        Text(titulo, fontSize = 18.sp, color = GrisTexto)
        Text(valor, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = TextoOscuro)
        Spacer(Modifier.height(8.dp))
        Etiqueta(estado, VerdeClaro, VerdeTexto)
    }
}

@Composable
fun TarjetaGrafico(
    titulo: String,
    subtitulo: String,
    promedio: String,
    colorPunto: Color,
    grafico: @Composable () -> Unit
) {
    TarjetaBlanca(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(titulo, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
                Text(subtitulo, fontSize = 17.sp, color = GrisTexto)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(colorPunto)
                )
                Spacer(Modifier.width(6.dp))
                Text(promedio, fontSize = 15.sp, color = GrisTexto)
            }
        }
        Spacer(Modifier.height(16.dp))
        grafico()
    }
}

@Composable
fun BotonFlecha(icono: ImageVector, descripcion: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Fondo)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, descripcion, tint = TextoOscuro, modifier = Modifier.size(34.dp))
    }
}

// Medio círculo (pantalla Aforo)
@Composable
fun Medidor(progreso: Float, color: Color, valor: String, unidad: String) {
    Box(Modifier.size(width = 260.dp, height = 150.dp), contentAlignment = Alignment.BottomCenter) {
        Canvas(Modifier.fillMaxSize()) {
            val grosor = 26.dp.toPx()
            val diametro = size.width - grosor
            val esquina = Offset(grosor / 2, grosor / 2)
            val tam = Size(diametro, diametro)
            drawArc(GrisClaro, 180f, 180f, false, esquina, tam, style = Stroke(grosor, cap = StrokeCap.Round))
            drawArc(
                color, 180f, 180f * progreso.coerceIn(0f, 1f), false, esquina, tam,
                style = Stroke(grosor, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(valor, fontSize = 64.sp, fontWeight = FontWeight.ExtraBold, color = TextoOscuro, lineHeight = 64.sp)
            Text(unidad, fontSize = 20.sp, color = GrisTexto)
        }
    }
}

// Círculo completo (pantalla Ambiente)
@Composable
fun Anillo(progreso: Float, color: Color, contenido: @Composable () -> Unit) {
    Box(Modifier.size(240.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val grosor = 24.dp.toPx()
            val esquina = Offset(grosor / 2, grosor / 2)
            val tam = Size(size.width - grosor, size.height - grosor)
            drawArc(GrisClaro, 0f, 360f, false, esquina, tam, style = Stroke(grosor))
            drawArc(
                color, -90f, 360f * progreso.coerceIn(0f, 1f), false, esquina, tam,
                style = Stroke(grosor, cap = StrokeCap.Round)
            )
        }
        contenido()
    }
}

// Gráfico de línea suave con relleno degradado
@Composable
fun GraficoLinea(
    datos: List<Float>,
    minY: Float,
    maxY: Float,
    colorLinea: Color,
    colorRelleno: Color,
    etiquetasY: List<String>,
    etiquetasX: List<String>
) {
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .height(190.dp)
        ) {
            Column(
                Modifier
                    .fillMaxHeight()
                    .width(36.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                etiquetasY.forEach { Text(it, fontSize = 13.sp, color = GrisTexto) }
            }
            Canvas(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp)
            ) {
                val ancho = size.width
                val alto = size.height
                val divisiones = etiquetasY.size - 1

                // Líneas guía punteadas
                for (i in 0..divisiones) {
                    val y = alto * i / divisiones
                    drawLine(
                        GrisClaro, Offset(0f, y), Offset(ancho, y), strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }

                val paso = ancho / (datos.size - 1)
                val puntos = datos.mapIndexed { i, v ->
                    Offset(i * paso, alto - (v - minY) / (maxY - minY) * alto)
                }

                val linea = Path().apply {
                    moveTo(puntos[0].x, puntos[0].y)
                    for (i in 1 until puntos.size) {
                        val a = puntos[i - 1]
                        val b = puntos[i]
                        val medio = (a.x + b.x) / 2
                        cubicTo(medio, a.y, medio, b.y, b.x, b.y)
                    }
                }
                val relleno = Path().apply {
                    addPath(linea)
                    lineTo(ancho, alto)
                    lineTo(0f, alto)
                    close()
                }

                drawPath(
                    relleno,
                    Brush.verticalGradient(listOf(colorRelleno.copy(alpha = 0.4f), Color.Transparent))
                )
                drawPath(linea, colorLinea, style = Stroke(width = 6f, cap = StrokeCap.Round))
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 36.dp, top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            etiquetasX.forEach { Text(it, fontSize = 13.sp, color = GrisTexto) }
        }
    }
}

// =====================================================
// VISTAS PREVIAS
// =====================================================
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun LoginPreview() {
    PantallaLogin(onIngresar = {})
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun AppPreview() {
    AppPrincipal(usuario = "farid@tecsup.edu.pe", onCerrarSesion = {})
}