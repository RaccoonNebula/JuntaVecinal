package com.gestion.juntavecinos.ui.screen

import android.content.res.Configuration
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gestion.juntavecinos.model.Reserva
import com.gestion.juntavecinos.ui.component.PantallaBase
import com.gestion.juntavecinos.ui.preview.PreviewData
import com.gestion.juntavecinos.ui.state.ResumenFinanciero
import com.gestion.juntavecinos.ui.theme.JuntaVecinosTheme
import com.gestion.juntavecinos.utils.conversionPesos

@Composable
fun HomeScreen(
    resumen: ResumenFinanciero,
    proximas: List<Reserva>,
    onReservas: () -> Unit = {},
    onNuevaReserva: () -> Unit = {},
    onFinanzas: () -> Unit = {},
    onNuevoMovimiento: () -> Unit = {}
) {
    // Val para diseño adaptable (vertical y horizontal)
    val horizontal = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    PantallaBase(titulo = "Junta de Vecinos Zaror") { padding ->
        if (horizontal) {
            Row(
                Modifier.padding(padding).padding(16.dp).fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TarjetaBalance(resumen)
                    Accesos(onReservas, onNuevaReserva, onFinanzas, onNuevoMovimiento)
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    ProximosArriendos(proximas)
                }
            }
        } else {
            Column(
                Modifier.padding(padding).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TarjetaBalance(resumen)
                Accesos(onReservas, onNuevaReserva, onFinanzas, onNuevoMovimiento)
                ProximosArriendos(proximas)
            }
        }
    }
}

@Composable
private fun TarjetaBalance(resumen: ResumenFinanciero) {
    val balance by animateIntAsState(resumen.balance, animationSpec = tween(800), label = "balance")
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Balance de la Junta de Vecinos Zaror", style = MaterialTheme.typography.labelLarge)
            Text(balance.conversionPesos(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
}

@Composable
private fun Accesos(
    onReservas: () -> Unit,
    onNuevaReserva: () -> Unit,
    onFinanzas: () -> Unit,
    onNuevoMovimiento: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Acceso(Icons.Default.Add, "Nueva reserva", onNuevaReserva, Modifier.weight(1f))
            Acceso(Icons.Default.EventAvailable, "Ver reservas", onReservas, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Acceso(Icons.Default.Paid, "Registrar pago/gasto", onNuevoMovimiento, Modifier.weight(1f))
            Acceso(Icons.Default.AccountBalanceWallet, "Ver finanzas", onFinanzas, Modifier.weight(1f))
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
            Text(texto, style = MaterialTheme.typography.labelLarge)
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
                            Text("${r.fecha} · ${r.horaInicio} - ${r.horaFin} · ${r.estado.etiqueta}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Home vertical")
@Composable
private fun HomePreview() {
    JuntaVecinosTheme { HomeScreen(PreviewData.resumen, PreviewData.reservas)}
}

@Preview(showBackground = true, widthDp = 820, heightDp = 380, name = "Home horizontal")
@Composable
private fun HomeHorizontalPreview() {
    JuntaVecinosTheme { HomeScreen(PreviewData.resumen, PreviewData.reservas)}
}
