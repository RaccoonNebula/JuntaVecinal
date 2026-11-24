package com.gestion.juntavecinos.data.remote

import com.gestion.juntavecinos.BuildConfig
import com.gestion.juntavecinos.data.session.SesionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Agrega el token a cada peticion. Si el backend responde 401, la sesion dejo de ser valida. */
class AuthInterceptor(private val sesiones: SesionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val esPublica = original.url.encodedPath.let { it == "/auth/login" || it == "/auth/register" }
        val token = if (esPublica) null else sesiones.token
        val peticion = if (token == null) original
        else original.newBuilder().header("Authorization", "Bearer $token").build()

        val respuesta = chain.proceed(peticion)
        if (respuesta.code == 401 && token != null) sesiones.expirar(token)
        return respuesta
    }
}

object ClienteApi {
    fun crearHttp(sesiones: SesionManager): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(sesiones))
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun crear(http: OkHttpClient): ApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(http)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    fun urlComprobante(id: Int) = "${BuildConfig.API_BASE_URL}comprobantes/$id/archivo"
}
