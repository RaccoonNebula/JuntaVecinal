package com.gestion.juntavecinos.ui.preview

import com.gestion.juntavecinos.model.EstadoReserva
import com.gestion.juntavecinos.model.Movimiento
import com.gestion.juntavecinos.model.Reserva
import com.gestion.juntavecinos.model.TipoMovimiento
import com.gestion.juntavecinos.ui.state.ResumenFinanciero

/** Datos de ejemplo para @Preview */
object PreviewData {
    val reservas = listOf(
        Reserva(
            1, "María González", "912345678",
            "Salón multiuso", "24/10/2026",
            0L, "15:00", "20:00", 40,
            "Cumpleaños familiar", true, 60000,
            EstadoReserva.CONFIRMADA
        ),
        Reserva(
            2, "Pedro Soto", "987654321",
            "Sala de reuniones", "30/10/2026",
            0L, "10:00", "12:00", 15,
            "Reunión de pasaje", false, 20000,
            EstadoReserva.PENDIENTE),
        Reserva(
            3, "Ana Rojas", "955512345",
            "Patio techado", "02/11/2026",
            0L, "12:00", "18:00", 90,
            "Almuerzo comunitario", true, 80000,
            EstadoReserva.CANCELADA)
    )
    val movimientos = listOf(
        Movimiento(
            1,
            TipoMovimiento.INGRESO,
            "Arriendo de sede",
            "Pago arriendo salón (María G.)",
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