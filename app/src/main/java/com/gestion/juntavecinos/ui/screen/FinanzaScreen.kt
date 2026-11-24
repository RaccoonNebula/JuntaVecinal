package com.gestion.juntavecinos.ui.screen

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gestion.juntavecinos.data.remote.ClienteApi
import com.gestion.juntavecinos.model.Movimiento
import com.gestion.juntavecinos.model.TipoMovimiento
import com.gestion.juntavecinos.ui.component.PantallaBase
import com.gestion.juntavecinos.ui.preview.PreviewData
import com.gestion.juntavecinos.ui.state.ResumenFinanciero
import com.gestion.juntavecinos.ui.theme.JuntaVecinosTheme
import com.gestion.juntavecinos.utils.conversionPesos

@Composable
fun FinanzasScreen(
    movimientos: List<Movimiento>,
    resumen: ResumenFinanciero?,
    puedeRegistrar: Boolean = true,
    cargando: Boolean = false,
    mensaje: String? = null,
    onMensajeMostrado: () -> Unit = {},
    onNuevo: () -> Unit = {},
    onEliminar: (Movimiento) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val horizontal = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    var porEliminar by remember { mutableStateOf<Movimiento?>(null) }
    // Solo Tesoreria puede registrar o anular; la directiva consulta
    val anular: ((Movimiento) -> Unit)? = if (puedeRegistrar) { m -> porEliminar = m } else null

    PantallaBase(
        titulo = "Finanzas",
        onBack = onBack,
        cargando = cargando,
        mensaje = mensaje,
        onMensajeMostrado = onMensajeMostrado,
        floatingActionButton = {
            if (puedeRegistrar) ExtendedFloatingActionButton(
                onClick = onNuevo,
                icon = { Icon(Icons.Default.Add, contentDescription = "Nuevo movimiento") },
                text = { Text("Nuevo movimiento") }
            )
        }
    ) { padding ->
        if (horizontal) {
            Row(Modifier.padding(padding).padding(16.dp).fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.weight(1f)) { resumen?.let { ResumenCard(it) } }
                Box(Modifier.weight(1.4f)) { ListaMovimientos(movimientos, anular) }
            }
        } else {
            Column(Modifier.padding(padding).padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                resumen?.let { ResumenCard(it) }
                ListaMovimientos(movimientos, anular)
            }
        }
    }

    // Confirmacion antes de anular un registro financiero
    porEliminar?.let { mov ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Anular movimiento") },
            text = { Text("¿Anular \"${mov.descripcion}\"? Dejará de contarse en los totales y no se puede deshacer.") },
            confirmButton = { TextButton(onClick = { onEliminar(mov); porEliminar = null }) { Text("Anular") } },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
internal fun ResumenCard(resumen: ResumenFinanciero) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Resumen financiero (datos ficticios)", style = MaterialTheme.typography.labelLarge)
            Text("Ingresos: ${resumen.ingresos.conversionPesos()}")
            Text("Gastos: ${resumen.gastos.conversionPesos()}")
            Text("Balance: ${resumen.balance.conversionPesos()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ListaMovimientos(movimientos: List<Movimiento>, onEliminar: ((Movimiento) -> Unit)?) {
    if (movimientos.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Aún no hay movimientos registrados.") }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 88.dp)) {
            items(movimientos, key = { it.id }) { mov ->
                MovimientoItem(mov, onEliminar?.let { { it(mov) } }, Modifier.animateItem())
            }
        }
    }
}

@Composable
private fun MovimientoItem(mov: Movimiento, onEliminar: (() -> Unit)?, modifier: Modifier = Modifier) {
    val esIngreso = mov.tipo == TipoMovimiento.INGRESO
    val color = if (esIngreso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Card(modifier.fillMaxWidth().animateContentSize()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (esIngreso) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = mov.tipo.etiqueta,
                tint = color
            )
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(mov.categoria, fontWeight = FontWeight.SemiBold)
                Text(mov.descripcion, style = MaterialTheme.typography.bodySmall)
                if (mov.reservaId != null) {
                    Text(
                        "Pago de reserva N° ${mov.reservaId}" + (mov.medioPago?.let { " · $it" } ?: "") + (mov.referencia?.let { " · $it" } ?: ""),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(mov.fecha, style = MaterialTheme.typography.labelSmall)
            }
            // Miniatura del comprobante guardado en el backend
            mov.comprobanteId?.let { id ->
                AsyncImage(
                    model = ClienteApi.urlComprobante(id),
                    contentDescription = "Comprobante",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
                )
            }
            Text(
                (if (esIngreso) "+" else "-") + mov.monto.conversionPesos(),
                color = color,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            if (onEliminar != null) {
                IconButton(onClick = onEliminar) { Icon(Icons.Default.Delete, contentDescription = "Anular") }
            }
        }
    }
}

@Preview(showBackground = true, name = "Finanzas vertical")
@Composable
private fun FinanzasPreview() {
    JuntaVecinosTheme { FinanzasScreen(PreviewData.movimientos, PreviewData.resumen) }
}

@Preview(showBackground = true, widthDp = 820, heightDp = 380, name = "Finanzas horizontal")
@Composable
private fun FinanzasHorizontalPreview() {
    JuntaVecinosTheme { FinanzasScreen(PreviewData.movimientos, PreviewData.resumen) }
}
