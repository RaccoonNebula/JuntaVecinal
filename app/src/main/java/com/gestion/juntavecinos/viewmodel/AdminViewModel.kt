package com.gestion.juntavecinos.viewmodel

import androidx.lifecycle.viewModelScope
import com.gestion.juntavecinos.data.remote.CategoriaRequest
import com.gestion.juntavecinos.data.remote.CrearUsuarioRequest
import com.gestion.juntavecinos.data.remote.DependenciaRequest
import com.gestion.juntavecinos.data.remote.EditarUsuarioRequest
import com.gestion.juntavecinos.data.remote.Resultado
import com.gestion.juntavecinos.data.repository.AdminRepository
import com.gestion.juntavecinos.data.repository.SedeRepository
import com.gestion.juntavecinos.model.Categoria
import com.gestion.juntavecinos.model.Dependencia
import com.gestion.juntavecinos.model.EstadoEtiqueta
import com.gestion.juntavecinos.model.Sede
import com.gestion.juntavecinos.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

/** Cuentas, dependencias, categorias, estados e informacion de la sede (perfil Administrador) */
class AdminViewModel(
    private val repo: AdminRepository,
    private val sedeRepo: SedeRepository
) : BaseViewModel() {

    private val mutableUsuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = mutableUsuarios.asStateFlow()

    val dependencias: StateFlow<List<Dependencia>> = sedeRepo.dependencias
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val mutableCategorias = MutableStateFlow<List<Categoria>>(emptyList())
    val categorias: StateFlow<List<Categoria>> = mutableCategorias.asStateFlow()

    private val mutableEstados = MutableStateFlow<List<EstadoEtiqueta>>(emptyList())
    val estados: StateFlow<List<EstadoEtiqueta>> = mutableEstados.asStateFlow()

    private val mutableSede = MutableStateFlow<Sede?>(null)
    val sede: StateFlow<Sede?> = mutableSede.asStateFlow()

    fun cargarUsuarios() = ejecutar({ repo.usuarios() }) { mutableUsuarios.value = it }

    fun cargarDependencias() = ejecutar({ sedeRepo.refrescar() })

    fun cargarCatalogos() {
        ejecutar({ repo.categorias() }) { mutableCategorias.value = it }
        ejecutar({ repo.estados() }) { mutableEstados.value = it }
        ejecutar({ repo.sede() }) { mutableSede.value = it }
    }

    /** id en null crea; con valor, modifica. alTerminar se llama solo si el backend acepto el cambio. */
    fun guardarUsuario(id: Int?, u: Usuario, password: String, alTerminar: () -> Unit) {
        val email = u.email.trim()
        guardar("Usuario guardado", alTerminar, ::cargarUsuarios) {
            if (id == null) repo.crearUsuario(CrearUsuarioRequest(u.nombre.trim(), email, u.telefono, password, u.rol))
            else repo.editarUsuario(id, EditarUsuarioRequest(u.nombre.trim(), email, u.telefono, u.rol))
        }
    }

    fun cambiarActivo(u: Usuario) =
        guardar(if (u.activo) "Usuario desactivado" else "Usuario activado", {}, ::cargarUsuarios) {
            repo.cambiarActivo(u.id, !u.activo)
        }

    fun cambiarContrasena(id: Int, password: String, alTerminar: () -> Unit) =
        guardar("Contraseña actualizada", alTerminar, {}) { repo.cambiarContrasena(id, password) }

    fun guardarDependencia(id: Int?, r: DependenciaRequest, alTerminar: () -> Unit) {
        guardar("Dependencia guardada", alTerminar, ::cargarDependencias) {
            if (id == null) repo.crearDependencia(r) else repo.editarDependencia(id, r)
        }
    }

    fun cambiarActiva(d: Dependencia) =
        guardar(if (d.activa) "Dependencia desactivada" else "Dependencia activada", {}, ::cargarDependencias) {
            repo.cambiarActiva(d.id, !d.activa)
        }

    fun guardarCategoria(id: Int?, r: CategoriaRequest, alTerminar: () -> Unit) {
        guardar("Categoría guardada", alTerminar, ::cargarCatalogos) {
            if (id == null) repo.crearCategoria(r) else repo.editarCategoria(id, r)
        }
    }

    fun guardarEstado(codigo: String, etiqueta: String, alTerminar: () -> Unit) =
        guardar("Estado guardado", alTerminar, ::cargarCatalogos) { repo.editarEstado(codigo, etiqueta.trim()) }

    fun guardarSede(s: Sede, alTerminar: () -> Unit) =
        guardar("Información guardada", alTerminar, ::cargarCatalogos) { repo.editarSede(s) }

    private fun guardar(
        mensaje: String, alTerminar: () -> Unit, recargar: () -> Unit, bloque: suspend () -> Resultado<Any>
    ) = ejecutar(bloque) {
        alTerminar()
        mostrar(mensaje)
        recargar()
    }
}
