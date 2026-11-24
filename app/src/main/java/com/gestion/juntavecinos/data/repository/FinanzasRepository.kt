package com.gestion.juntavecinos.data.repository

import com.gestion.juntavecinos.data.remote.ApiService
import com.gestion.juntavecinos.data.remote.FinanzasPublicasDto
import com.gestion.juntavecinos.data.remote.InformeDto
import com.gestion.juntavecinos.data.remote.MovimientoDto
import com.gestion.juntavecinos.data.remote.MovimientoRequest
import com.gestion.juntavecinos.data.remote.Resultado
import com.gestion.juntavecinos.data.remote.llamar
import com.gestion.juntavecinos.data.session.SesionManager
import com.gestion.juntavecinos.model.Categoria
import com.gestion.juntavecinos.model.MovimientoDao
import com.gestion.juntavecinos.model.Visibilidad
import com.gestion.juntavecinos.utils.FechaUtils
import com.gestion.juntavecinos.utils.ImagenUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class FinanzasRepository(
    private val api: ApiService,
    private val dao: MovimientoDao,
    private val sesiones: SesionManager
) {
    val movimientos = dao.observarTodos()

    suspend fun refrescar(): Resultado<Unit> {
        val sesion = sesiones.sesion ?: return Resultado.Exito(Unit)
        if (!sesion.rol.veFinanzas) return Resultado.Exito(Unit)
        return llamar {
            val lista = api.movimientos()
            val comprobantes = api.comprobantes().associate { it.movimientoId to it.id }
            if (sesiones.token == sesion.token) dao.reemplazar(lista.map { it.aModelo(comprobantes[it.id]) })
        }
    }

    suspend fun registrar(request: MovimientoRequest): Resultado<MovimientoDto> =
        llamar { api.registrarMovimiento(request) }

    suspend fun adjuntar(movimientoId: Int, foto: File): Resultado<Unit> {
        val jpeg = withContext(Dispatchers.IO) { ImagenUtils.prepararJpeg(foto) }
            ?: return Resultado.Error("No se pudo leer la foto del comprobante.")
        return llamar {
            api.adjuntarComprobante(
                movimientoId.toString().toRequestBody("text/plain".toMediaType()),
                MultipartBody.Part.createFormData(
                    "archivo", "comprobante.jpg", jpeg.toRequestBody("image/jpeg".toMediaType())
                )
            )
            Unit
        }
    }

    suspend fun anular(id: Int): Resultado<Unit> = llamar { api.anularMovimiento(id) }.also { refrescar() }

    suspend fun categorias(): Resultado<List<Categoria>> = llamar { api.categorias() }

    suspend fun publico(): Resultado<FinanzasPublicasDto> = llamar { api.finanzasPublicas() }

    suspend fun informe(desde: String, hasta: String): Resultado<InformeDto> =
        llamar { api.informe(FechaUtils.aIso(desde), FechaUtils.aIso(hasta)) }

    suspend fun visibilidad(): Resultado<Visibilidad> = llamar { api.visibilidad() }

    suspend fun editarVisibilidad(v: Visibilidad): Resultado<Visibilidad> = llamar { api.editarVisibilidad(v) }
}
