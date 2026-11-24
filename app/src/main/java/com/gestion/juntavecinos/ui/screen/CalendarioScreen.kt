package com.gestion.juntavecinos.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gestion.juntavecinos.model.Reserva
import com.gestion.juntavecinos.ui.component.PantallaBase
import com.gestion.juntavecinos.ui.preview.PreviewData
import com.gestion.juntavecinos.ui.theme.JuntaVecinosTheme
import com.gestion.juntavecinos.utils.FechaUtils
import java.util.Calendar

private val meses = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio",
    "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

/** Calendario mensual con las reservas que ocupan horario */
@Composable
fun CalendarioScreen(
    reservas: List<Reserva>,
    cargando: Boolean = false,
    onAbrir: (Reserva) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var desplazamiento by rememberSaveable { mutableIntStateOf(0) }
    var seleccion by rememberSaveable { mutableStateOf(FechaUtils.fechaHoyTexto()) }

    val mes = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MONTH, desplazamiento)
    }
    val anio = mes.get(Calendar.YEAR)
    val numeroMes = mes.get(Calendar.MONTH)
    val dias = mes.getActualMaximum(Calendar.DAY_OF_MONTH)
    val vacios = (mes.get(Calendar.DAY_OF_WEEK) + 5) % 7   // semana desde el lunes
    val activas = reservas.filter { !it.estado.finalizada }
    val ocupados = activas.map { it.fecha }.toSet()

    PantallaBase("Calendario", onBack, cargando = cargando) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { desplazamiento-- }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior")
                }
                Text(
                    "${meses[numeroMes]} $anio", Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { desplazamiento++ }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente")
                }
            }
            Row {
                listOf("L", "M", "M", "J", "V", "S", "D").forEach {
                    Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                }
            }
            (List(vacios) { 0 } + (1..dias)).chunked(7).forEach { semana ->
                Row {
                    semana.forEach { dia ->
                        if (dia == 0) Spacer(Modifier.weight(1f))
                        else {
                            val fecha = "%02d/%02d/%04d".format(dia, numeroMes + 1, anio)
                            Dia(dia, fecha, fecha == seleccion, fecha in ocupados, Modifier.weight(1f)) { seleccion = fecha }
                        }
                    }
                    repeat(7 - semana.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            Text("Reservas del $seleccion", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            val delDia = activas.filter { it.fecha == seleccion }
            if (delDia.isEmpty()) Text("Sin reservas: todas las dependencias están disponibles.")
            delDia.forEach { r -> ReservaCard(r, onAbrir = { onAbrir(r) }) }
        }
    }
}

@Composable
private fun Dia(dia: Int, fecha: String, elegido: Boolean, ocupado: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Column(
        modifier.height(48.dp).clip(CircleShape)
            .background(if (elegido) colores.primary else colores.surface)
            .clickable(onClick = onClick)
            .semantics { contentDescription = fecha + if (ocupado) ", con reservas" else ", sin reservas" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("$dia", color = if (elegido) colores.onPrimary else colores.onSurface)
        Box(
            Modifier.size(6.dp).clip(CircleShape)
                .background(if (!ocupado) colores.surface.copy(alpha = 0f) else if (elegido) colores.onPrimary else colores.primary)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarioPreview() {
    JuntaVecinosTheme { CalendarioScreen(PreviewData.reservas) }
}
