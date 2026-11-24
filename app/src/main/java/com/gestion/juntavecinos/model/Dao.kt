package com.gestion.juntavecinos.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(reservas: List<Reserva>)

    @Query("DELETE FROM reservas")
    suspend fun borrarTodas()

    /** Deja la cache igual a lo que entrego el backend */
    @Transaction
    suspend fun reemplazar(reservas: List<Reserva>) {
        borrarTodas()
        insertarTodas(reservas)
    }

    // Para obtener las reservas
    @Query("SELECT * FROM reservas ORDER BY fechaMs ASC, horaInicio ASC")
    fun observarTodas(): Flow<List<Reserva>>

    // Para recordatorios (reservas confirmadas dentro de un rango de fechas)
    @Query("SELECT * FROM reservas WHERE estado = 'CONFIRMADA' AND fechaMs BETWEEN :desde AND :hasta")
    suspend fun confirmadasEntre(desde: Long, hasta: Long): List<Reserva>
}

@Dao
interface MovimientoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(movimientos: List<Movimiento>)

    @Query("DELETE FROM movimientos")
    suspend fun borrarTodos()

    @Transaction
    suspend fun reemplazar(movimientos: List<Movimiento>) {
        borrarTodos()
        insertarTodos(movimientos)
    }

    // Para obtener los movimientos
    @Query("SELECT * FROM movimientos ORDER BY fechaMs DESC, id DESC")
    fun observarTodos(): Flow<List<Movimiento>>
}

@Dao
interface DependenciaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(dependencias: List<Dependencia>)

    @Query("DELETE FROM dependencias")
    suspend fun borrarTodas()

    @Transaction
    suspend fun reemplazar(dependencias: List<Dependencia>) {
        borrarTodas()
        insertarTodas(dependencias)
    }

    @Query("SELECT * FROM dependencias ORDER BY nombre ASC")
    fun observarTodas(): Flow<List<Dependencia>>
}
