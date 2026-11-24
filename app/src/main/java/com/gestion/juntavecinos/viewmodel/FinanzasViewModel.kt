package com.gestion.juntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.gestion.juntavecinos.data.remote.FinanzasPublicasDto
import com.gestion.juntavecinos.data.remote.InformeDto
import com.gestion.juntavecinos.data.remote.MovimientoRequest
import com.gestion.juntavecinos.data.remote.Resultado
import com.gestion.juntavecinos.data.repository.FinanzasRepository
import com.gestion.juntavecinos.data.repository.ReservasRepository
import com.gestion.juntavecinos.logic.Validacion
import com.gestion.juntavecinos.model.Categoria
import com.gestion.juntavecinos.model.EstadoReserva
import com.gestion.juntavecinos.model.Movimiento
import com.gestion.juntavecinos.model.Reserva
import com.gestion.juntavecinos.model.Sesion
import com.gestion.juntavecinos.model.TipoMovimiento
import com.gestion.juntavecinos.model.Visibilidad
import com.gestion.juntavecinos.ui.state.MovimientoFormState
import com.gestion.juntavecinos.ui.state.ResumenFinanciero
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
import java.io.File

class FinanzasViewModel(
    private val app: Application,
    private val repo: FinanzasRepository,
    reservasRepo: ReservasRepository,
    private val sesion: Sesion
) : BaseViewModel() {

    val movimientos: StateFlow<List<Movimiento>> = repo.movimientos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Finanzas autorizadas por la directiva (solo perfil Vecino) */
    private val mutablePublicas = MutableStateFlow<FinanzasPublicasDto?>(null)
    val publicas: StateFlow<FinanzasPublicasDto?> = mutablePublicas.asStateFlow()

    /** null si el perfil no tiene un resumen disponible */
    val resumen: StateFlow<ResumenFinanciero?> = combine(movimientos, mutablePublicas) { lista, publicas ->
        when {
            sesion.rol.veFinanzas -> ResumenFinanciero(
                ingresos = lista.filter { it.tipo == TipoMovimiento.INGRESO }.sumOf { it.monto.toLong() },
                gastos = lista.filter { it.tipo == TipoMovimiento.GASTO }.sumOf { it.monto.toLong() }
            )
            else -> publicas?.resumen?.let { ResumenFinanciero(it.ingresos, it.gastos) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val mutableCategorias = MutableStateFlow<List<Categoria>>(emptyList())
    val categorias: StateFlow<List<Categoria>> = mutableCategorias.asStateFlow()

    /** Reservas confirmadas que aun no tienen un pago registrado */
    val reservasSinPago: StateFlow<List<Reserva>> = combine(reservasRepo.reservas, repo.movimientos) { reservas, movs ->
        val pagadas = movs.mapNotNull { it.reservaId }.toSet()
        reservas.filter { it.estado == EstadoReserva.CONFIRMADA && it.id !in pagadas }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refrescar() {
        when {
            sesion.rol.veFinanzas -> ejecutar({ repo.refrescar() })
            sesion.rol.veFinanzasPublicas -> ejecutar({ repo.publico() }) { mutablePublicas.value = it }
        }
        if (sesion.rol.registraMovimientos) ejecutar({ repo.categorias() }) { mutableCategorias.value = it }
    }


    /** Formulario */
    private val mutableForm = MutableStateFlow(MovimientoFormState())

    val form: StateFlow<MovimientoFormState> = mutableForm.asStateFlow()

    fun onTipo(tipo: TipoMovimiento) = mutableForm.update {
        it.copy(tipo = tipo, categoria = "", reservaId = null, errores = it.errores.copy(categoria = null))
    }

    fun onCategoria(v: String) = mutableForm.update {
        it.copy(categoria = v, errores = it.errores.copy(categoria = Validacion.categoria(v)))
    }

    fun onDescripcion(v: String) = mutableForm.update {
        it.copy(descripcion = v, errores = it.errores.copy(descripcion = Validacion.descripcion(v)))
    }

    fun onMonto(v: String) {
        val soloDigitos = v.filter { it.isDigit() }.take(8)
        mutableForm.update { it.copy(monto = soloDigitos, errores = it.errores.copy(monto = Validacion.monto(soloDigitos))) }
    }

    fun onFecha(v: String) = mutableForm.update {
        it.copy(fecha = v, errores = it.errores.copy(fecha = Validacion.fechaMovimiento(v)))
    }

    /** Asocia el ingreso a una reserva confirmada y propone sus datos */
    fun onReserva(reserva: Reserva?) = mutableForm.update {
        if (reserva == null) it.copy(reservaId = null)
        else it.copy(
            reservaId = reserva.id,
            monto = reserva.monto.toString(),
            descripcion = it.descripcion.ifBlank { "Pago arriendo ${reserva.dependencia} ${reserva.fecha}" },
            errores = it.errores.copy(monto = null, descripcion = null)
        )
    }

    fun onMedioPago(v: String) = mutableForm.update { it.copy(medioPago = v) }

    fun onReferencia(v: String) = mutableForm.update { it.copy(referencia = v.take(100)) }

    fun onComprobante(ruta: String?) = mutableForm.update { it.copy(comprobantePath = ruta) }

    fun quitarComprobante() {
        mutableForm.value.comprobantePath?.let { File(it).delete() }
        onComprobante(null)
    }

    fun limpiarFormulario() { mutableForm.value = MovimientoFormState() }

    fun guardar() {
        val f = mutableForm.value
        if (f.enviando) return
        val errores = Validacion.validarMovimiento(f)
        val categoria = mutableCategorias.value.firstOrNull { it.nombre == f.categoria && it.tipo == f.tipo }
        if (errores.hayErrores() || (categoria == null && f.movimientoCreadoId == null)) {
            mutableForm.update {
                it.copy(errores = if (categoria == null) errores.copy(categoria = "Selecciona una categoría") else errores)
            }
            return
        }
        mutableForm.update { it.copy(enviando = true, errorGeneral = null) }
        viewModelScope.launch {
            // Si el movimiento ya se guardo y solo fallo la foto, se reintenta unicamente la foto
            val id = f.movimientoCreadoId ?: registrar(f, categoria!!.id) ?: return@launch
            val foto = f.comprobantePath?.let { File(it) }
            val envio = if (foto == null) Resultado.Exito(Unit) else repo.adjuntar(id, foto)
            repo.refrescar()
            when (envio) {
                is Resultado.Exito -> {
                    foto?.delete()
                    mutableForm.update { it.copy(enviando = false, comprobantePath = null, guardadoOk = true) }
                }
                is Resultado.Error -> mutableForm.update {
                    it.copy(
                        enviando = false, movimientoCreadoId = id,
                        errorGeneral = "El movimiento se guardó, pero no se pudo adjuntar el comprobante: " +
                                "${envio.mensaje} Presiona guardar para reintentar o quita la foto."
                    )
                }
            }
        }
    }

    private suspend fun registrar(f: MovimientoFormState, categoriaId: Int): Int? {
        val monto = f.monto.toInt()
        val r = repo.registrar(
            MovimientoRequest(
                tipo = f.tipo,
                categoriaId = categoriaId,
                descripcion = f.descripcion.trim(),
                monto = monto,
                fecha = FechaUtils.aIso(f.fecha),
                reservaId = f.reservaId,
                medioPago = f.medioPago.ifBlank { null }?.takeIf { f.reservaId != null },
                referencia = f.referencia.trim().ifBlank { null }?.takeIf { f.reservaId != null }
            )
        )
        return when (r) {
            is Resultado.Exito -> {
                NotificacionHelper.notificar(
                    app, 20_000 + r.dato.id,
                    "${f.tipo.etiqueta} registrado",
                    "${f.categoria}: ${monto.conversionPesos()} (${f.fecha})."
                )
                r.dato.id
            }
            is Resultado.Error -> {
                mutableForm.update { it.copy(enviando = false, errorGeneral = r.mensaje) }
                null
            }
        }
    }

    fun anular(movimiento: Movimiento) {
        ejecutar({ repo.anular(movimiento.id) }) { mostrar("Movimiento anulado") }
    }


    /** Informes (Tesoreria) */
    private val mutableInforme = MutableStateFlow<InformeDto?>(null)
    val informe: StateFlow<InformeDto?> = mutableInforme.asStateFlow()

    fun generarInforme(desde: String, hasta: String) {
        mutableInforme.value = null
        ejecutar({ repo.informe(desde, hasta) }) { mutableInforme.value = it }
    }


    /** Visibilidad para vecinos (Directiva) */
    private val mutableVisibilidad = MutableStateFlow<Visibilidad?>(null)
    val visibilidad: StateFlow<Visibilidad?> = mutableVisibilidad.asStateFlow()

    fun cargarVisibilidad() = ejecutar({ repo.visibilidad() }) { mutableVisibilidad.value = it }

    fun cambiarVisibilidad(v: Visibilidad) = ejecutar({ repo.editarVisibilidad(v) }) {
        mutableVisibilidad.value = it
        mostrar("Configuración guardada")
    }

}
