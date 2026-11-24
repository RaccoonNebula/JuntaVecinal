package com.gestion.juntavecinos.data.remote

import com.gestion.juntavecinos.model.EstadoReserva
import com.gestion.juntavecinos.model.Movimiento
import com.gestion.juntavecinos.model.Reserva
import com.gestion.juntavecinos.model.Rol
import com.gestion.juntavecinos.model.TipoMovimiento
import com.gestion.juntavecinos.model.Usuario
import com.gestion.juntavecinos.utils.FechaUtils

// Las fechas viajan en formato ISO (yyyy-MM-dd) y en la app se muestran como dd/MM/yyyy

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(val status: String, val token: String, val expiraEn: Long, val usuario: Usuario)

data class RegistroRequest(val nombre: String, val email: String, val telefono: String, val password: String)

data class ReservaDto(
    val id: Int,
    val usuarioId: Int,
    val solicitante: String,
    val telefono: String,
    val dependenciaId: Int,
    val dependencia: String,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val personas: Int,
    val motivo: String,
    val usaCocina: Boolean,
    val monto: Int,
    val estado: EstadoReserva,
    val observacion: String
) {
    fun aModelo() = Reserva(
        id = id,
        solicitante = solicitante,
        telefono = telefono,
        dependencia = dependencia,
        fecha = FechaUtils.desdeIso(fecha),
        fechaMs = FechaUtils.conversionMs(FechaUtils.desdeIso(fecha)) ?: 0L,
        horaInicio = horaInicio,
        horaFin = horaFin,
        personas = personas,
        motivo = motivo,
        usaCocina = usaCocina,
        monto = monto,
        estado = estado,
        usuarioId = usuarioId,
        dependenciaId = dependenciaId,
        observacion = observacion
    )
}

data class ReservaRequest(
    val solicitante: String,
    val telefono: String,
    val dependenciaId: Int,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val personas: Int,
    val motivo: String,
    val usaCocina: Boolean,
    val monto: Int,
    val aceptaReglamento: Boolean
)

data class CambioEstadoRequest(val estado: EstadoReserva, val observacion: String)

data class ObservacionRequest(val observacion: String)

data class MovimientoDto(
    val id: Int,
    val tipo: TipoMovimiento,
    val categoria: String,
    val descripcion: String,
    val monto: Int,
    val fecha: String,
    val reservaId: Int?,
    val medioPago: String?,
    val referencia: String?
) {
    fun aModelo(comprobanteId: Int? = null) = Movimiento(
        id = id,
        tipo = tipo,
        categoria = categoria,
        descripcion = descripcion,
        monto = monto,
        fecha = FechaUtils.desdeIso(fecha),
        fechaMs = FechaUtils.conversionMs(FechaUtils.desdeIso(fecha)) ?: 0L,
        reservaId = reservaId,
        medioPago = medioPago,
        referencia = referencia,
        comprobanteId = comprobanteId
    )
}

data class MovimientoRequest(
    val tipo: TipoMovimiento,
    val categoriaId: Int,
    val descripcion: String,
    val monto: Int,
    val fecha: String,
    val reservaId: Int?,
    val medioPago: String?,
    val referencia: String?
)

data class ResumenDto(val ingresos: Long, val gastos: Long, val balance: Long)

/** Finanzas para vecinos: sin descripcion, reserva ni comprobante */
data class MovimientoPublicoDto(
    val id: Int,
    val tipo: TipoMovimiento,
    val categoria: String,
    val monto: Int,
    val fecha: String
)

data class FinanzasPublicasDto(
    val mostrarResumen: Boolean,
    val mostrarMovimientos: Boolean,
    val resumen: ResumenDto?,
    val movimientos: List<MovimientoPublicoDto>
)

data class TotalCategoriaDto(val tipo: TipoMovimiento, val categoria: String, val total: Long, val cantidad: Int)

data class InformeDto(
    val desde: String,
    val hasta: String,
    val resumen: ResumenDto,
    val categorias: List<TotalCategoriaDto>,
    val movimientos: List<MovimientoDto>
)

data class ComprobanteDto(val id: Int, val movimientoId: Int)

data class CrearUsuarioRequest(
    val nombre: String, val email: String, val telefono: String, val password: String, val rol: Rol
)

data class EditarUsuarioRequest(val nombre: String, val email: String, val telefono: String, val rol: Rol)

data class ActivoRequest(val activo: Boolean)

data class ContrasenaRequest(val password: String)

data class DependenciaRequest(
    val nombre: String, val descripcion: String, val ubicacion: String, val capacidad: Int
)

data class ActivaRequest(val activa: Boolean)

data class CategoriaRequest(
    val nombre: String, val tipo: TipoMovimiento, val descripcion: String, val activa: Boolean
)

data class EtiquetaRequest(val etiqueta: String)

/** Cuerpo de error comun de todos los microservicios */
data class ApiErrorDto(val message: String?)
