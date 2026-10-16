package com.gestion.juntavecinos.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservaDao {
    @Insert
    suspend fun insertar(reserva: Reserva): Long

    @Update
    suspend fun actualizar(reserva: Reserva)

    // Para obtener las reservas
    @Query("SELECT * FROM reservas ORDER BY fechaMs ASC, horaInicio ASC")
    fun observarTodas(): Flow<List<Reserva>>

    // Para revisar disponibilidad (reservas no canceladas de esa dependencia y día)
    @Query("SELECT * FROM reservas WHERE fecha = :fecha AND dependencia = :dependencia AND estado != 'CANCELADA'")
    suspend fun buscarPorFechaYDependencia(fecha: String, dependencia: String): List<Reserva>

    // Para recordatorios (reservas confirmadas dentro de un rango de fechas)
    @Query("SELECT * FROM reservas WHERE estado = 'CONFIRMADA' AND fechaMs BETWEEN :desde AND :hasta")
    suspend fun confirmadasEntre(desde: Long, hasta: Long): List<Reserva>
}

@Dao
interface MovimientoDao {
    @Insert
    suspend fun insertar(movimiento: Movimiento): Long

    @Delete
    suspend fun eliminar(movimiento: Movimiento)

    // Para obtener los movimientos
    @Query("SELECT * FROM movimientos ORDER BY fechaMs DESC, id DESC")
    fun observarTodos(): Flow<List<Movimiento>>
}
