package com.gestion.juntavecinos.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gestion.juntavecinos.logic.Validacion
import com.gestion.juntavecinos.model.Aviso
import com.gestion.juntavecinos.model.Dependencia
import com.gestion.juntavecinos.model.Horario
import com.gestion.juntavecinos.model.Sede
import com.gestion.juntavecinos.ui.component.BotonAnimado
import com.gestion.juntavecinos.ui.component.CampoFecha
import com.gestion.juntavecinos.ui.component.CampoSeleccion
import com.gestion.juntavecinos.ui.component.PantallaBase
import com.gestion.juntavecinos.ui.preview.PreviewData
import com.gestion.juntavecinos.ui.theme.JuntaVecinosTheme

/** Informacion general y disponibilidad, para todos los perfiles */
@Composable
fun SedeScreen(
    sede: Sede?,
    dependencias: List<Dependencia>,
    ocupados: List<Horario>?,
    cargando: Boolean = false,
    mensaje: String? = null,
    onMensajeMostrado: () -> Unit = {},
    onConsultar: (Dependencia, String) -> Unit = { _, _ -> },
    onCambio: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var elegida by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf("") }
    var errorDependencia by rememberSaveable { mutableStateOf<String?>(null) }
    var errorFecha by rememberSaveable { mutableStateOf<String?>(null) }

    PantallaBase("Información de la sede", onBack, cargando = cargando, mensaje = mensaje, onMensajeMostrado = onMensajeMostrado) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            sede?.let {
                Tarjeta(it.nombre) { Text(it.direccion); Text(it.descripcion) }
                Tarjeta("Reglamento de uso") { Text(it.reglamento) }
                Tarjeta("Instrucciones de pago") { Text(it.instruccionesPago) }
            }
            Text("Dependencias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            dependencias.forEach { d ->
                Tarjeta(d.nombre) {
                    Text("Capacidad: ${d.capacidad} personas")
                    if (d.descripcion.isNotBlank()) Text(d.descripcion)
                    if (d.ubicacion.isNotBlank()) Text("Ubicación: ${d.ubicacion}")
                }
            }
            Text("Consultar disponibilidad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            CampoSeleccion(
                elegida, dependencias.map { it.nombre }, { elegida = it; errorDependencia = null; onCambio() },
                "Dependencia", Icons.Default.MeetingRoom, errorDependencia
            )
            CampoFecha(fecha, { fecha = it; errorFecha = null; onCambio() }, "Fecha", errorFecha)
            BotonAnimado("Ver horarios", Icons.Default.Search, {
                val dependencia = dependencias.firstOrNull { it.nombre == elegida }
                errorDependencia = if (dependencia == null) "Selecciona una dependencia" else null
                errorFecha = Validacion.fechaReserva(fecha)
                if (dependencia != null && errorFecha == null) onConsultar(dependencia, fecha)
            }, Modifier.fillMaxWidth())
            ocupados?.let { lista ->
                Tarjeta(if (lista.isEmpty()) "Disponible de 08:00 a 23:00" else "Horarios ocupados") {
                    lista.forEach { Text("${it.horaInicio} - ${it.horaFin}") }
                }
            }
        }
    }
}

@Composable
private fun Tarjeta(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            contenido()
        }
    }
}

@Composable
fun AvisosScreen(
    avisos: List<Aviso>,
    cargando: Boolean = false,
    mensaje: String? = null,
    onMensajeMostrado: () -> Unit = {},
    onAbrir: (Aviso) -> Unit = {},
    onBack: () -> Unit = {}
) {
    PantallaBase("Notificaciones", onBack, cargando = cargando, mensaje = mensaje, onMensajeMostrado = onMensajeMostrado) { padding ->
        if (avisos.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (cargando) "Cargando…" else "No tienes notificaciones.")
            }
        } else {
            LazyColumn(
                Modifier.padding(padding).padding(horizontal = 16.dp).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(avisos, key = { it.id }) { a ->
                    Card(
                        onClick = { onAbrir(a) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = if (a.leida) CardDefaults.cardColors()
                        else CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (!a.leida) Badge { Text("Nueva") }
                            Text(a.titulo, fontWeight = FontWeight.Bold)
                            Text(a.mensaje, style = MaterialTheme.typography.bodyMedium)
                            Text(a.fechaCreacion, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SedePreview() {
    JuntaVecinosTheme {
        SedeScreen(
            Sede("Junta de Vecinos Zaror", "Dirección de demostración", "Datos ficticios", "Reglamento", "Instrucciones"),
            PreviewData.dependencias, listOf(Horario("10:00", "12:00"))
        )
    }
}
