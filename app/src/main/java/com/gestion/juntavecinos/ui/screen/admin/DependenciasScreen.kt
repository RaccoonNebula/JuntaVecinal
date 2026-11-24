package com.gestion.juntavecinos.ui.screen.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gestion.juntavecinos.data.remote.DependenciaRequest
import com.gestion.juntavecinos.model.Dependencia
import com.gestion.juntavecinos.ui.component.CampoTexto
import com.gestion.juntavecinos.ui.component.DialogoConfirmar
import com.gestion.juntavecinos.ui.component.MensajeError
import com.gestion.juntavecinos.ui.component.PantallaBase
import com.gestion.juntavecinos.ui.component.PropiedadesFormulario
import com.gestion.juntavecinos.viewmodel.AdminViewModel

/** Salones y espacios: crear, modificar y activar/desactivar */
@Composable
fun DependenciasScreen(vm: AdminViewModel, onBack: () -> Unit) {
    val dependencias by vm.dependencias.collectAsStateWithLifecycle()
    val cargando by vm.cargando.collectAsStateWithLifecycle()
    val mensaje by vm.mensaje.collectAsStateWithLifecycle()
    var editando by remember { mutableStateOf<Dependencia?>(null) }
    var creando by remember { mutableStateOf(false) }
    var cambiando by remember { mutableStateOf<Dependencia?>(null) }
    LaunchedEffect(Unit) { vm.cargarDependencias() }

    PantallaBase(
        "Dependencias", onBack, cargando = cargando,
        mensaje = mensaje.takeUnless { creando || editando != null }, onMensajeMostrado = vm::limpiarMensaje,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { vm.limpiarMensaje(); creando = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Nueva dependencia") },
                text = { Text("Nueva dependencia") }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).padding(horizontal = 16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
        ) {
            items(dependencias, key = { it.id }) { d ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(d.nombre, fontWeight = FontWeight.Bold)
                        Text("Capacidad: ${d.capacidad} personas")
                        if (d.descripcion.isNotBlank()) Text(d.descripcion, style = MaterialTheme.typography.bodySmall)
                        if (d.ubicacion.isNotBlank()) Text(d.ubicacion, style = MaterialTheme.typography.bodySmall)
                        Text(
                            if (d.activa) "Activa" else "Inactiva",
                            color = if (d.activa) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Row {
                            TextButton(onClick = { vm.limpiarMensaje(); editando = d }) { Text("Modificar") }
                            TextButton(onClick = { cambiando = d }) { Text(if (d.activa) "Desactivar" else "Activar") }
                        }
                    }
                }
            }
        }
    }

    if (creando || editando != null) {
        val actual = editando
        DialogoDependencia(
            actual, cargando, mensaje,
            onGuardar = {
                vm.limpiarMensaje()
                vm.guardarDependencia(actual?.id, it) { creando = false; editando = null }
            },
            onCerrar = { vm.limpiarMensaje(); creando = false; editando = null }
        )
    }
    cambiando?.let { d ->
        DialogoConfirmar(
            if (d.activa) "Desactivar dependencia" else "Activar dependencia",
            if (d.activa) "${d.nombre} no admitirá reservas nuevas. Las existentes se conservan."
            else "${d.nombre} volverá a estar disponible.",
            if (d.activa) "Desactivar" else "Activar",
            onAceptar = { vm.cambiarActiva(d); cambiando = null },
            onCerrar = { cambiando = null }
        )
    }
}

@Composable
private fun DialogoDependencia(
    dependencia: Dependencia?,
    cargando: Boolean,
    error: String?,
    onGuardar: (DependenciaRequest) -> Unit,
    onCerrar: () -> Unit
) {
    var nombre by remember { mutableStateOf(dependencia?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(dependencia?.descripcion ?: "") }
    var ubicacion by remember { mutableStateOf(dependencia?.ubicacion ?: "") }
    var capacidad by remember { mutableStateOf(dependencia?.capacidad?.toString() ?: "") }
    var intento by remember { mutableStateOf(false) }

    val errorNombre = if (nombre.trim().length < 3) "Debe tener al menos 3 caracteres" else null
    val valor = capacidad.toIntOrNull() ?: 0
    val errorCapacidad = if (valor !in 1..5000) "Debe estar entre 1 y 5000" else null

    AlertDialog(
        onDismissRequest = { if (!cargando) onCerrar() },
        properties = PropiedadesFormulario,
        modifier = Modifier.imePadding(),
        title = { Text(if (dependencia == null) "Nueva dependencia" else "Modificar dependencia") },
        text = {
            Column {
                Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                    CampoTexto(nombre, { nombre = it.take(80) }, "Nombre", Icons.Default.MeetingRoom, errorNombre.takeIf { intento })
                    CampoTexto(capacidad, { v -> capacidad = v.filter { it.isDigit() }.take(4) }, "Capacidad",
                        Icons.Default.Groups, errorCapacidad.takeIf { intento }, teclado = KeyboardType.Number)
                    CampoTexto(descripcion, { descripcion = it.take(255) }, "Descripción (opcional)", Icons.Default.Description, null, lineas = 2)
                    CampoTexto(ubicacion, { ubicacion = it.take(120) }, "Ubicación (opcional)", Icons.Default.LocationOn, null)
                }
                MensajeError(error)
            }
        },
        confirmButton = {
            TextButton(enabled = !cargando, onClick = {
                intento = true
                if (errorNombre == null && errorCapacidad == null) {
                    onGuardar(DependenciaRequest(nombre.trim(), descripcion.trim(), ubicacion.trim(), valor))
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(enabled = !cargando, onClick = onCerrar) { Text("Cancelar") } }
    )
}
