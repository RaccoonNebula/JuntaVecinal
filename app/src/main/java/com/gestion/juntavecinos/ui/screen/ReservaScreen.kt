package com.gestion.juntavecinos.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gestion.juntavecinos.model.EstadoReserva
import com.gestion.juntavecinos.model.Reserva
import com.gestion.juntavecinos.model.Rol
import com.gestion.juntavecinos.ui.component.DialogoConfirmar
import com.gestion.juntavecinos.ui.component.DialogoTexto
import com.gestion.juntavecinos.ui.component.EtiquetaEstado
import com.gestion.juntavecinos.ui.component.FilaDato
import com.gestion.juntavecinos.ui.component.PantallaBase
import com.gestion.juntavecinos.ui.preview.PreviewData
import com.gestion.juntavecinos.ui.theme.JuntaVecinosTheme
import com.gestion.juntavecinos.utils.conversionPesos

@Composable
fun ReservasScreen(
    reservas: List<Reserva>,
    busqueda: String,
    rol: Rol,
    cargando: Boolean = false,
    mensaje: String? = null,
    onMensajeMostrado: () -> Unit = {},
    onBusqueda: (String) -> Unit = {},
    onNueva: () -> Unit = {},
    onAbrir: (Reserva) -> Unit = {},
    onEstado: (Reserva, EstadoReserva, String) -> Unit = { _, _, _ -> },
    onCorregir: (Reserva) -> Unit = {},
    onBack: () -> Unit = {}
) {
    PantallaBase(
        titulo = when {
            rol.solicitaReservas -> "Mis reservas"
            rol.resuelveReservas -> "Solicitudes de reserva"
            else -> "Reservas confirmadas"
        },
        onBack = onBack,
        cargando = cargando,
        mensaje = mensaje,
        onMensajeMostrado = onMensajeMostrado,
        floatingActionButton = {
            if (rol.solicitaReservas) ExtendedFloatingActionButton(
                onClick = onNueva,
                icon = { Icon(Icons.Default.Add, contentDescription = "Solicitar reserva") },
                text = { Text("Solicitar reserva") }
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
                    Text(if (cargando) "Cargando reservas…" else "No hay reservas para mostrar.")
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
                            onAbrir = { onAbrir(reserva) },
                            modifier = Modifier.animateItem()
                        ) { AccionesReserva(reserva, rol, onEstado, onCorregir) }
                    }
                }
            }
        }
    }
}

