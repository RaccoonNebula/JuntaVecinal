package com.gestion.juntavecinos.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EstadoReserva(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    REQUIERE_AJUSTES("Requiere ajustes"),
    CONFIRMADA("Confirmada"),
    RECHAZADA("Rechazada"),
    CANCELADA("Cancelada");

    /** Estados finales: la reserva ya no ocupa horario. */
    val finalizada: Boolean get() = this == RECHAZADA || this == CANCELADA
}

enum class TipoMovimiento(val etiqueta: String) {
    INGRESO("Ingreso"),
    GASTO("Gasto")
}

/** Cache local de una reserva. El registro oficial esta en el backend. */
@Entity(tableName = "reservas")
data class Reserva(
    @PrimaryKey val id: Int = 0,
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
    val estado: EstadoReserva = EstadoReserva.PENDIENTE,
    val usuarioId: Int = 0,
    val dependenciaId: Int = 0,
    val observacion: String = ""
)

/** Cache local de un ingreso o gasto. comprobanteId identifica la foto guardada en el backend. */
@Entity(tableName = "movimientos")
data class Movimiento(
    @PrimaryKey val id: Int = 0,
    val tipo: TipoMovimiento,
    val categoria: String,
    val descripcion: String,
    val monto: Int,
    val fecha: String,
    val fechaMs: Long,
    val reservaId: Int? = null,
    val medioPago: String? = null,
    val referencia: String? = null,
    val comprobanteId: Int? = null
)

@Entity(tableName = "dependencias")
data class Dependencia(
    @PrimaryKey val id: Int = 0,
    val nombre: String,
    val capacidad: Int,
    val descripcion: String = "",
    val ubicacion: String = "",
    val activa: Boolean = true
)

data class Categoria(
    val id: Int = 0,
    val nombre: String,
    val tipo: TipoMovimiento,
    val descripcion: String = "",
    val activa: Boolean = true
)

data class Sede(
    val nombre: String = "",
    val direccion: String = "",
    val descripcion: String = "",
    val reglamento: String = "",
    val instruccionesPago: String = ""
)

data class Usuario(
    val id: Int = 0,
    val nombre: String,
    val email: String,
    val telefono: String,
    val rol: Rol,
    val activo: Boolean = true
)

data class Aviso(
    val id: Int,
    val tipo: String,
    val titulo: String,
    val mensaje: String,
    val reservaId: Int? = null,
    val movimientoId: Int? = null,
    val fechaCreacion: String,
    val leida: Boolean
)

data class Horario(val horaInicio: String, val horaFin: String)

data class EstadoEtiqueta(val codigo: String, val etiqueta: String)

data class Visibilidad(val mostrarResumen: Boolean = false, val mostrarMovimientos: Boolean = false)
