package com.gestion.juntavecinos.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EstadoReserva(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    CANCELADA("Cancelada")
}

enum class TipoMovimiento(val etiqueta: String) {
    INGRESO("Ingreso"),
    GASTO("Gasto")
}

/** DB de reserva/arriendo de una dependencia de la junta */
@Entity(tableName = "reservas")
data class Reserva(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val solicitante: String,
    val telefono: String,
    val dependencia: String,
    val fecha: String,          // Recordar formato dd/MM/yyyy
    val fechaMs: Long,          // Recordar fechaMs > en milisegundos para buscar por fecha
    val horaInicio: String,     // Recordar formato HH:mm
    val horaFin: String,
    val personas: Int,
    val motivo: String,
    val usaCocina: Boolean,
    val monto: Int,
    val estado: EstadoReserva = EstadoReserva.PENDIENTE
)

/** DB de ingreso o gasto de la junta de vecinos. El comprobante de la
 * foto tomada con la cámara (se guardara la ruta de la foto) */
@Entity(tableName = "movimientos")
data class Movimiento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tipo: TipoMovimiento,
    val categoria: String,
    val descripcion: String,
    val monto: Int,
    val fecha: String,
    val fechaMs: Long,
    val comprobantePath: String? = null
)
