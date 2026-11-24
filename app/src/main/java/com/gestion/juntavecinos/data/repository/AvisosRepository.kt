package com.gestion.juntavecinos.data.repository

import android.content.Context
import com.gestion.juntavecinos.data.remote.ApiService
import com.gestion.juntavecinos.data.remote.Resultado
import com.gestion.juntavecinos.data.remote.llamar
import com.gestion.juntavecinos.data.session.SesionManager
import com.gestion.juntavecinos.model.Aviso
import com.gestion.juntavecinos.utils.NotificacionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Avisos del backend. Los nuevos tambien se muestran como notificacion del dispositivo. */
class AvisosRepository(
    private val contexto: Context,
    private val api: ApiService,
    private val sesiones: SesionManager
) {
    private val mutableAvisos = MutableStateFlow<List<Aviso>>(emptyList())
    val avisos: StateFlow<List<Aviso>> = mutableAvisos.asStateFlow()

    private val mutex = Mutex()

    suspend fun refrescar(): Resultado<Unit> = mutex.withLock {
        val sesion = sesiones.sesion ?: return Resultado.Exito(Unit)
        llamar {
            val lista = api.notificaciones()
            if (sesiones.token == sesion.token) {
                mutableAvisos.value = lista
                notificarNuevos(sesion.id, lista)
            }
        }
    }

    suspend fun marcarLeido(id: Int): Resultado<Unit> {
        val r = llamar { api.marcarLeida(id) }
        if (r is Resultado.Exito) {
            mutableAvisos.value = mutableAvisos.value.map { if (it.id == id) it.copy(leida = true) else it }
        }
        return r
    }

    fun limpiar() { mutableAvisos.value = emptyList() }

    private suspend fun notificarNuevos(usuarioId: Int, lista: List<Aviso>) {
        val ultimo = sesiones.ultimoAviso(usuarioId)
        val maximo = lista.maxOfOrNull { it.id } ?: return
        if (maximo <= ultimo) return
        lista.filter { it.id > ultimo && !it.leida }.take(5).reversed().forEach {
            NotificacionHelper.notificar(contexto, 30_000 + it.id, it.titulo, it.mensaje)
        }
        sesiones.guardarUltimoAviso(usuarioId, maximo)
    }
}
