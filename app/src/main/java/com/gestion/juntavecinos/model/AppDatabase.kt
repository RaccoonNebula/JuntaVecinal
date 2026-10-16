package com.gestion.juntavecinos.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Reserva::class, Movimiento::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun reservaDao(): ReservaDao
    abstract fun movimientoDao(): MovimientoDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "junta_vecinos.db"
                ).build().also { instancia = it }
            }
    }

}
