package com.gestion.juntavecinos.data.repository

import com.gestion.juntavecinos.data.remote.ApiService
import com.gestion.juntavecinos.data.remote.LoginRequest
import com.gestion.juntavecinos.data.remote.RegistroRequest
import com.gestion.juntavecinos.data.remote.Resultado
import com.gestion.juntavecinos.data.remote.llamar
import com.gestion.juntavecinos.data.session.SesionManager
import com.gestion.juntavecinos.model.Sesion

class AuthRepository(private val api: ApiService, private val sesiones: SesionManager) {

    val estado = sesiones.estado

    suspend fun login(email: String, password: String): Resultado<Unit> = llamar {
        val r = api.login(LoginRequest(email.trim(), password))
        sesiones.iniciar(
            Sesion(
                id = r.usuario.id,
                nombre = r.usuario.nombre,
                email = r.usuario.email,
                telefono = r.usuario.telefono,
                rol = r.usuario.rol,
                token = r.token,
                expiraEn = r.expiraEn
            )
        )
    }

    /** Crea la cuenta (siempre con perfil Vecino) e inicia sesion */
    suspend fun registrar(nombre: String, email: String, telefono: String, password: String): Resultado<Unit> {
        val creado = llamar { api.registrar(RegistroRequest(nombre.trim(), email.trim(), telefono, password)) }
        return when (creado) {
            is Resultado.Error -> creado
            is Resultado.Exito -> login(email, password)
        }
    }

    /** Confirma que la cuenta siga activa y con el mismo perfil */
    suspend fun validarSesion() {
        val actual = sesiones.sesion ?: return
        val r = llamar { api.usuarioActual() }
        if (r is Resultado.Exito && r.dato.rol != actual.rol && sesiones.token == actual.token) {
            sesiones.cerrar("Tu perfil cambió. Inicia sesión nuevamente.")
        }
    }

    suspend fun cerrarSesion() = sesiones.cerrar()
}
