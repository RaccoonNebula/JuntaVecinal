package com.gestion.juntavecinos.data.remote

import com.gestion.juntavecinos.model.Aviso
import com.gestion.juntavecinos.model.Categoria
import com.gestion.juntavecinos.model.Dependencia
import com.gestion.juntavecinos.model.EstadoEtiqueta
import com.gestion.juntavecinos.model.Horario
import com.gestion.juntavecinos.model.Sede
import com.gestion.juntavecinos.model.Usuario
import com.gestion.juntavecinos.model.Visibilidad
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints del backend, siempre a traves del api-gateway (puerto 9090) */
interface ApiService {

    // auth-service
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun registrar(@Body request: RegistroRequest): Usuario

    @GET("auth/me")
    suspend fun usuarioActual(): Usuario

    @GET("usuarios")
    suspend fun usuarios(): List<Usuario>

    @POST("usuarios")
    suspend fun crearUsuario(@Body request: CrearUsuarioRequest): Usuario

    @PUT("usuarios/{id}")
    suspend fun editarUsuario(@Path("id") id: Int, @Body request: EditarUsuarioRequest): Usuario

    @PATCH("usuarios/{id}/activo")
    suspend fun cambiarActivo(@Path("id") id: Int, @Body request: ActivoRequest): Usuario

    @PATCH("usuarios/{id}/contrasena")
    suspend fun cambiarContrasena(@Path("id") id: Int, @Body request: ContrasenaRequest)

    // dependencia-service
    @GET("dependencias")
    suspend fun dependencias(): List<Dependencia>

    @POST("dependencias")
    suspend fun crearDependencia(@Body request: DependenciaRequest): Dependencia

    @PUT("dependencias/{id}")
    suspend fun editarDependencia(@Path("id") id: Int, @Body request: DependenciaRequest): Dependencia

    @PATCH("dependencias/{id}/activa")
    suspend fun cambiarActiva(@Path("id") id: Int, @Body request: ActivaRequest): Dependencia

    @GET("dependencias/sede")
    suspend fun sede(): Sede

    @PUT("dependencias/sede")
    suspend fun editarSede(@Body sede: Sede): Sede

    // reserva-service
    @GET("reservas")
    suspend fun reservas(): List<ReservaDto>

    @GET("reservas/mias")
    suspend fun misReservas(): List<ReservaDto>

    @GET("reservas/confirmadas")
    suspend fun reservasConfirmadas(): List<ReservaDto>

    @GET("reservas/disponibilidad")
    suspend fun disponibilidad(
        @Query("dependenciaId") dependenciaId: Int, @Query("fecha") fecha: String
    ): List<Horario>

    @POST("reservas")
    suspend fun crearReserva(@Body request: ReservaRequest): ReservaDto

    @PUT("reservas/{id}")
    suspend fun corregirReserva(@Path("id") id: Int, @Body request: ReservaRequest): ReservaDto

    @PATCH("reservas/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: Int, @Body request: CambioEstadoRequest): ReservaDto

    @PATCH("reservas/{id}/observacion")
    suspend fun observar(@Path("id") id: Int, @Body request: ObservacionRequest): ReservaDto

    @GET("reservas/estados")
    suspend fun estados(): List<EstadoEtiqueta>

    @PUT("reservas/estados/{codigo}")
    suspend fun editarEstado(@Path("codigo") codigo: String, @Body request: EtiquetaRequest): EstadoEtiqueta

    // movimiento-service
    @GET("movimientos")
    suspend fun movimientos(): List<MovimientoDto>

    @POST("movimientos")
    suspend fun registrarMovimiento(@Body request: MovimientoRequest): MovimientoDto

    @DELETE("movimientos/{id}")
    suspend fun anularMovimiento(@Path("id") id: Int)

    @GET("movimientos/publico")
    suspend fun finanzasPublicas(): FinanzasPublicasDto

    @GET("movimientos/informe")
    suspend fun informe(@Query("desde") desde: String, @Query("hasta") hasta: String): InformeDto

    @GET("movimientos/visibilidad")
    suspend fun visibilidad(): Visibilidad

    @PUT("movimientos/visibilidad")
    suspend fun editarVisibilidad(@Body visibilidad: Visibilidad): Visibilidad

    @GET("categorias")
    suspend fun categorias(): List<Categoria>

    @POST("categorias")
    suspend fun crearCategoria(@Body request: CategoriaRequest): Categoria

    @PUT("categorias/{id}")
    suspend fun editarCategoria(@Path("id") id: Int, @Body request: CategoriaRequest): Categoria

    // comprobante-service
    @GET("comprobantes")
    suspend fun comprobantes(): List<ComprobanteDto>

    @Multipart
    @POST("comprobantes")
    suspend fun adjuntarComprobante(
        @Part("movimientoId") movimientoId: RequestBody, @Part archivo: MultipartBody.Part
    ): ComprobanteDto

    // notificacion-service
    @GET("notificaciones")
    suspend fun notificaciones(): List<Aviso>

    @PATCH("notificaciones/{id}/leida")
    suspend fun marcarLeida(@Path("id") id: Int)
}
