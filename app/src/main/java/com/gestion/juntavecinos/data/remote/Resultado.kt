package com.gestion.juntavecinos.data.remote

import com.google.gson.Gson
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/** Resultado de una operacion con el backend */
sealed interface Resultado<out T> {
    data class Exito<T>(val dato: T) : Resultado<T>
    data class Error(val mensaje: String, val codigo: Int? = null) : Resultado<Nothing>
}

/** Ejecuta una llamada y traduce los errores a mensajes para el usuario */
suspend fun <T> llamar(bloque: suspend () -> T): Resultado<T> = try {
    Resultado.Exito(bloque())
} catch (e: CancellationException) {
    throw e
} catch (e: HttpException) {
    Resultado.Error(mensajeDe(e), e.code())
} catch (e: IOException) {
    Resultado.Error("No se pudo conectar con el servidor. Revisa tu conexión e intenta nuevamente.")
} catch (e: JsonParseException) {
    Resultado.Error("El servidor entregó una respuesta no válida.")
}

private fun mensajeDe(e: HttpException): String {
    val delBackend = try {
        Gson().fromJson(e.response()?.errorBody()?.string(), ApiErrorDto::class.java)?.message
    } catch (_: Exception) {
        null
    }
    if (!delBackend.isNullOrBlank()) return delBackend
    return when (e.code()) {
        401 -> "Tu sesión expiró. Inicia sesión nuevamente."
        403 -> "Tu perfil no tiene permiso para esta acción."
        404 -> "No se encontró la información solicitada."
        413 -> "El archivo es demasiado grande."
        else -> "Ocurrió un error en el servidor. Intenta más tarde."
    }
}
