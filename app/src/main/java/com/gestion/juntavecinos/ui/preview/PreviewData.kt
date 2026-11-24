package com.gestion.juntavecinos.ui.preview

import com.gestion.juntavecinos.model.Dependencia
import com.gestion.juntavecinos.model.EstadoReserva
import com.gestion.juntavecinos.model.Movimiento
import com.gestion.juntavecinos.model.Reserva
import com.gestion.juntavecinos.model.TipoMovimiento
import com.gestion.juntavecinos.ui.state.ResumenFinanciero

/** Datos ficticios para @Preview */
object PreviewData {
    val dependencias = listOf(
        Dependencia(1, "Salón multiuso", 80),
        Dependencia(2, "Sala de reuniones", 25),
        Dependencia(3, "Patio techado", 120)
    )
    val reservas = listOf(
        Reserva(
            1, "Vecina Ejemplo", "900000001",
            "Salón multiuso", "24/10/2026",
            0L, "15:00", "20:00", 40,
            "Cumpleaños familiar", true, 60000,
            EstadoReserva.CONFIRMADA
        ),
        Reserva(
            2, "Vecino Ejemplo", "900000002",
            "Sala de reuniones", "30/10/2026",
            0L, "10:00", "12:00", 15,
            "Reunión de pasaje", false, 20000,
            EstadoReserva.PENDIENTE),
        Reserva(
            3, "Vecina Demo", "900000003",
            "Patio techado", "02/11/2026",
            0L, "12:00", "18:00", 90,
            "Almuerzo comunitario", true, 80000,
            EstadoReserva.REQUIERE_AJUSTES, observacion = "Reducir el horario")
    )
    val movimientos = listOf(
        Movimiento(
            1,
            TipoMovimiento.INGRESO,
            "Arriendo de sede",
            "Pago arriendo salón",
            60000,
            "24/10/2026",
            0L
        ),
        Movimiento(
            2,
            TipoMovimiento.GASTO,
            "Luz",
            "Cuenta de luz octubre",
            35000,
            "10/10/2026",
            0L),
        Movimiento(
            3,
            TipoMovimiento.GASTO,
            "Aseo",
            "Productos de limpieza",
            12500,
            "05/10/2026",
            0L)
    )
    val resumen = ResumenFinanciero(ingresos = 60000, gastos = 47500)
}
