package com.gestion.juntavecinos.viewmodel

import androidx.lifecycle.viewModelScope
import com.gestion.juntavecinos.data.remote.Resultado
import com.gestion.juntavecinos.data.repository.AvisosRepository
import com.gestion.juntavecinos.data.repository.ReservasRepository
import com.gestion.juntavecinos.data.repository.SedeRepository
import com.gestion.juntavecinos.model.Dependencia
import com.gestion.juntavecinos.model.Horario
import com.gestion.juntavecinos.model.Sede
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Informacion de la sede, disponibilidad y avisos: comun a todos los perfiles */
class SedeViewModel(
    private val repo: SedeRepository,
    private val reservas: ReservasRepository,
    private val avisosRepo: AvisosRepository
) : BaseViewModel() {

    private val mutableSede = MutableStateFlow<Sede?>(null)
    val sede: StateFlow<Sede?> = mutableSede.asStateFlow()

    val dependencias: StateFlow<List<Dependencia>> = repo.dependencias
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** null mientras no se consulta */
    private val mutableOcupados = MutableStateFlow<List<Horario>?>(null)
    val ocupados: StateFlow<List<Horario>?> = mutableOcupados.asStateFlow()

    val avisos = avisosRepo.avisos

    fun refrescar() {
        ejecutar({ repo.sede() }) { mutableSede.value = it }
        ejecutar({ repo.refrescar() })
    }

    fun consultarDisponibilidad(dependencia: Dependencia, fecha: String) {
        mutableOcupados.value = null
        ejecutar({ reservas.disponibilidad(dependencia.id, fecha) }) { mutableOcupados.value = it }
    }

    fun limpiarDisponibilidad() { mutableOcupados.value = null }

    /** Silencioso: los avisos se consultan al abrir pantallas */
    fun refrescarAvisos() { viewModelScope.launch { avisosRepo.refrescar() } }

    fun refrescarAvisosConError() = ejecutar({ avisosRepo.refrescar() })

    fun marcarLeido(id: Int) {
        viewModelScope.launch {
            val r = avisosRepo.marcarLeido(id)
            if (r is Resultado.Error) mostrar(r.mensaje)
        }
    }
}
