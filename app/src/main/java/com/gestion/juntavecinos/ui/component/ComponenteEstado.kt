package com.gestion.juntavecinos.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.gestion.juntavecinos.model.EstadoReserva

/** Nombres visibles de los estados, definidos por el administrador */
val LocalEtiquetas = compositionLocalOf<Map<EstadoReserva, String>> { emptyMap() }

@Composable
fun EstadoReserva.nombreVisible(): String = LocalEtiquetas.current[this] ?: etiqueta

@Composable
fun EtiquetaEstado(estado: EstadoReserva) {
    val colorObjetivo = when (estado) {
        EstadoReserva.PENDIENTE -> Color(0xFFF9A825)
        EstadoReserva.REQUIERE_AJUSTES -> Color(0xFFEF6C00)
        EstadoReserva.CONFIRMADA -> Color(0xFF2E7D32)
        EstadoReserva.RECHAZADA, EstadoReserva.CANCELADA -> MaterialTheme.colorScheme.error
    }
    // Animacion para etiqueta (dependiendo del estado)
    val color by animateColorAsState(colorObjetivo, tween(500), label = "colorEstado")
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(50)) {
        Text(
            estado.nombreVisible(),
            color = color,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun FilaDato(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text("  $texto", style = MaterialTheme.typography.bodyMedium)
    }
}

/** Contraseña oculta, con opcion de mostrarla */
@Composable
fun CampoPassword(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    error: String?,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }
        },
        isError = error != null,
        supportingText = { MensajeError(error) },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        modifier = modifier
    )
}

/** Formularios en dialogo: no se pierden al tocar fuera y dejan espacio para el teclado */
val PropiedadesFormulario = DialogProperties(dismissOnClickOutside = false, decorFitsSystemWindows = false)

/** Dialogo con un texto, por ejemplo la observacion de una reserva */
@Composable
fun DialogoTexto(
    titulo: String,
    descripcion: String,
    etiqueta: String,
    obligatorio: Boolean,
    inicial: String = "",
    maximo: Int = 500,
    onAceptar: (String) -> Unit,
    onCerrar: () -> Unit
) {
    var texto by rememberSaveable { mutableStateOf(inicial) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onCerrar,
        properties = PropiedadesFormulario,
        modifier = Modifier.imePadding(),
        title = { Text(titulo) },
        text = {
            Column {
                if (descripcion.isNotEmpty()) Text(descripcion)
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it.take(maximo); error = null },
                    label = { Text(etiqueta) },
                    isError = error != null,
                    supportingText = { MensajeError(error) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (obligatorio && texto.isBlank()) error = "Este campo es obligatorio" else onAceptar(texto.trim())
            }) { Text("Aceptar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cerrar") } }
    )
}

@Composable
fun DialogoConfirmar(titulo: String, texto: String, accion: String, onAceptar: () -> Unit, onCerrar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(titulo) },
        text = { Text(texto) },
        confirmButton = { TextButton(onClick = onAceptar) { Text(accion) } },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}
