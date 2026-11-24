package com.gestion.juntavecinos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.juntavecinos.data.remote.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado de carga y mensajes comunes a todas las pantallas */
abstract class BaseViewModel : ViewModel() {

    private val pendientes = MutableStateFlow(0)
    private val mutableCargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = mutableCargando.asStateFlow()

    private val mutableMensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = mutableMensaje.asStateFlow()

    fun limpiarMensaje() { mutableMensaje.value = null }

    protected fun mostrar(texto: String) { mutableMensaje.value = texto }

    /** Ejecuta una operacion con el backend mostrando la carga y, si falla, el error */
    protected fun <T> ejecutar(bloque: suspend () -> Resultado<T>, alExito: suspend (T) -> Unit = {}) {
        viewModelScope.launch {
            pendientes.update { it + 1 }
            mutableCargando.value = true
            try {
                when (val r = bloque()) {
                    is Resultado.Exito -> alExito(r.dato)
                    is Resultado.Error -> mostrar(r.mensaje)
                }
            } finally {
                pendientes.update { it - 1 }
                mutableCargando.value = pendientes.value > 0
            }
        }
    }
}
