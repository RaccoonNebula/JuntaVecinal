package com.gestion.juntavecinos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.juntavecinos.data.remote.Resultado
import com.gestion.juntavecinos.data.repository.AuthRepository
import com.gestion.juntavecinos.logic.Validacion
import com.gestion.juntavecinos.ui.state.LoginFormState
import com.gestion.juntavecinos.ui.state.RegistroFormState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Inicio de sesion, creacion de cuenta y cierre de sesion */
class SesionViewModel(private val auth: AuthRepository) : ViewModel() {

    val estado = auth.estado

    private val mutableLogin = MutableStateFlow(LoginFormState())
    val login: StateFlow<LoginFormState> = mutableLogin.asStateFlow()

    private val mutableRegistro = MutableStateFlow(RegistroFormState())
    val registro: StateFlow<RegistroFormState> = mutableRegistro.asStateFlow()

    fun onEmail(v: String) = mutableLogin.update {
        it.copy(email = v.trim(), errores = it.errores.copy(email = null), errorGeneral = null)
    }

    fun onPassword(v: String) = mutableLogin.update {
        it.copy(password = v, errores = it.errores.copy(password = null), errorGeneral = null)
    }

    fun iniciarSesion() {
        val f = mutableLogin.value
        if (f.enviando) return
        val errores = Validacion.validarLogin(f)
        if (errores.hayErrores()) {
            mutableLogin.update { it.copy(errores = errores) }
            return
        }
        mutableLogin.update { it.copy(enviando = true, errorGeneral = null) }
        viewModelScope.launch {
            when (val r = auth.login(f.email, f.password)) {
                is Resultado.Exito -> limpiarFormularios()
                is Resultado.Error -> mutableLogin.update { it.copy(enviando = false, errorGeneral = r.mensaje) }
            }
        }
    }

    fun onRegNombre(v: String) = mutableRegistro.update {
        it.copy(nombre = v, errores = it.errores.copy(nombre = Validacion.solicitante(v)), errorGeneral = null)
    }

    fun onRegEmail(v: String) = mutableRegistro.update {
        it.copy(email = v.trim(), errores = it.errores.copy(email = Validacion.email(v)), errorGeneral = null)
    }

    fun onRegTelefono(v: String) {
        val soloDigitos = v.filter { it.isDigit() }.take(9)
        mutableRegistro.update {
            it.copy(telefono = soloDigitos, errores = it.errores.copy(telefono = Validacion.telefono(soloDigitos)), errorGeneral = null)
        }
    }

    fun onRegPassword(v: String) = mutableRegistro.update {
        val errorRepetir = if (it.repetir.isNotEmpty()) Validacion.repetir(v, it.repetir) else null
        it.copy(password = v, errores = it.errores.copy(password = Validacion.password(v), repetir = errorRepetir), errorGeneral = null)
    }

    fun onRegRepetir(v: String) = mutableRegistro.update {
        it.copy(repetir = v, errores = it.errores.copy(repetir = Validacion.repetir(it.password, v)), errorGeneral = null)
    }

    fun registrar() {
        val f = mutableRegistro.value
        if (f.enviando) return
        val errores = Validacion.validarRegistro(f)
        if (errores.hayErrores()) {
            mutableRegistro.update { it.copy(errores = errores) }
            return
        }
        mutableRegistro.update { it.copy(enviando = true, errorGeneral = null) }
        viewModelScope.launch {
            when (val r = auth.registrar(f.nombre, f.email, f.telefono, f.password)) {
                is Resultado.Exito -> limpiarFormularios()
                is Resultado.Error -> mutableRegistro.update { it.copy(enviando = false, errorGeneral = r.mensaje) }
            }
        }
    }

    fun limpiarFormularios() {
        mutableLogin.value = LoginFormState()
        mutableRegistro.value = RegistroFormState()
    }

    fun validarSesion() { viewModelScope.launch { auth.validarSesion() } }

    fun cerrarSesion() { viewModelScope.launch { auth.cerrarSesion() } }
}
