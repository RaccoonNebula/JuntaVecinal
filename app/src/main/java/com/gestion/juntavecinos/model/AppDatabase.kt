package com.gestion.juntavecinos.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Cache local. Desde la version 2 los datos oficiales estan en el backend. */
@Database(
    entities = [Reserva::class, Movimiento::class, Dependencia::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun reservaDao(): ReservaDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun dependenciaDao(): DependenciaDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "junta_vecinos.db"
                ).fallbackToDestructiveMigration(true).build().also { instancia = it }
            }
    }

}
