package com.gestion.juntavecinos.ui.screen.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
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
import com.gestion.juntavecinos.logic.Validacion
import com.gestion.juntavecinos.model.Rol
import com.gestion.juntavecinos.model.Usuario
import com.gestion.juntavecinos.ui.component.CampoPassword
import com.gestion.juntavecinos.ui.component.CampoSeleccion
import com.gestion.juntavecinos.ui.component.CampoTexto
import com.gestion.juntavecinos.ui.component.DialogoConfirmar
import com.gestion.juntavecinos.ui.component.MensajeError
import com.gestion.juntavecinos.ui.component.PantallaBase
import com.gestion.juntavecinos.ui.component.PropiedadesFormulario
import com.gestion.juntavecinos.viewmodel.AdminViewModel

/** Cuentas de acceso: crear, modificar, activar/desactivar y restablecer contraseña */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UsuariosScreen(vm: AdminViewModel, propioId: Int, onBack: () -> Unit) {
    val usuarios by vm.usuarios.collectAsStateWithLifecycle()
    val cargando by vm.cargando.collectAsStateWithLifecycle()
    val mensaje by vm.mensaje.collectAsStateWithLifecycle()
    var editando by remember { mutableStateOf<Usuario?>(null) }
    var creando by remember { mutableStateOf(false) }
    var cambiando by remember { mutableStateOf<Usuario?>(null) }
    var restableciendo by remember { mutableStateOf<Usuario?>(null) }
    LaunchedEffect(Unit) { vm.cargarUsuarios() }

    val dialogoAbierto = creando || editando != null || restableciendo != null
    PantallaBase(
        "Usuarios", onBack, cargando = cargando,
        mensaje = mensaje.takeUnless { dialogoAbierto }, onMensajeMostrado = vm::limpiarMensaje,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { vm.limpiarMensaje(); creando = true },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Nuevo usuario") },
                text = { Text("Nuevo usuario") }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).padding(horizontal = 16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
        ) {
            items(usuarios, key = { it.id }) { u ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(u.nombre + if (u.id == propioId) " (tú)" else "", fontWeight = FontWeight.Bold)
                        Text("${u.email} · ${u.telefono}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${u.rol.etiqueta} · ${if (u.activo) "Activo" else "Inactivo"}",
                            color = if (u.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge
                        )
                        FlowRow {
                            TextButton(onClick = { vm.limpiarMensaje(); editando = u }) { Text("Modificar") }
                            TextButton(onClick = { vm.limpiarMensaje(); restableciendo = u }) { Text("Contraseña") }
                            if (u.id != propioId) TextButton(onClick = { cambiando = u }) {
                                Text(if (u.activo) "Desactivar" else "Activar")
                            }
                        }
                    }
                }
            }
        }
    }

    if (creando || editando != null) {
        val actual = editando
        DialogoUsuario(
            usuario = actual,
            esPropio = actual?.id == propioId,
            cargando = cargando,
            error = mensaje,
            onGuardar = { u, password ->
                vm.limpiarMensaje()
                vm.guardarUsuario(actual?.id, u, password) { creando = false; editando = null }
            },
            onCerrar = { vm.limpiarMensaje(); creando = false; editando = null }
        )
    }
    cambiando?.let { u ->
        DialogoConfirmar(
            if (u.activo) "Desactivar usuario" else "Activar usuario",
            if (u.activo) "${u.nombre} no podrá iniciar sesión." else "${u.nombre} podrá iniciar sesión nuevamente.",
            if (u.activo) "Desactivar" else "Activar",
            onAceptar = { vm.cambiarActivo(u); cambiando = null },
            onCerrar = { cambiando = null }
        )
    }
    restableciendo?.let { u ->
        DialogoContrasena(
            u, cargando, mensaje,
            onGuardar = { vm.limpiarMensaje(); vm.cambiarContrasena(u.id, it) { restableciendo = null } },
            onCerrar = { vm.limpiarMensaje(); restableciendo = null }
        )
    }
}

