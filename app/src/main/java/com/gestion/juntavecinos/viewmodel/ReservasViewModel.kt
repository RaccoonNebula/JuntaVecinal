package com.gestion.juntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.juntavecinos.logic.Validacion
import com.gestion.juntavecinos.model.AppDatabase
import com.gestion.juntavecinos.model.ArriendoRepository
import com.gestion.juntavecinos.model.EstadoReserva
import com.gestion.juntavecinos.model.Reserva
import com.gestion.juntavecinos.ui.state.ReservaFormState
import com.gestion.juntavecinos.utils.FechaUtils
import com.gestion.juntavecinos.utils.NotificacionHelper
import com.gestion.juntavecinos.utils.conversionPesos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.text.toInt

class ReservasViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ArriendoRepository(AppDatabase.getInstance(app))

    private val reservasTodas = repo.reservas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val mutableBusqueda = MutableStateFlow("")

    val busqueda: StateFlow<String> = mutableBusqueda.asStateFlow()


    /** Filtro para la busqueda de reservas */
    val reservasFiltradas: StateFlow<List<Reserva>> = combine(reservasTodas, mutableBusqueda) { lista, texto ->
        val q = texto.trim()
        if (q.isEmpty()) lista
        else lista.filter {
            it.solicitante.contains(q, ignoreCase = true) || it.dependencia.contains(q, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())



    /** Los proximas reservas/arriendos (no canceladas)
     * Se mostrara en la pantalla inicio */
    val proximas: StateFlow<List<Reserva>> = reservasTodas.map { lista ->
        lista.filter { it.estado != EstadoReserva.CANCELADA && it.fechaMs >= FechaUtils.fechaHoyMs() }.take(3)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onBusqueda(texto: String) { mutableBusqueda.value = texto }


    /** Formulario */
    private val mutableForm = MutableStateFlow(ReservaFormState())
    val form: StateFlow<ReservaFormState> = mutableForm.asStateFlow()

    fun onSolicitante(v: String) = mutableForm.update {
        it.copy(solicitante = v, errores = it.errores.copy(solicitante = Validacion.solicitante(v)))
    }

    fun onTelefono(v: String) {
        val soloDigitos = v.filter { it.isDigit() }.take(9)
        mutableForm.update { it.copy(telefono = soloDigitos, errores = it.errores.copy(telefono = Validacion.telefono(soloDigitos))) }
    }

    fun onDependencia(v: String) = mutableForm.update {
        val errorPersonas = if (it.personas.isNotEmpty()) Validacion.personas(it.personas, v) else it.errores.personas
        it.copy(dependencia = v, errores = it.errores.copy(dependencia = Validacion.dependencia(v), personas = errorPersonas))
    }

    fun onFecha(v: String) = mutableForm.update {
        it.copy(fecha = v, errores = it.errores.copy(fecha = Validacion.fechaReserva(v)))
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
        mutableForm.update { it.copy(personas = soloDigitos, errores = it.errores.copy(personas = Validacion.personas(soloDigitos, it.dependencia))) }
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

    fun limpiarFormulario() { mutableForm.value = ReservaFormState() }

    fun guardar() {
        val f = mutableForm.value
        val errores = Validacion.validarReserva(f)
        if (errores.hayErrores()) {
            mutableForm.update { it.copy(errores = errores) }
            return
        }
        viewModelScope.launch {
            /** Validacion para reservas duplicidadas */
            val existentes = repo.reservasDelDia(f.fecha, f.dependencia)
            if (Validacion.hayConflicto(f.horaInicio, f.horaFin, existentes)) {
                mutableForm.update {
                    it.copy(errores = errores.copy(horaInicio = "La dependencia ya está reservada en ese horario"))
                }
                return@launch
            }
            val reserva = Reserva(
                solicitante = f.solicitante.trim(),
                telefono = f.telefono,
                dependencia = f.dependencia,
                fecha = f.fecha,
                fechaMs = FechaUtils.conversionMs(f.fecha) ?: 0L,
                horaInicio = f.horaInicio,
                horaFin = f.horaFin,
                personas = f.personas.toInt(),
                motivo = f.motivo.trim(),
                usaCocina = f.usaCocina,
                monto = f.monto.toInt()
            )
            val id = repo.insertarReserva(reserva).toInt()
            NotificacionHelper.notificar(
                getApplication(), id,
                "Reserva registrada",
                "${reserva.dependencia} el ${reserva.fecha} de ${reserva.horaInicio} a ${reserva.horaFin} " +
                        "(${reserva.monto.conversionPesos()}). Queda pendiente de confirmación."
            )
            mutableForm.update { it.copy(guardadoOk = true) }
        }
    }


    /** Metodos de cosas que se pueden hacer sobre una reserva */
    fun cambiarEstado(reserva: Reserva, nuevo: EstadoReserva) {
        viewModelScope.launch {
            repo.actualizarReserva(reserva.copy(estado = nuevo))
            val mensaje = if (nuevo == EstadoReserva.CONFIRMADA)
                "Se confirmó el arriendo de ${reserva.dependencia} para el ${reserva.fecha}."
            else
                "Se canceló el arriendo de ${reserva.dependencia} del ${reserva.fecha}."
            NotificacionHelper.notificar(getApplication(), reserva.id, "Reserva ${nuevo.etiqueta.lowercase()}", mensaje)
        }
    }

    //Recordatorio que avisa de los arriendos confirmados de hoy y mañana
    fun revisarRecordatorios() {
        viewModelScope.launch {
            val desde = FechaUtils.fechaHoyMs()
            val hasta = desde + 2 * FechaUtils.UN_DIA_MS - 1
            repo.confirmadasEntre(desde, hasta).forEach {
                NotificacionHelper.notificar(
                    getApplication(), 10_000 + it.id,
                    "Recordatorio de arriendo",
                    "${it.dependencia}: ${it.fecha} a las ${it.horaInicio} (${it.solicitante})."
                )
            }
        }
    }

}