package com.gestion.juntavecinos.di

import android.content.Context
import coil.Coil
import com.gestion.juntavecinos.data.remote.ClienteApi
import com.gestion.juntavecinos.data.repository.AdminRepository
import com.gestion.juntavecinos.data.repository.AuthRepository
import com.gestion.juntavecinos.data.repository.AvisosRepository
import com.gestion.juntavecinos.data.repository.FinanzasRepository
import com.gestion.juntavecinos.data.repository.ReservasRepository
import com.gestion.juntavecinos.data.repository.SedeRepository
import com.gestion.juntavecinos.data.session.SesionManager
import com.gestion.juntavecinos.model.AppDatabase
import com.gestion.juntavecinos.utils.CamaraUtils
import com.gestion.juntavecinos.utils.NotificacionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Crea y comparte las dependencias de la aplicacion */
class Contenedor(contexto: Context) {

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val db = AppDatabase.getInstance(contexto)

    val sesiones = SesionManager(contexto, alcance)
    val http = ClienteApi.crearHttp(sesiones)
    private val api = ClienteApi.crear(http)

    val auth = AuthRepository(api, sesiones)
    val reservas = ReservasRepository(api, db.reservaDao(), sesiones)
    val finanzas = FinanzasRepository(api, db.movimientoDao(), sesiones)
    val sede = SedeRepository(api, db.dependenciaDao(), sesiones)
    val avisos = AvisosRepository(contexto, api, sesiones)
    val admin = AdminRepository(api)

    init {
        // Al cerrar sesion no quedan datos de la cuenta anterior en el dispositivo
        sesiones.alCerrar = {
            db.reservaDao().borrarTodas()
            db.movimientoDao().borrarTodos()
            db.dependenciaDao().borrarTodas()
            avisos.limpiar()
            CamaraUtils.borrarTodo(contexto)
            NotificacionHelper.cancelarTodas(contexto)
            Coil.imageLoader(contexto).memoryCache?.clear()
        }
        sesiones.cargar()
    }
}