@Composable
private fun DialogoUsuario(
    usuario: Usuario?,
    esPropio: Boolean,
    cargando: Boolean,
    error: String?,
    onGuardar: (Usuario, String) -> Unit,
    onCerrar: () -> Unit
) {
    var nombre by remember { mutableStateOf(usuario?.nombre ?: "") }
    var email by remember { mutableStateOf(usuario?.email ?: "") }
    var telefono by remember { mutableStateOf(usuario?.telefono ?: "") }
    var rol by remember { mutableStateOf(usuario?.rol ?: Rol.VECINO) }
    var password by remember { mutableStateOf("") }
    var intento by remember { mutableStateOf(false) }

    val errorNombre = Validacion.solicitante(nombre)
    val errorEmail = Validacion.email(email)
    val errorTelefono = Validacion.telefono(telefono)
    val errorPassword = if (usuario == null) Validacion.password(password) else null
    val hayErrores = listOfNotNull(errorNombre, errorEmail, errorTelefono, errorPassword).isNotEmpty()

    AlertDialog(
        onDismissRequest = { if (!cargando) onCerrar() },
        properties = PropiedadesFormulario,
        modifier = Modifier.imePadding(),
        title = { Text(if (usuario == null) "Nuevo usuario" else "Modificar usuario") },
        text = {
            Column {
                Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                    CampoTexto(nombre, { nombre = it }, "Nombre", Icons.Default.Person, errorNombre.takeIf { intento })
                    CampoTexto(email, { email = it.trim() }, "Correo", Icons.Default.Email, errorEmail.takeIf { intento }, teclado = KeyboardType.Email)
                    CampoTexto(telefono, { v -> telefono = v.filter { it.isDigit() }.take(9) }, "Teléfono (9 dígitos)",
                        Icons.Default.Phone, errorTelefono.takeIf { intento }, teclado = KeyboardType.Phone)
                    if (esPropio) Text("Perfil: ${rol.etiqueta}")
                    else CampoSeleccion(
                        rol.etiqueta, Rol.entries.map { it.etiqueta },
                        { elegido -> rol = Rol.entries.first { it.etiqueta == elegido } },
                        "Perfil", Icons.Default.Badge, null
                    )
                    if (usuario == null) CampoPassword(password, { password = it }, "Contraseña (mínimo 8)", errorPassword.takeIf { intento })
                }
                MensajeError(error)
            }
        },
        confirmButton = {
            TextButton(enabled = !cargando, onClick = {
                intento = true
                if (!hayErrores) onGuardar(Usuario(usuario?.id ?: 0, nombre, email, telefono, rol), password)
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(enabled = !cargando, onClick = onCerrar) { Text("Cancelar") } }
    )
}

@Composable
private fun DialogoContrasena(
    usuario: Usuario, cargando: Boolean, error: String?, onGuardar: (String) -> Unit, onCerrar: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var repetir by remember { mutableStateOf("") }
    var intento by remember { mutableStateOf(false) }
    val errorPassword = Validacion.password(password)
    val errorRepetir = Validacion.repetir(password, repetir)

    AlertDialog(
        onDismissRequest = { if (!cargando) onCerrar() },
        properties = PropiedadesFormulario,
        modifier = Modifier.imePadding(),
        title = { Text("Restablecer contraseña") },
        text = {
            Column {
                Text("Nueva contraseña para ${usuario.nombre}.")
                CampoPassword(password, { password = it }, "Contraseña (mínimo 8)", errorPassword.takeIf { intento })
                CampoPassword(repetir, { repetir = it }, "Repetir contraseña", errorRepetir.takeIf { intento })
                MensajeError(error)
            }
        },
        confirmButton = {
            TextButton(enabled = !cargando, onClick = {
                intento = true
                if (errorPassword == null && errorRepetir == null) onGuardar(password)
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(enabled = !cargando, onClick = onCerrar) { Text("Cancelar") } }
    )
}
