package com.gestion.juntavecinos.pruebas

import com.gestion.juntavecinos.data.remote.MovimientoDto
import com.gestion.juntavecinos.data.remote.ReservaDto
import com.gestion.juntavecinos.model.Categoria
import com.gestion.juntavecinos.model.Dependencia
import com.gestion.juntavecinos.model.EstadoReserva
import com.gestion.juntavecinos.model.Reserva
import com.gestion.juntavecinos.model.Rol
import com.gestion.juntavecinos.model.Sesion
import com.gestion.juntavecinos.model.TipoMovimiento
import com.gestion.juntavecinos.model.Usuario
import com.gestion.juntavecinos.utils.FechaUtils

/** Datos ficticios compartidos por las pruebas */
object DatosPrueba {

    /** Una fecha que siempre es futura, para que las pruebas no venzan */
    val fechaFutura: String = FechaUtils.textoDesdeMs(FechaUtils.fechaHoyMs() + 10 * FechaUtils.UN_DIA_MS)

    val salon = Dependencia(id = 1, nombre = "Salón", capacidad = 50)

    fun sesion(rol: Rol = Rol.VECINO, token: String = "token") =
        Sesion(1, "Vecina Demo", "vecina@zaror.test", "900000001", rol, token, Long.MAX_VALUE)

    fun usuario(rol: Rol = Rol.VECINO, activo: Boolean = true) =
        Usuario(1, "Vecina Demo", "vecina@zaror.test", "900000001", rol, activo)

    fun reserva(
        id: Int = 1,
        estado: EstadoReserva = EstadoReserva.PENDIENTE
    ) = Reserva(
        id = id, solicitante = "Vecina Demo", telefono = "900000001", dependencia = "Salón",
        fecha = fechaFutura, fechaMs = FechaUtils.conversionMs(fechaFutura)!!, horaInicio = "10:00", horaFin = "12:00",
        personas = 10, motivo = "Taller de prueba", usaCocina = false, monto = 20000,
        estado = estado, usuarioId = 1, dependenciaId = 1
    )

    fun reservaDto(id: Int = 1) = ReservaDto(
        id = id, usuarioId = 1, solicitante = "Vecina Demo", telefono = "900000001",
        dependenciaId = 1, dependencia = "Salón", fecha = "2026-12-15",
        horaInicio = "10:00", horaFin = "12:00", personas = 10, motivo = "Taller de prueba",
        usaCocina = false, monto = 20000, estado = EstadoReserva.PENDIENTE, observacion = ""
    )

    fun movimientoDto(id: Int = 1) = MovimientoDto(
        id = id, tipo = TipoMovimiento.INGRESO, categoria = "Cuota vecinal", descripcion = "Cuota de prueba",
        monto = 1000, fecha = "2026-11-20", reservaId = null, medioPago = null, referencia = null
    )

    val categoriaIngreso = Categoria(1, "Cuota vecinal", TipoMovimiento.INGRESO)
}
