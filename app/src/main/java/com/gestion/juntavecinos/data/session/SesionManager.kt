package com.gestion.juntavecinos.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gestion.juntavecinos.model.Rol
import com.gestion.juntavecinos.model.Sesion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val Context.sesionStore by preferencesDataStore(name = "sesion")

sealed interface EstadoSesion {
    data object Cargando : EstadoSesion
    data class SinSesion(val mensaje: String? = null) : EstadoSesion
    data class Activa(val sesion: Sesion) : EstadoSesion
}

class SesionManager(
    private val contexto: Context,
    private val alcance: CoroutineScope,
    private val cifrado: CifradoToken = CifradoToken()
) {
    private val mutableEstado = MutableStateFlow<EstadoSesion>(EstadoSesion.Cargando)
    val estado: StateFlow<EstadoSesion> = mutableEstado.asStateFlow()

    private val mutex = Mutex()

    /** Se ejecuta al cerrar sesion para borrar los datos locales */
    var alCerrar: suspend () -> Unit = {}

    val sesion: Sesion? get() = (estado.value as? EstadoSesion.Activa)?.sesion
    val token: String? get() = sesion?.token

    fun cargar() {
        alcance.launch {
            mutex.withLock {
                if (mutableEstado.value != EstadoSesion.Cargando) return@withLock
                val guardada = leer()
                if (guardada != null && guardada.expiraEn > System.currentTimeMillis()) {
                    mutableEstado.value = EstadoSesion.Activa(guardada)
                } else {
                    val mensaje = if (guardada != null) EXPIRADA else null
                    limpiar()
                    mutableEstado.value = EstadoSesion.SinSesion(mensaje)
                }
            }
        }
    }

    suspend fun iniciar(sesion: Sesion) = mutex.withLock {
        limpiar()
        contexto.sesionStore.edit {
            it[TOKEN] = cifrado.cifrar(sesion.token)
            it[EXPIRA] = sesion.expiraEn
            it[ID] = sesion.id
            it[NOMBRE] = sesion.nombre
            it[EMAIL] = sesion.email
            it[TELEFONO] = sesion.telefono
            it[ROL] = sesion.rol.name
        }
        mutableEstado.value = EstadoSesion.Activa(sesion)
    }

    suspend fun cerrar(mensaje: String? = null) = mutex.withLock {
        mutableEstado.value = EstadoSesion.SinSesion(mensaje)
        limpiar()
    }

    /** Solo cierra si el token rechazado es el de la sesion actual */
    fun expirar(tokenRechazado: String) {
        alcance.launch { if (token == tokenRechazado) cerrar(EXPIRADA) }
    }

    /** Ultimo aviso mostrado como notificacion del dispositivo a cada usuario */
    suspend fun ultimoAviso(usuarioId: Int): Int =
        contexto.sesionStore.data.first()[intPreferencesKey("ultimo_aviso_$usuarioId")] ?: 0

    suspend fun guardarUltimoAviso(usuarioId: Int, avisoId: Int) {
        contexto.sesionStore.edit { it[intPreferencesKey("ultimo_aviso_$usuarioId")] = avisoId }
    }

    private suspend fun leer(): Sesion? {
        val p = contexto.sesionStore.data.first()
        return Sesion(
            id = p[ID] ?: return null,
            nombre = p[NOMBRE] ?: return null,
            email = p[EMAIL] ?: return null,
            telefono = p[TELEFONO] ?: return null,
            rol = Rol.entries.firstOrNull { it.name == p[ROL] } ?: return null,
            token = p[TOKEN]?.let { cifrado.descifrar(it) } ?: return null,
            expiraEn = p[EXPIRA] ?: return null
        )
    }

    private suspend fun limpiar() {
        contexto.sesionStore.edit { p ->
            listOf(TOKEN, NOMBRE, EMAIL, TELEFONO, ROL).forEach { p.remove(it) }
            p.remove(ID)
            p.remove(EXPIRA)
        }
        alCerrar()
    }

    private companion object {
        const val EXPIRADA = "Tu sesión expiró. Inicia sesión nuevamente."
        val TOKEN = stringPreferencesKey("token")
        val EXPIRA = longPreferencesKey("expira_en")
        val ID = intPreferencesKey("id")
        val NOMBRE = stringPreferencesKey("nombre")
        val EMAIL = stringPreferencesKey("email")
        val TELEFONO = stringPreferencesKey("telefono")
        val ROL = stringPreferencesKey("rol")
    }
}
