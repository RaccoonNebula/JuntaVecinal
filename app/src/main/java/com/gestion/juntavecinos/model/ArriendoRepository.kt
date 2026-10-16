package com.gestion.juntavecinos.model

/** El acceso a los datos. Conexion con viewmodel y los datos. */
class ArriendoRepository (db: AppDatabase){

    private val reservaDao = db.reservaDao()
    private val movimientoDao = db.movimientoDao()

    val reservas = reservaDao.observarTodas()
    val movimientos = movimientoDao.observarTodos()

    suspend fun insertarReserva(reserva: Reserva) = reservaDao.insertar(reserva)
    suspend fun actualizarReserva(reserva: Reserva) = reservaDao.actualizar(reserva)
    suspend fun reservasDelDia(fecha: String, dependencia: String) =
        reservaDao.buscarPorFechaYDependencia(fecha, dependencia)
    suspend fun confirmadasEntre(desde: Long, hasta: Long) = reservaDao.confirmadasEntre(desde, hasta)

    suspend fun insertarMovimiento(movimiento: Movimiento) = movimientoDao.insertar(movimiento)
    suspend fun eliminarMovimiento(movimiento: Movimiento) = movimientoDao.eliminar(movimiento)

}