package com.gestion.juntavecinos.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gestion.juntavecinos.model.Catalogo
import com.gestion.juntavecinos.ui.component.BotonAnimado
import com.gestion.juntavecinos.ui.component.CampoFecha
import com.gestion.juntavecinos.ui.component.CampoSeleccion
import com.gestion.juntavecinos.ui.component.CampoTexto
import com.gestion.juntavecinos.ui.component.MensajeError
import com.gestion.juntavecinos.ui.component.PantallaBase
import com.gestion.juntavecinos.ui.state.ReservaErrores
import com.gestion.juntavecinos.ui.state.ReservaFormState
import com.gestion.juntavecinos.ui.theme.JuntaVecinosTheme

/** Agrupacion las acciones de formulario para
 * juntar los 12 parametros en la vista */
data class ReservaFormActiones(
    val onSolicitante: (String) -> Unit = {},
    val onTelefono: (String) -> Unit = {},
    val onDependencia: (String) -> Unit = {},
    val onFecha: (String) -> Unit = {},
    val onHoraInicio: (String) -> Unit = {},
    val onHoraFin: (String) -> Unit = {},
    val onPersonas: (String) -> Unit = {},
    val onMotivo: (String) -> Unit = {},
    val onMonto: (String) -> Unit = {},
    val onUsaCocina: (Boolean) -> Unit = {},
    val onReglamento: (Boolean) -> Unit = {},
    val onGuardar: () -> Unit = {}
)

@Composable
fun NuevaReservaScreen(
    form: ReservaFormState,
    acciones: ReservaFormActiones = ReservaFormActiones(),
    onBack: () -> Unit = {}
) {
    val horizontal = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    PantallaBase(titulo = "Nueva reserva", onBack = onBack) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (horizontal) {
                // Horizontal: dos columnas de campos
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.weight(1f)) { DatosSolicitante(form, acciones) }
                    Column(Modifier.weight(1f)) { DatosReserva(form, acciones) }
                }
            } else {
                DatosSolicitante(form, acciones)
                DatosReserva(form, acciones)
            }
            BotonAnimado(
                texto = "Guardar reserva",
                icono = Icons.Default.Check,
                onClick = acciones.onGuardar,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DatosSolicitante(form: ReservaFormState, a: ReservaFormActiones) {
    CampoTexto(form.solicitante, a.onSolicitante, "Nombre del solicitante",
        Icons.Default.Person, form.errores.solicitante)
    CampoTexto(form.telefono, a.onTelefono, "Teléfono (9 dígitos)",
        Icons.Default.Phone, form.errores.telefono, teclado = KeyboardType.Phone)
    CampoSeleccion(
        valor = form.dependencia,
        opciones = Catalogo.dependencias.map { it.nombre },
        onSeleccion = a.onDependencia,
        etiqueta = "Dependencia",
        icono = Icons.Default.MeetingRoom,
        error = form.errores.dependencia
    )
    CampoTexto(form.personas, a.onPersonas, "Cantidad de personas",
        Icons.Default.Groups, form.errores.personas, teclado = KeyboardType.Number)
    CampoTexto(form.motivo, a.onMotivo, "Motivo del arriendo",
        Icons.Default.Description, form.errores.motivo, lineas = 2)
}

@Composable
private fun DatosReserva(form: ReservaFormState, a: ReservaFormActiones) {
    CampoFecha(form.fecha, a.onFecha, "Fecha del arriendo", form.errores.fecha)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CampoSeleccion(form.horaInicio, Catalogo.horas, a.onHoraInicio,
            "Inicio", Icons.Default.Schedule, form.errores.horaInicio, Modifier.weight(1f))
        CampoSeleccion(form.horaFin, Catalogo.horas, a.onHoraFin,
            "Término", Icons.Default.Schedule, form.errores.horaFin, Modifier.weight(1f))
    }
    CampoTexto(form.monto, a.onMonto, "Monto acordado (CLP, ficticio)",
        Icons.Default.AttachMoney, form.errores.monto, teclado = KeyboardType.Number)

    // Switch
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Requiere uso de cocina", Modifier.weight(1f))
        Switch(checked = form.usaCocina, onCheckedChange = a.onUsaCocina)
    }
    // Checkbox
    Row(
        Modifier.fillMaxWidth().clickable { a.onReglamento(!form.aceptaReglamento) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = form.aceptaReglamento, onCheckedChange = a.onReglamento)
        Text("Acepto el reglamento de uso de la sede")
    }
    MensajeError(form.errores.reglamento)
}

@Preview(showBackground = true, name = "Formulario vacío")
@Composable
private fun NuevaReservaPreview() {
    JuntaVecinosTheme { NuevaReservaScreen(ReservaFormState()) }
}

@Preview(showBackground = true, name = "Formulario con errores")
@Composable
private fun NuevaReservaErroresPreview() {
    JuntaVecinosTheme {
        NuevaReservaScreen(
            ReservaFormState(
                solicitante = "A1", telefono = "123", personas = "500", dependencia = "Sala de reuniones",
                errores = ReservaErrores(
                    solicitante = "Solo se permiten letras y espacios",
                    telefono = "Debe tener 9 dígitos",
                    personas = "Capacidad máxima: 25 personas",
                    fecha = "Selecciona una fecha",
                    reglamento = "Debes aceptar el reglamento de uso"
                )
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 820, heightDp = 380, name = "Formulario horizontal")
@Composable
private fun NuevaReservaHorizontalPreview() {
    JuntaVecinosTheme { NuevaReservaScreen(ReservaFormState()) }
}
