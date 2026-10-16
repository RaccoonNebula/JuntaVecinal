package com.gestion.juntavecinos.ui.state

import com.gestion.juntavecinos.model.TipoMovimiento
import com.gestion.juntavecinos.utils.FechaUtils

// Definicion de formulario Reserva y sus estados
data class ReservaErrores(
    val solicitante: String? = null,
    val telefono: String? = null,
    val dependencia: String? = null,
    val fecha: String? = null,
    val horaInicio: String? = null,
    val horaFin: String? = null,
    val personas: String? = null,
    val motivo: String? = null,
    val monto: String? = null,
    val reglamento: String? = null
) {
    fun hayErrores() = listOfNotNull(
        solicitante, telefono, dependencia, fecha, horaInicio, horaFin, personas, motivo, monto, reglamento
    ).isNotEmpty()
}

data class ReservaFormState(
    val solicitante: String = "",
    val telefono: String = "",
    val dependencia: String = "",
    val fecha: String = "",
    val horaInicio: String = "",
    val horaFin: String = "",
    val personas: String = "",
    val motivo: String = "",
    val monto: String = "",
    val usaCocina: Boolean = false,
    val aceptaReglamento: Boolean = false,
    val errores: ReservaErrores = ReservaErrores(),
    val guardadoOk: Boolean = false
)

/** Definicion de formulario movimiento (registro ingreso/gasto)
 * y su estado, ademas de un resumen de las finanzas */
data class MovimientoErrores(
    val categoria: String? = null,
    val descripcion: String? = null,
    val monto: String? = null,
    val fecha: String? = null
) {
    fun hayErrores() = listOfNotNull(categoria, descripcion, monto, fecha).isNotEmpty()
}

data class MovimientoFormState(
    val tipo: TipoMovimiento = TipoMovimiento.INGRESO,
    val categoria: String = "",
    val descripcion: String = "",
    val monto: String = "",
    val fecha: String = FechaUtils.fechaHoyTexto(),
    val comprobantePath: String? = null,
    val errores: MovimientoErrores = MovimientoErrores(),
    val guardadoOk: Boolean = false
)

data class ResumenFinanciero(val ingresos: Int = 0, val gastos: Int = 0) {
    val balance: Int get() = ingresos - gastos
}
