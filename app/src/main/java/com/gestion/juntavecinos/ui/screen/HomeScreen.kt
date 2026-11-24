package com.gestion.juntavecinos.ui.screen

import android.content.res.Configuration
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gestion.juntavecinos.model.Reserva
import com.gestion.juntavecinos.model.Rol
import com.gestion.juntavecinos.model.Sesion
import com.gestion.juntavecinos.ui.component.DialogoConfirmar
import com.gestion.juntavecinos.ui.component.PantallaBase
import com.gestion.juntavecinos.ui.component.nombreVisible
import com.gestion.juntavecinos.ui.preview.PreviewData
import com.gestion.juntavecinos.ui.state.ResumenFinanciero
import com.gestion.juntavecinos.ui.theme.JuntaVecinosTheme
import com.gestion.juntavecinos.utils.conversionPesos

/** Acceso directo de la pantalla de inicio */
data class AccesoHome(val icono: ImageVector, val texto: String, val onClick: () -> Unit = {})

@Composable
fun HomeScreen(
    sesion: Sesion,
    resumen: ResumenFinanciero?,
    proximas: List<Reserva>,
    accesos: List<AccesoHome>,
    avisosSinLeer: Int = 0,
    cargando: Boolean = false,
    mensaje: String? = null,
    onMensajeMostrado: () -> Unit = {},
    onAvisos: () -> Unit = {},
    onRefrescar: () -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    // Val para diseño adaptable (vertical y horizontal)
    val horizontal = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }

    PantallaBase(
        titulo = "Junta de Vecinos Zaror",
        cargando = cargando,
        mensaje = mensaje,
        onMensajeMostrado = onMensajeMostrado,
        acciones = {
            IconButton(onClick = onRefrescar) { Icon(Icons.Default.Refresh, contentDescription = "Actualizar") }
            IconButton(onClick = onAvisos) {
                BadgedBox(badge = { if (avisosSinLeer > 0) Badge { Text("$avisosSinLeer") } }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                }
            }
            IconButton(onClick = { confirmarSalida = true }) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
            }
        }
    ) { padding ->
        if (horizontal) {
            Row(
                Modifier.padding(padding).padding(16.dp).fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Saludo(sesion)
                    resumen?.let { TarjetaBalance(it) }
                    Accesos(accesos)
                }
                if (sesion.rol.veReservas) {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) { ProximosArriendos(proximas) }
                }
            }
        } else {
            Column(
                Modifier.padding(padding).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Saludo(sesion)
                resumen?.let { TarjetaBalance(it) }
                Accesos(accesos)
                if (sesion.rol.veReservas) ProximosArriendos(proximas)
            }
        }
    }

    if (confirmarSalida) {
        DialogoConfirmar(
            "Cerrar sesión", "¿Deseas salir de tu cuenta?", "Salir",
            onAceptar = { confirmarSalida = false; onCerrarSesion() },
            onCerrar = { confirmarSalida = false }
        )
    }
}

@Composable
private fun Saludo(sesion: Sesion) {
    Column {
        Text("Hola, ${sesion.nombre}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Perfil: ${sesion.rol.etiqueta}", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun TarjetaBalance(resumen: ResumenFinanciero) {
    // La animacion usa Int; el valor se limita por seguridad
    val objetivo = resumen.balance.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
    val balance by animateIntAsState(objetivo, animationSpec = tween(800), label = "balance")
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Balance de la Junta de Vecinos Zaror", style = MaterialTheme.typography.labelLarge)
            Text(balance.conversionPesos(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TrendingUp, contentDescription = "Ingresos", tint = MaterialTheme.colorScheme.primary)
                Text("  Ingresos: ${resumen.ingresos.conversionPesos()}")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TrendingDown, contentDescription = "Gastos", tint = MaterialTheme.colorScheme.error)
                Text("  Gastos: ${resumen.gastos.conversionPesos()}")
            }
        }
    }
}

@Composable
private fun Accesos(accesos: List<AccesoHome>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        accesos.chunked(2).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                fila.forEach { Acceso(it.icono, it.texto, it.onClick, Modifier.weight(1f)) }
                if (fila.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Acceso(icono: ImageVector, texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(texto, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ProximosArriendos(proximas: List<Reserva>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Próximos arriendos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (proximas.isEmpty()) {
            Text("No hay arriendos próximos.", style = MaterialTheme.typography.bodyMedium)
        } else {
            proximas.forEach { r ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(Modifier.padding(start = 12.dp)) {
                            Text(r.dependencia, fontWeight = FontWeight.SemiBold)
                            Text("${r.fecha} · ${r.horaInicio} - ${r.horaFin} · ${r.estado.nombreVisible()}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

private val sesionPreview = Sesion(1, "Vecina Demo", "vecina@zaror.test", "900000001", Rol.VECINO, "", 0L)
private val accesosPreview = listOf(
    AccesoHome(Icons.Default.Add, "Solicitar reserva"), AccesoHome(Icons.Default.EventAvailable, "Mis reservas")
)

@Preview(showBackground = true, name = "Home vertical")
@Composable
private fun HomePreview() {
    JuntaVecinosTheme { HomeScreen(sesionPreview, PreviewData.resumen, PreviewData.reservas, accesosPreview, avisosSinLeer = 2) }
}

@Preview(showBackground = true, widthDp = 820, heightDp = 380, name = "Home horizontal")
@Composable
private fun HomeHorizontalPreview() {
    JuntaVecinosTheme { HomeScreen(sesionPreview, PreviewData.resumen, PreviewData.reservas, accesosPreview) }
}
