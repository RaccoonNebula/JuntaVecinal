package com.gestion.juntavecinos.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gestion.juntavecinos.model.EstadoReserva
import com.gestion.juntavecinos.model.Reserva
import com.gestion.juntavecinos.ui.component.PantallaBase
import com.gestion.juntavecinos.ui.preview.PreviewData
import com.gestion.juntavecinos.ui.theme.JuntaVecinosTheme
import com.gestion.juntavecinos.utils.conversionPesos

@Composable
fun ReservasScreen(
    reservas: List<Reserva>,
    busqueda: String,
    onBusqueda: (String) -> Unit = {},
    onNueva: () -> Unit = {},
    onConfirmar: (Reserva) -> Unit = {},
    onCancelar: (Reserva) -> Unit = {},
    onBack: () -> Unit = {}
) {
    PantallaBase(
        titulo = "Reservas",
        onBack = onBack,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNueva,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nueva reserva") }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 16.dp).fillMaxSize()) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = onBusqueda,
                label = { Text("Buscar por nombre o dependencia") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (busqueda.isNotEmpty()) {
                        IconButton(onClick = { onBusqueda("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
            if (reservas.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay reservas para mostrar.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(reservas, key = { it.id }) { reserva ->
                        ReservaCard(
                            reserva = reserva,
                            onConfirmar = { onConfirmar(reserva) },
                            onCancelar = { onCancelar(reserva) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReservaCard(
    reserva: Reserva,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorObjetivo = when (reserva.estado) {
        EstadoReserva.PENDIENTE -> Color(0xFFF9A825)
        EstadoReserva.CONFIRMADA -> Color(0xFF2E7D32)
        EstadoReserva.CANCELADA -> MaterialTheme.colorScheme.error
    }
    // Animacion para etiqueta (dependiendo del estado)
    val colorEstado by animateColorAsState(colorObjetivo, tween(500), label = "colorEstado")

    Card(modifier.fillMaxWidth().animateContentSize()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(reserva.dependencia, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(color = colorEstado.copy(alpha = 0.15f), shape = RoundedCornerShape(50)) {
                    Text(
                        reserva.estado.etiqueta,
                        color = colorEstado,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Fila(Icons.Default.Person, "${reserva.solicitante} · ${reserva.telefono}")
            Fila(Icons.Default.Event, "${reserva.fecha} · ${reserva.horaInicio} - ${reserva.horaFin}")
            Fila(Icons.Default.Groups, "${reserva.personas} personas · ${reserva.monto.conversionPesos()}" + if (reserva.usaCocina) " · con cocina" else "")
            Text(reserva.motivo, style = MaterialTheme.typography.bodySmall)
            if (reserva.estado != EstadoReserva.CANCELADA) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (reserva.estado == EstadoReserva.PENDIENTE) {
                        Button(onClick = onConfirmar) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Text(" Confirmar")
                        }
                    }
                    OutlinedButton(onClick = onCancelar) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Text(" Cancelar")
                    }
                }
            }
        }
    }
}

@Composable
private fun Fila(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text("  $texto", style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true, name = "Reservas vertical")
@Composable
private fun ReservasPreview() {
    JuntaVecinosTheme { ReservasScreen(PreviewData.reservas, busqueda = "") }
}

@Preview(showBackground = true, widthDp = 820, heightDp = 380, name = "Reservas horizontal")
@Composable
private fun ReservasHorizontalPreview() {
    JuntaVecinosTheme { ReservasScreen(PreviewData.reservas, busqueda = "") }
}
