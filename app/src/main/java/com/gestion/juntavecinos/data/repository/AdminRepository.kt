package com.gestion.juntavecinos.data.repository

import com.gestion.juntavecinos.data.remote.ActivaRequest
import com.gestion.juntavecinos.data.remote.ActivoRequest
import com.gestion.juntavecinos.data.remote.ApiService
import com.gestion.juntavecinos.data.remote.CategoriaRequest
import com.gestion.juntavecinos.data.remote.ContrasenaRequest
import com.gestion.juntavecinos.data.remote.CrearUsuarioRequest
import com.gestion.juntavecinos.data.remote.DependenciaRequest
import com.gestion.juntavecinos.data.remote.EditarUsuarioRequest
import com.gestion.juntavecinos.data.remote.EtiquetaRequest
import com.gestion.juntavecinos.data.remote.llamar
import com.gestion.juntavecinos.model.Sede

/** Operaciones exclusivas del perfil Administrador */
class AdminRepository(private val api: ApiService) {

    suspend fun usuarios() = llamar { api.usuarios() }
    suspend fun crearUsuario(r: CrearUsuarioRequest) = llamar { api.crearUsuario(r) }
    suspend fun editarUsuario(id: Int, r: EditarUsuarioRequest) = llamar { api.editarUsuario(id, r) }
    suspend fun cambiarActivo(id: Int, activo: Boolean) = llamar { api.cambiarActivo(id, ActivoRequest(activo)) }
    suspend fun cambiarContrasena(id: Int, password: String) =
        llamar { api.cambiarContrasena(id, ContrasenaRequest(password)) }

    suspend fun crearDependencia(r: DependenciaRequest) = llamar { api.crearDependencia(r) }
    suspend fun editarDependencia(id: Int, r: DependenciaRequest) = llamar { api.editarDependencia(id, r) }
    suspend fun cambiarActiva(id: Int, activa: Boolean) = llamar { api.cambiarActiva(id, ActivaRequest(activa)) }

    suspend fun categorias() = llamar { api.categorias() }
    suspend fun crearCategoria(r: CategoriaRequest) = llamar { api.crearCategoria(r) }
    suspend fun editarCategoria(id: Int, r: CategoriaRequest) = llamar { api.editarCategoria(id, r) }

    suspend fun estados() = llamar { api.estados() }
    suspend fun editarEstado(codigo: String, etiqueta: String) =
        llamar { api.editarEstado(codigo, EtiquetaRequest(etiqueta)) }

    suspend fun sede() = llamar { api.sede() }
    suspend fun editarSede(sede: Sede) = llamar { api.editarSede(sede) }
}