@Composable
fun ReservaCard(
    reserva: Reserva,
    onAbrir: () -> Unit,
    modifier: Modifier = Modifier,
    acciones: @Composable () -> Unit = {}
) {
    Card(onClick = onAbrir, modifier = modifier.fillMaxWidth().animateContentSize()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(reserva.dependencia, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                EtiquetaEstado(reserva.estado)
            }
            DatosReserva(reserva)
            if (reserva.observacion.isNotBlank()) {
                Text("Observación: ${reserva.observacion}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
            acciones()
        }
    }
}

@Composable
private fun DatosReserva(reserva: Reserva) {
    FilaDato(Icons.Default.Person, reserva.solicitante + if (reserva.telefono.isNotBlank()) " · ${reserva.telefono}" else "")
    FilaDato(Icons.Default.Event, "${reserva.fecha} · ${reserva.horaInicio} - ${reserva.horaFin}")
    FilaDato(Icons.Default.Groups, "${reserva.personas} personas · ${reserva.monto.conversionPesos()}" + if (reserva.usaCocina) " · con cocina" else "")
    Text(reserva.motivo, style = MaterialTheme.typography.bodySmall)
}

/** Acciones permitidas segun el perfil y el estado. El backend valida las mismas reglas. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccionesReserva(
    reserva: Reserva,
    rol: Rol,
    onEstado: (Reserva, EstadoReserva, String) -> Unit,
    onCorregir: (Reserva) -> Unit
) {
    var dialogo by rememberSaveable { mutableStateOf<EstadoReserva?>(null) }

    if (rol.solicitaReservas && reserva.estado == EstadoReserva.REQUIERE_AJUSTES) {
        Button(onClick = { onCorregir(reserva) }) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Text(" Corregir solicitud")
        }
    }
    if (rol.resuelveReservas && !reserva.estado.finalizada) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (reserva.estado == EstadoReserva.PENDIENTE) {
                Button(onClick = { dialogo = EstadoReserva.CONFIRMADA }) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Text(" Confirmar")
                }
                BotonSecundario(Icons.Default.Edit, "Pedir ajustes") { dialogo = EstadoReserva.REQUIERE_AJUSTES }
            }
            if (reserva.estado != EstadoReserva.CONFIRMADA) {
                BotonSecundario(Icons.Default.Block, "Rechazar") { dialogo = EstadoReserva.RECHAZADA }
            }
            BotonSecundario(Icons.Default.Cancel, "Cancelar") { dialogo = EstadoReserva.CANCELADA }
        }
    }

    val cerrar = { dialogo = null }
    when (val estado = dialogo) {
        null -> Unit
        EstadoReserva.PENDIENTE -> Unit
        EstadoReserva.CONFIRMADA -> DialogoConfirmar(
            "Confirmar reserva", "${reserva.dependencia}, ${reserva.fecha} de ${reserva.horaInicio} a ${reserva.horaFin}.",
            "Confirmar", onAceptar = { onEstado(reserva, estado, ""); cerrar() }, onCerrar = cerrar
        )
        else -> DialogoTexto(
            titulo = when (estado) {
                EstadoReserva.REQUIERE_AJUSTES -> "Pedir ajustes"
                EstadoReserva.RECHAZADA -> "Rechazar solicitud"
                else -> "Cancelar reserva"
            },
            descripcion = if (estado == EstadoReserva.REQUIERE_AJUSTES) "El vecino podrá corregir su solicitud."
            else "Esta acción no se puede deshacer y libera el horario.",
            etiqueta = "Observación para el vecino",
            obligatorio = estado != EstadoReserva.CANCELADA,
            onAceptar = { onEstado(reserva, estado, it); cerrar() },
            onCerrar = cerrar
        )
    }
}

@Composable
private fun BotonSecundario(icono: ImageVector, texto: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) {
        Icon(icono, contentDescription = null)
        Text(" $texto")
    }
}

/** Estado completo de una reserva, observacion e instrucciones de pago */
@Composable
fun DetalleReservaScreen(
    reserva: Reserva?,
    rol: Rol,
    instruccionesPago: String?,
    cargando: Boolean = false,
    mensaje: String? = null,
    onMensajeMostrado: () -> Unit = {},
    onEstado: (Reserva, EstadoReserva, String) -> Unit = { _, _, _ -> },
    onObservar: (Reserva, String) -> Unit = { _, _ -> },
    onCorregir: (Reserva) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var editarObservacion by rememberSaveable { mutableStateOf(false) }

    PantallaBase("Detalle de la reserva", onBack, cargando = cargando, mensaje = mensaje, onMensajeMostrado = onMensajeMostrado) { padding ->
        if (reserva == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (cargando) "Cargando…" else "La reserva no está disponible.")
            }
            return@PantallaBase
        }
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(reserva.dependencia, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                EtiquetaEstado(reserva.estado)
            }
            DatosReserva(reserva)
            if (reserva.observacion.isNotBlank()) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Observación de la directiva", style = MaterialTheme.typography.labelLarge)
                        Text(reserva.observacion)
                    }
                }
            }
            if (rol.solicitaReservas && reserva.estado == EstadoReserva.CONFIRMADA && instruccionesPago != null) {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilaDato(Icons.Default.AttachMoney, "Instrucciones de pago")
                        Text(instruccionesPago)
                    }
                }
            }
            AccionesReserva(reserva, rol, onEstado, onCorregir)
            if (rol.resuelveReservas) {
                BotonSecundario(Icons.AutoMirrored.Filled.Comment, "Registrar observación") { editarObservacion = true }
            }
        }
        if (editarObservacion) {
            DialogoTexto(
                "Registrar observación", "El vecino podrá verla en su reserva.", "Observación",
                obligatorio = true, inicial = reserva.observacion,
                onAceptar = { onObservar(reserva, it); editarObservacion = false },
                onCerrar = { editarObservacion = false }
            )
        }
    }
}

@Preview(showBackground = true, name = "Reservas vecino")
@Composable
private fun ReservasPreview() {
    JuntaVecinosTheme { ReservasScreen(PreviewData.reservas, busqueda = "", rol = Rol.VECINO) }
}

@Preview(showBackground = true, widthDp = 820, heightDp = 380, name = "Reservas directiva horizontal")
@Composable
private fun ReservasHorizontalPreview() {
    JuntaVecinosTheme { ReservasScreen(PreviewData.reservas, busqueda = "", rol = Rol.DIRECTIVA) }
}
