package com.gestion.juntavecinos.ui.screen

import android.content.Intent
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gestion.juntavecinos.data.remote.FinanzasPublicasDto
import com.gestion.juntavecinos.data.remote.InformeDto
import com.gestion.juntavecinos.model.TipoMovimiento
import com.gestion.juntavecinos.model.Visibilidad
import com.gestion.juntavecinos.ui.component.BotonAnimado
import com.gestion.juntavecinos.ui.component.CampoFecha
import com.gestion.juntavecinos.ui.component.PantallaBase
import com.gestion.juntavecinos.ui.state.ResumenFinanciero
import com.gestion.juntavecinos.utils.FechaUtils
import com.gestion.juntavecinos.utils.conversionPesos

/** Vecino: solo lo que la directiva autorizo, sin descripciones ni comprobantes */
@Composable
fun FinanzasPublicasScreen(
    publicas: FinanzasPublicasDto?,
    cargando: Boolean,
    mensaje: String?,
    onMensajeMostrado: () -> Unit,
    onBack: () -> Unit
) {
    PantallaBase("Finanzas de la junta", onBack, cargando = cargando, mensaje = mensaje, onMensajeMostrado = onMensajeMostrado) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (publicas == null) return@Column
            if (!publicas.mostrarResumen && !publicas.mostrarMovimientos) {
                Text("La directiva no ha autorizado información financiera para mostrar.")
            }
            publicas.resumen?.let { ResumenCard(ResumenFinanciero(it.ingresos, it.gastos)) }
            if (publicas.mostrarMovimientos) {
                Text("Movimientos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (publicas.movimientos.isEmpty()) Text("Aún no hay movimientos registrados.")
                publicas.movimientos.forEach { m ->
                    FilaMonto(m.categoria, FechaUtils.desdeIso(m.fecha), m.tipo, m.monto.toLong())
                }
            }
        }
    }
}

@Composable
private fun FilaMonto(titulo: String, detalle: String, tipo: TipoMovimiento, monto: Long) {
    val esIngreso = tipo == TipoMovimiento.INGRESO
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.SemiBold)
                Text(detalle, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                (if (esIngreso) "+" else "-") + monto.conversionPesos(),
                color = if (esIngreso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Directiva: define que informacion financiera pueden ver los vecinos */
@Composable
fun VisibilidadScreen(
    visibilidad: Visibilidad?,
    cargando: Boolean,
    mensaje: String?,
    onMensajeMostrado: () -> Unit,
    onCambio: (Visibilidad) -> Unit,
    onBack: () -> Unit
) {
    PantallaBase("Finanzas visibles", onBack, cargando = cargando, mensaje = mensaje, onMensajeMostrado = onMensajeMostrado) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Define qué información pueden consultar los vecinos. Nunca se muestran datos personales, descripciones ni comprobantes.")
            visibilidad?.let { v ->
                FilaSwitch("Mostrar resumen (ingresos, gastos y balance)", v.mostrarResumen, !cargando) {
                    onCambio(v.copy(mostrarResumen = it))
                }
                FilaSwitch("Mostrar listado de movimientos por categoría", v.mostrarMovimientos, !cargando) {
                    onCambio(v.copy(mostrarMovimientos = it))
                }
            }
        }
    }
}

@Composable
private fun FilaSwitch(texto: String, valor: Boolean, habilitado: Boolean, onCambio: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(enabled = habilitado) { onCambio(!valor) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(texto, Modifier.weight(1f))
        Switch(checked = valor, onCheckedChange = onCambio, enabled = habilitado)
    }
}

/** Tesoreria: informe por periodo calculado por el backend desde los movimientos vigentes */
@Composable
fun InformesScreen(
    informe: InformeDto?,
    cargando: Boolean,
    mensaje: String?,
    onMensajeMostrado: () -> Unit,
    onGenerar: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val contexto = LocalContext.current
    var desde by rememberSaveable { mutableStateOf("01" + FechaUtils.fechaHoyTexto().drop(2)) }
    var hasta by rememberSaveable { mutableStateOf(FechaUtils.fechaHoyTexto()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    PantallaBase("Informes y rendiciones", onBack, cargando = cargando, mensaje = mensaje, onMensajeMostrado = onMensajeMostrado) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CampoFecha(desde, { desde = it; error = null }, "Desde", error)
            CampoFecha(hasta, { hasta = it; error = null }, "Hasta", null)
            BotonAnimado("Generar informe", Icons.Default.Assessment, {
                val inicio = FechaUtils.conversionMs(desde)
                val fin = FechaUtils.conversionMs(hasta)
                if (inicio == null || fin == null || inicio > fin) error = "La fecha inicial no puede ser posterior a la final"
                else onGenerar(desde, hasta)
            }, Modifier.fillMaxWidth())

            informe?.let { i ->
                ResumenCard(ResumenFinanciero(i.resumen.ingresos, i.resumen.gastos))
                Text("Totales por categoría", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (i.categorias.isEmpty()) Text("No hay movimientos en el período.")
                i.categorias.forEach {
                    FilaMonto(it.categoria, "${it.tipo.etiqueta} · ${it.cantidad} movimiento(s)", it.tipo, it.total)
                }
                BotonAnimado("Compartir informe", Icons.Default.Share, {
                    val envio = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, textoInforme(i))
                    }
                    contexto.startActivity(Intent.createChooser(envio, "Compartir informe"))
                }, Modifier.fillMaxWidth())
            }
        }
    }
}

private fun textoInforme(i: InformeDto) = buildString {
    appendLine("Junta de Vecinos Zaror - Informe financiero (datos ficticios)")
    appendLine("Período: ${FechaUtils.desdeIso(i.desde)} al ${FechaUtils.desdeIso(i.hasta)}")
    appendLine("Ingresos: ${i.resumen.ingresos.conversionPesos()}")
    appendLine("Gastos: ${i.resumen.gastos.conversionPesos()}")
    appendLine("Balance: ${i.resumen.balance.conversionPesos()}")
    appendLine()
    i.categorias.forEach { appendLine("${it.tipo.etiqueta} - ${it.categoria}: ${it.total.conversionPesos()} (${it.cantidad})") }
}
