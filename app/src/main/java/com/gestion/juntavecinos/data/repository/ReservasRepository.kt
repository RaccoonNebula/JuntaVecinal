package com.gestion.juntavecinos.data.repository

import com.gestion.juntavecinos.data.remote.ApiService
import com.gestion.juntavecinos.data.remote.CambioEstadoRequest
import com.gestion.juntavecinos.data.remote.ObservacionRequest
import com.gestion.juntavecinos.data.remote.ReservaDto
import com.gestion.juntavecinos.data.remote.ReservaRequest
import com.gestion.juntavecinos.data.remote.Resultado
import com.gestion.juntavecinos.data.remote.llamar
import com.gestion.juntavecinos.data.session.SesionManager
import com.gestion.juntavecinos.model.EstadoReserva
import com.gestion.juntavecinos.model.Horario
import com.gestion.juntavecinos.model.ReservaDao
import com.gestion.juntavecinos.model.Rol
import com.gestion.juntavecinos.utils.FechaUtils

/** El backend es la fuente oficial; Room solo guarda la ultima consulta */
class ReservasRepository(
    private val api: ApiService,
    private val dao: ReservaDao,
    private val sesiones: SesionManager
) {
    val reservas = dao.observarTodas()

    suspend fun refrescar(): Resultado<Unit> {
        val sesion = sesiones.sesion ?: return Resultado.Exito(Unit)
        return llamar {
            val lista = when (sesion.rol) {
                Rol.VECINO -> api.misReservas()
                Rol.DIRECTIVA -> api.reservas()
                Rol.TESORERIA -> api.reservasConfirmadas()
                Rol.ADMIN -> emptyList()
            }
            // Si la sesion cambio mientras tanto, no se guardan datos de otro usuario
            if (sesiones.token == sesion.token) dao.reemplazar(lista.map { it.aModelo() })
        }
    }

    suspend fun crear(request: ReservaRequest): Resultado<ReservaDto> = conRefresco { api.crearReserva(request) }

    suspend fun corregir(id: Int, request: ReservaRequest): Resultado<ReservaDto> =
        conRefresco { api.corregirReserva(id, request) }

    suspend fun cambiarEstado(id: Int, estado: EstadoReserva, observacion: String): Resultado<ReservaDto> =
        conRefresco { api.cambiarEstado(id, CambioEstadoRequest(estado, observacion)) }

    suspend fun observar(id: Int, observacion: String): Resultado<ReservaDto> =
        conRefresco { api.observar(id, ObservacionRequest(observacion)) }

    suspend fun disponibilidad(dependenciaId: Int, fecha: String): Resultado<List<Horario>> =
        llamar { api.disponibilidad(dependenciaId, FechaUtils.aIso(fecha)) }

    suspend fun estados(): Resultado<Map<EstadoReserva, String>> = llamar {
        api.estados().mapNotNull { e ->
            EstadoReserva.entries.firstOrNull { it.name == e.codigo }?.let { it to e.etiqueta }
        }.toMap()
    }

    suspend fun confirmadasEntre(desde: Long, hasta: Long) = dao.confirmadasEntre(desde, hasta)

    private suspend fun <T> conRefresco(bloque: suspend () -> T): Resultado<T> =
        llamar(bloque).also { refrescar() }
}
