package com.gestion.juntavecinos.ui.screen.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gestion.juntavecinos.data.remote.CategoriaRequest
import com.gestion.juntavecinos.model.Categoria
import com.gestion.juntavecinos.model.EstadoEtiqueta
import com.gestion.juntavecinos.model.Sede
import com.gestion.juntavecinos.model.TipoMovimiento
import com.gestion.juntavecinos.ui.component.CampoTexto
import com.gestion.juntavecinos.ui.component.DialogoTexto
import com.gestion.juntavecinos.ui.component.MensajeError
import com.gestion.juntavecinos.ui.component.PantallaBase
import com.gestion.juntavecinos.ui.component.PropiedadesFormulario
import com.gestion.juntavecinos.viewmodel.AdminViewModel

/** Parametros basicos: informacion de la sede, categorias y nombres de los estados */
@Composable
fun CatalogosScreen(vm: AdminViewModel, onBack: () -> Unit) {
    val categorias by vm.categorias.collectAsStateWithLifecycle()
    val estados by vm.estados.collectAsStateWithLifecycle()
    val sede by vm.sede.collectAsStateWithLifecycle()
    val cargando by vm.cargando.collectAsStateWithLifecycle()
    val mensaje by vm.mensaje.collectAsStateWithLifecycle()
    var editarSede by remember { mutableStateOf(false) }
    var categoria by remember { mutableStateOf<Categoria?>(null) }
    var creando by remember { mutableStateOf(false) }
    var estado by remember { mutableStateOf<EstadoEtiqueta?>(null) }
    LaunchedEffect(Unit) { vm.cargarCatalogos() }

    val dialogoAbierto = editarSede || categoria != null || creando
    PantallaBase(
        "Catálogos y sede", onBack, cargando = cargando,
        mensaje = mensaje.takeUnless { dialogoAbierto }, onMensajeMostrado = vm::limpiarMensaje
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Titulo("Información de la sede")
            sede?.let { s ->
                Fila(s.nombre, s.direccion) { vm.limpiarMensaje(); editarSede = true }
            }
            Titulo("Categorías de movimientos")
            categorias.forEach { c ->
                Fila(c.nombre, "${c.tipo.etiqueta} · ${if (c.activa) "Activa" else "Inactiva"}") {
                    vm.limpiarMensaje(); categoria = c
                }
            }
            OutlinedButton(onClick = { vm.limpiarMensaje(); creando = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(" Nueva categoría")
            }
            Titulo("Estados de reserva")
            estados.forEach { e -> Fila(e.etiqueta, "Código: ${e.codigo}") { estado = e } }
        }
    }

    sede?.takeIf { editarSede }?.let { s ->
        DialogoSede(
            s, cargando, mensaje,
            onGuardar = { vm.limpiarMensaje(); vm.guardarSede(it) { editarSede = false } },
            onCerrar = { vm.limpiarMensaje(); editarSede = false }
        )
    }
    if (creando || categoria != null) {
        val actual = categoria
        DialogoCategoria(
            actual, cargando, mensaje,
            onGuardar = { vm.limpiarMensaje(); vm.guardarCategoria(actual?.id, it) { creando = false; categoria = null } },
            onCerrar = { vm.limpiarMensaje(); creando = false; categoria = null }
        )
    }
    estado?.let { e ->
        DialogoTexto(
            "Nombre del estado", "El código ${e.codigo} no cambia.", "Nombre visible", obligatorio = true,
            inicial = e.etiqueta, maximo = 40,
            onAceptar = { vm.guardarEstado(e.codigo, it) {}; estado = null }, onCerrar = { estado = null }
        )
    }
}

@Composable
private fun Titulo(texto: String) =
    Text(texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))

@Composable
private fun Fila(titulo: String, detalle: String, onEditar: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.SemiBold)
                Text(detalle, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onEditar) { Text("Modificar") }
        }
    }
}

