package com.gestion.juntavecinos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.juntavecinos.logic.Validacion
import com.gestion.juntavecinos.model.AppDatabase
import com.gestion.juntavecinos.model.ArriendoRepository
import com.gestion.juntavecinos.model.Movimiento
import com.gestion.juntavecinos.model.TipoMovimiento
import com.gestion.juntavecinos.ui.state.MovimientoFormState
import com.gestion.juntavecinos.ui.state.ResumenFinanciero
import com.gestion.juntavecinos.utils.FechaUtils
import com.gestion.juntavecinos.utils.NotificacionHelper
import com.gestion.juntavecinos.utils.conversionPesos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import kotlin.text.toInt

class FinanzasViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ArriendoRepository(AppDatabase.getInstance(app))

    val movimientos: StateFlow<List<Movimiento>> = repo.movimientos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val resumen: StateFlow<ResumenFinanciero> = movimientos.map { lista ->
        ResumenFinanciero(
            ingresos = lista.filter { it.tipo == TipoMovimiento.INGRESO }.sumOf { it.monto },
            gastos = lista.filter { it.tipo == TipoMovimiento.GASTO }.sumOf { it.monto }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ResumenFinanciero())


    /** Formulario */
    private val mutableForm = MutableStateFlow(MovimientoFormState())

    val form: StateFlow<MovimientoFormState> = mutableForm.asStateFlow()

    fun onTipo(tipo: TipoMovimiento) = mutableForm.update {
        it.copy(tipo = tipo, categoria = "", errores = it.errores.copy(categoria = null))
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

    fun onComprobante(ruta: String?) = mutableForm.update { it.copy(comprobantePath = ruta) }

    fun quitarComprobante() {
        mutableForm.value.comprobantePath?.let { File(it).delete() }
        onComprobante(null)
    }

    fun limpiarFormulario() { mutableForm.value = MovimientoFormState() }

    fun guardar() {
        val f = mutableForm.value
        val errores = Validacion.validarMovimiento(f)
        if (errores.hayErrores()) {
            mutableForm.update { it.copy(errores = errores) }
            return
        }
        viewModelScope.launch {
            val monto = f.monto.toInt()
            val id = repo.insertarMovimiento(
                Movimiento(
                    tipo = f.tipo,
                    categoria = f.categoria,
                    descripcion = f.descripcion.trim(),
                    monto = monto,
                    fecha = f.fecha,
                    fechaMs = FechaUtils.conversionMs(f.fecha) ?: 0L,
                    comprobantePath = f.comprobantePath
                )
            ).toInt()
            NotificacionHelper.notificar(
                getApplication(), 20_000 + id,
                "${f.tipo.etiqueta} registrado",
                "${f.categoria}: ${monto.conversionPesos()} (${f.fecha})."
            )
            mutableForm.update { it.copy(guardadoOk = true) }
        }
    }

    fun eliminar(movimiento: Movimiento) {
        viewModelScope.launch {
            repo.eliminarMovimiento(movimiento)
            movimiento.comprobantePath?.let { File(it).delete() }
        }
    }

}