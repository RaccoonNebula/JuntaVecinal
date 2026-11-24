package com.gestion.juntavecinos.data.repository

import com.gestion.juntavecinos.data.remote.ApiService
import com.gestion.juntavecinos.data.remote.Resultado
import com.gestion.juntavecinos.data.remote.llamar
import com.gestion.juntavecinos.data.session.SesionManager
import com.gestion.juntavecinos.model.DependenciaDao
import com.gestion.juntavecinos.model.Sede

class SedeRepository(
    private val api: ApiService,
    private val dao: DependenciaDao,
    private val sesiones: SesionManager
) {
    val dependencias = dao.observarTodas()

    suspend fun refrescar(): Resultado<Unit> {
        val sesion = sesiones.sesion ?: return Resultado.Exito(Unit)
        return llamar {
            val lista = api.dependencias()
            if (sesiones.token == sesion.token) dao.reemplazar(lista)
        }
    }

    suspend fun sede(): Resultado<Sede> = llamar { api.sede() }
}