@Composable
private fun DialogoCategoria(
    categoria: Categoria?, cargando: Boolean, error: String?, onGuardar: (CategoriaRequest) -> Unit, onCerrar: () -> Unit
) {
    var nombre by remember { mutableStateOf(categoria?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(categoria?.descripcion ?: "") }
    var tipo by remember { mutableStateOf(categoria?.tipo ?: TipoMovimiento.GASTO) }
    var activa by remember { mutableStateOf(categoria?.activa ?: true) }
    var intento by remember { mutableStateOf(false) }
    val errorNombre = if (nombre.trim().length < 3) "Debe tener al menos 3 caracteres" else null

    AlertDialog(
        onDismissRequest = { if (!cargando) onCerrar() },
        properties = PropiedadesFormulario,
        modifier = Modifier.imePadding(),
        title = { Text(if (categoria == null) "Nueva categoría" else "Modificar categoría") },
        text = {
            Column {
                Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                    if (categoria == null) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TipoMovimiento.entries.forEach { t ->
                            FilterChip(selected = tipo == t, onClick = { tipo = t }, label = { Text(t.etiqueta) })
                        }
                    } else Text("Tipo: ${tipo.etiqueta}")
                    CampoTexto(nombre, { nombre = it.take(60) }, "Nombre", Icons.Default.Category, errorNombre.takeIf { intento })
                    CampoTexto(descripcion, { descripcion = it.take(255) }, "Descripción (opcional)", Icons.Default.Description, null)
                    Row(Modifier.fillMaxWidth().clickable { activa = !activa }, verticalAlignment = Alignment.CenterVertically) {
                        Text("Activa", Modifier.weight(1f))
                        Switch(checked = activa, onCheckedChange = { activa = it })
                    }
                }
                MensajeError(error)
            }
        },
        confirmButton = {
            TextButton(enabled = !cargando, onClick = {
                intento = true
                if (errorNombre == null) onGuardar(CategoriaRequest(nombre.trim(), tipo, descripcion.trim(), activa))
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(enabled = !cargando, onClick = onCerrar) { Text("Cancelar") } }
    )
}

@Composable
private fun DialogoSede(sede: Sede, cargando: Boolean, error: String?, onGuardar: (Sede) -> Unit, onCerrar: () -> Unit) {
    var s by remember { mutableStateOf(sede) }
    var intento by remember { mutableStateOf(false) }
    fun falta(v: String) = if (intento && v.isBlank()) "Este campo es obligatorio" else null

    AlertDialog(
        onDismissRequest = { if (!cargando) onCerrar() },
        properties = PropiedadesFormulario,
        modifier = Modifier.imePadding(),
        title = { Text("Información de la sede") },
        text = {
            Column {
                Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                    CampoTexto(s.nombre, { s = s.copy(nombre = it.take(120)) }, "Nombre", Icons.Default.Home, falta(s.nombre))
                    CampoTexto(s.direccion, { s = s.copy(direccion = it.take(200)) }, "Dirección", Icons.Default.LocationOn, falta(s.direccion))
                    CampoTexto(s.descripcion, { s = s.copy(descripcion = it.take(1000)) }, "Descripción", Icons.Default.Description, falta(s.descripcion), lineas = 3)
                    CampoTexto(s.reglamento, { s = s.copy(reglamento = it.take(1000)) }, "Reglamento", Icons.Default.Gavel, falta(s.reglamento), lineas = 3)
                    CampoTexto(s.instruccionesPago, { s = s.copy(instruccionesPago = it.take(1000)) }, "Instrucciones de pago",
                        Icons.Default.AttachMoney, falta(s.instruccionesPago), lineas = 3)
                }
                MensajeError(error)
            }
        },
        confirmButton = {
            TextButton(enabled = !cargando, onClick = {
                intento = true
                if (listOf(s.nombre, s.direccion, s.descripcion, s.reglamento, s.instruccionesPago).none { it.isBlank() }) onGuardar(s)
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(enabled = !cargando, onClick = onCerrar) { Text("Cancelar") } }
    )
}
