package com.gestion.juntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.gestion.juntavecinos.data.remote.ReservaRequest
import com.gestion.juntavecinos.data.remote.Resultado
import com.gestion.juntavecinos.data.repository.AvisosRepository
import com.gestion.juntavecinos.data.repository.ReservasRepository
import com.gestion.juntavecinos.data.repository.SedeRepository
import com.gestion.juntavecinos.logic.Validacion
import com.gestion.juntavecinos.model.Dependencia
import com.gestion.juntavecinos.model.EstadoReserva
import com.gestion.juntavecinos.model.Reserva
import com.gestion.juntavecinos.model.Sesion
import com.gestion.juntavecinos.ui.state.ReservaFormState
import com.gestion.juntavecinos.utils.FechaUtils
import com.gestion.juntavecinos.utils.NotificacionHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReservasViewModel(
    private val app: Application,
    private val repo: ReservasRepository,
    private val sedeRepo: SedeRepository,
    private val avisos: AvisosRepository,
    private val sesion: Sesion
) : BaseViewModel() {

    val reservas: StateFlow<List<Reserva>> = repo.reservas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dependencias: StateFlow<List<Dependencia>> = sedeRepo.dependencias
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Nombre visible de cada estado, administrado desde el backend */
    private val mutableEtiquetas = MutableStateFlow<Map<EstadoReserva, String>>(emptyMap())
    val etiquetas: StateFlow<Map<EstadoReserva, String>> = mutableEtiquetas.asStateFlow()

    private val mutableBusqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = mutableBusqueda.asStateFlow()

    /** Filtro para la busqueda de reservas */
    val reservasFiltradas: StateFlow<List<Reserva>> = combine(reservas, mutableBusqueda) { lista, texto ->
        val q = texto.trim()
        if (q.isEmpty()) lista
        else lista.filter {
            it.solicitante.contains(q, ignoreCase = true) || it.dependencia.contains(q, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Los proximas reservas/arriendos (no canceladas ni rechazadas)
     * Se mostrara en la pantalla inicio */
    val proximas: StateFlow<List<Reserva>> = reservas.map { lista ->
        lista.filter { !it.estado.finalizada && it.fechaMs >= FechaUtils.fechaHoyMs() }.take(3)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onBusqueda(texto: String) { mutableBusqueda.value = texto }

    fun refrescar() {
        ejecutar({ sedeRepo.refrescar() })
        ejecutar({ repo.estados() }) { mutableEtiquetas.value = it }
        ejecutar({ repo.refrescar() }) { revisarRecordatorios() }
    }


    /** Formulario */
    private val mutableForm = MutableStateFlow(nuevoFormulario())
    val form: StateFlow<ReservaFormState> = mutableForm.asStateFlow()

    private fun nuevoFormulario() = ReservaFormState(solicitante = sesion.nombre, telefono = sesion.telefono)

    private fun dependenciaElegida(nombre: String) = dependencias.value.firstOrNull { it.nombre == nombre }
    private fun capacidadDe(nombre: String) = dependenciaElegida(nombre)?.capacidad ?: 0

    fun onSolicitante(v: String) = mutableForm.update {
        it.copy(solicitante = v, errores = it.errores.copy(solicitante = Validacion.solicitante(v)))
    }

    fun onTelefono(v: String) {
        val soloDigitos = v.filter { it.isDigit() }.take(9)
        mutableForm.update { it.copy(telefono = soloDigitos, errores = it.errores.copy(telefono = Validacion.telefono(soloDigitos))) }
    }

    fun onDependencia(v: String) {
        mutableForm.update {
            val errorPersonas = if (it.personas.isNotEmpty()) Validacion.personas(it.personas, capacidadDe(v)) else it.errores.personas
            it.copy(dependencia = v, errores = it.errores.copy(dependencia = Validacion.dependencia(v), personas = errorPersonas))
        }
        consultarDisponibilidad()
    }

    fun onFecha(v: String) {
        mutableForm.update {
            it.copy(fecha = v, errores = it.errores.copy(fecha = Validacion.fechaReserva(v)))
        }
        consultarDisponibilidad()
    }

    fun onHoraInicio(v: String) = mutableForm.update {
        val errorFin = if (it.horaFin.isNotEmpty()) Validacion.horaFin(v, it.horaFin) else it.errores.horaFin
        it.copy(horaInicio = v, errores = it.errores.copy(horaInicio = Validacion.horaInicio(v), horaFin = errorFin))
    }

    fun onHoraFin(v: String) = mutableForm.update {
        it.copy(horaFin = v, errores = it.errores.copy(horaFin = Validacion.horaFin(it.horaInicio, v)))
    }

    fun onPersonas(v: String) {
        val soloDigitos = v.filter { it.isDigit() }.take(4)
        mutableForm.update { it.copy(personas = soloDigitos, errores = it.errores.copy(personas = Validacion.personas(soloDigitos, capacidadDe(it.dependencia)))) }
    }

    fun onMotivo(v: String) = mutableForm.update {
        it.copy(motivo = v, errores = it.errores.copy(motivo = Validacion.motivo(v)))
    }

    fun onMonto(v: String) {
        val soloDigitos = v.filter { it.isDigit() }.take(8)
        mutableForm.update { it.copy(monto = soloDigitos, errores = it.errores.copy(monto = Validacion.monto(soloDigitos))) }
    }

    fun onUsaCocina(v: Boolean) = mutableForm.update { it.copy(usaCocina = v) }

    fun onReglamento(v: Boolean) = mutableForm.update {
        it.copy(aceptaReglamento = v, errores = it.errores.copy(reglamento = Validacion.reglamento(v)))
    }

    fun limpiarFormulario() {
        consulta?.cancel()
        mutableForm.value = nuevoFormulario()
    }

    /** Carga una solicitud propia que requiere ajustes para corregirla */
    fun corregir(reserva: Reserva) {
        mutableForm.value = ReservaFormState(
            solicitante = reserva.solicitante,
            telefono = reserva.telefono,
            dependencia = reserva.dependencia,
            fecha = reserva.fecha,
            horaInicio = reserva.horaInicio,
            horaFin = reserva.horaFin,
            personas = reserva.personas.toString(),
            motivo = reserva.motivo,
            monto = reserva.monto.toString(),
            usaCocina = reserva.usaCocina,
            corrigiendoId = reserva.id
        )
        consultarDisponibilidad()
    }

    private var consulta: Job? = null

    /** Muestra las horas ocupadas (sin datos personales) de la dependencia y fecha elegidas */
    private fun consultarDisponibilidad() {
        consulta?.cancel()
        mutableForm.update { it.copy(ocupados = emptyList()) }
        val f = mutableForm.value
        val dependencia = dependenciaElegida(f.dependencia) ?: return
        if (Validacion.fechaReserva(f.fecha) != null) return
        consulta = viewModelScope.launch {
            val r = repo.disponibilidad(dependencia.id, f.fecha)
            if (r is Resultado.Exito) {
                // Al corregir, el horario propio aparece como ocupado y no es un conflicto
                val propia = reservaPropia(f.corrigiendoId)
                val ocupados = if (propia != null && propia.fecha == f.fecha && propia.dependenciaId == dependencia.id)
                    r.dato.filterNot { it.horaInicio == propia.horaInicio && it.horaFin == propia.horaFin }
                else r.dato
                mutableForm.update { it.copy(ocupados = ocupados) }
            }
        }
    }

    private fun reservaPropia(id: Int?) = id?.let { buscado -> reservas.value.firstOrNull { it.id == buscado } }

    fun guardar() {
        val f = mutableForm.value
        if (f.enviando) return
        val dependencia = dependenciaElegida(f.dependencia)
        val errores = Validacion.validarReserva(f, dependencia?.capacidad ?: 0).let {
            if (it.dependencia == null && dependencia == null) it.copy(dependencia = "La dependencia ya no está disponible") else it
        }
        if (errores.hayErrores() || dependencia == null) {
            mutableForm.update { it.copy(errores = errores, errorGeneral = "Revisa los campos marcados") }
            return
        }
        /** Validacion temprana para reservas duplicadas */
        if (Validacion.hayConflicto(f.horaInicio, f.horaFin, f.ocupados)) {
            mutableForm.update {
                it.copy(errores = errores.copy(horaInicio = "La dependencia ya está reservada en ese horario"))
            }
            return
        }
        val request = ReservaRequest(
            solicitante = f.solicitante.trim(),
            telefono = f.telefono,
            dependenciaId = dependencia.id,
            fecha = FechaUtils.aIso(f.fecha),
            horaInicio = f.horaInicio,
            horaFin = f.horaFin,
            personas = f.personas.toInt(),
            motivo = f.motivo.trim(),
            usaCocina = f.usaCocina,
            monto = f.monto.toInt(),
            aceptaReglamento = f.aceptaReglamento
        )
        mutableForm.update { it.copy(enviando = true, errorGeneral = null, errores = errores) }
        viewModelScope.launch {
            val r = if (f.corrigiendoId == null) repo.crear(request) else repo.corregir(f.corrigiendoId, request)
            when (r) {
                is Resultado.Exito -> {
                    mutableForm.update { it.copy(enviando = false, guardadoOk = true) }
                    avisos.refrescar()
                }
                is Resultado.Error -> {
                    mutableForm.update { it.copy(enviando = false, errorGeneral = r.mensaje) }
                    if (r.codigo == 409) consultarDisponibilidad()
                }
            }
        }
    }


    /** Metodos de cosas que la directiva puede hacer sobre una reserva */
    fun cambiarEstado(reserva: Reserva, nuevo: EstadoReserva, observacion: String = "") {
        ejecutar({ repo.cambiarEstado(reserva.id, nuevo, observacion) }) {
            val etiqueta = etiquetas.value[nuevo] ?: nuevo.etiqueta
            mostrar("Reserva: ${etiqueta.lowercase()}")
            NotificacionHelper.notificar(
                app, reserva.id, "Reserva: ${etiqueta.lowercase()}",
                "${reserva.dependencia} del ${reserva.fecha}, de ${reserva.horaInicio} a ${reserva.horaFin}."
            )
        }
    }

    fun observar(reserva: Reserva, observacion: String) {
        ejecutar({ repo.observar(reserva.id, observacion) }) { mostrar("Observación guardada") }
    }

    //Recordatorio que avisa de los arriendos confirmados de hoy y mañana (solo los visibles para el perfil)
    private suspend fun revisarRecordatorios() {
        val desde = FechaUtils.fechaHoyMs()
        val hasta = desde + 2 * FechaUtils.UN_DIA_MS - 1
        repo.confirmadasEntre(desde, hasta).forEach {
            NotificacionHelper.notificar(
                app, 10_000 + it.id,
                "Recordatorio de arriendo",
                "${it.dependencia}: ${it.fecha} a las ${it.horaInicio} (${it.solicitante})."
            )
        }
    }

}
