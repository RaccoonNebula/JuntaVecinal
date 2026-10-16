package com.gestion.juntavecinos.ui.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gestion.juntavecinos.model.Catalogo
import com.gestion.juntavecinos.model.TipoMovimiento
import com.gestion.juntavecinos.ui.component.BotonAnimado
import com.gestion.juntavecinos.ui.component.CampoFecha
import com.gestion.juntavecinos.ui.component.CampoSeleccion
import com.gestion.juntavecinos.ui.component.CampoTexto
import com.gestion.juntavecinos.ui.component.MensajeError
import com.gestion.juntavecinos.ui.component.PantallaBase
import com.gestion.juntavecinos.ui.state.MovimientoErrores
import com.gestion.juntavecinos.ui.state.MovimientoFormState
import com.gestion.juntavecinos.ui.theme.JuntaVecinosTheme
import java.io.File

/** Misma agrupacion que en NuevaReservaScreen */
data class MovimientoFormActiones(
    val onTipo: (TipoMovimiento) -> Unit = {},
    val onCategoria: (String) -> Unit = {},
    val onDescripcion: (String) -> Unit = {},
    val onMonto: (String) -> Unit = {},
    val onFecha: (String) -> Unit = {},
    val onTomarFoto: () -> Unit = {},
    val onQuitarFoto: () -> Unit = {},
    val onGuardar: () -> Unit = {}
)

@Composable
fun NuevoMovimientoScreen(
    form: MovimientoFormState,
    mensajeCamara: String? = null,
    acciones: MovimientoFormActiones = MovimientoFormActiones(),
    onBack: () -> Unit = {}
) {
    val horizontal = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    PantallaBase(titulo = "Registrar pago o gasto", onBack = onBack) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (horizontal) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.weight(1f)) { DatosMovimiento(form, acciones) }
                    Column(Modifier.weight(1f)) { Comprobante(form, mensajeCamara, acciones) }
                }
            } else {
                DatosMovimiento(form, acciones)
                Comprobante(form, mensajeCamara, acciones)
            }
            BotonAnimado("Guardar movimiento", Icons.Default.Check, acciones.onGuardar, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun DatosMovimiento(form: MovimientoFormState, a: MovimientoFormActiones) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
        TipoMovimiento.entries.forEach { tipo ->
            FilterChip(
                selected = form.tipo == tipo,
                onClick = { a.onTipo(tipo) },
                label = { Text(tipo.etiqueta) },
                leadingIcon = if (form.tipo == tipo) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else null
            )
        }
    }
    val categorias = if (form.tipo == TipoMovimiento.INGRESO) Catalogo.categoriasIngreso else Catalogo.categoriasGasto

    CampoSeleccion(form.categoria, categorias,
        a.onCategoria, "Categoría", Icons.Default.Category, form.errores.categoria)

    CampoTexto(form.descripcion, a.onDescripcion,
        "Descripción", Icons.Default.Description, form.errores.descripcion, lineas = 2)

    CampoTexto(form.monto, a.onMonto,
        "Monto (CLP, ficticio)", Icons.Default.AttachMoney, form.errores.monto, teclado = KeyboardType.Number)
}

@Composable
private fun Comprobante(form: MovimientoFormState, mensajeCamara: String?, a: MovimientoFormActiones) {
    CampoFecha(form.fecha, a.onFecha, "Fecha del movimiento", form.errores.fecha)
    BotonAnimado(
        texto = if (form.comprobantePath == null) "Tomar foto del comprobante" else "Tomar otra foto",
        icono = Icons.Default.CameraAlt,
        onClick = a.onTomarFoto,
        modifier = Modifier.fillMaxWidth()
    )
    MensajeError(mensajeCamara)
    AnimatedVisibility(
        visible = form.comprobantePath != null,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Box(Modifier.padding(top = 8.dp)) {
            AsyncImage(
                model = form.comprobantePath?.let { File(it) },
                contentDescription = "Foto del comprobante",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().size(180.dp).clip(RoundedCornerShape(12.dp))
            )
            IconButton(onClick = a.onQuitarFoto, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(Icons.Default.Close, contentDescription = "Quitar foto")
            }
        }
    }
}

@Preview(showBackground = true, name = "Movimiento vacío")
@Composable
private fun NuevoMovimientoPreview() {
    JuntaVecinosTheme { NuevoMovimientoScreen(MovimientoFormState(fecha = "16/10/2026")) }
}

@Preview(showBackground = true, name = "Movimiento con errores")
@Composable
private fun NuevoMovimientoErroresPreview() {
    JuntaVecinosTheme() {
        NuevoMovimientoScreen(
            MovimientoFormState(
                tipo = TipoMovimiento.GASTO, fecha = "16/10/2026",
                errores = MovimientoErrores(
                    categoria = "Selecciona una categoría",
                    descripcion = "La descripción es obligatoria",
                    monto = "El monto es obligatorio"
                )
            ),
            mensajeCamara = "Sin permiso de cámara no se puede adjuntar el comprobante"
        )
    }
}

@Preview(showBackground = true, widthDp = 820, heightDp = 380, name = "Movimiento horizontal")
@Composable
private fun NuevoMovimientoHorizontalPreview() {
    JuntaVecinosTheme() { NuevoMovimientoScreen(MovimientoFormState(fecha = "16/10/2026")) }
}
