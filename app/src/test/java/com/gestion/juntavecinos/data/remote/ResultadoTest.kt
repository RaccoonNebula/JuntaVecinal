package com.gestion.juntavecinos.data.remote

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** Simula una respuesta de error del backend */
fun errorHttp(codigo: Int, cuerpo: String = "") =
    HttpException(Response.error<Any>(codigo, cuerpo.toResponseBody("application/json".toMediaType())))

class ResultadoTest : StringSpec({

    "llamar() debe informar cuando no hay conexion" {
        runTest {
            val r = llamar<Int> { throw IOException() }
            r.shouldBeInstanceOf<Resultado.Error>().mensaje shouldStartWith "No se pudo conectar"
        }
    }

    "llamar() debe mostrar el mensaje que envia el backend" {
        runTest {
            val r = llamar<Int> { throw errorHttp(409, """{"message":"Horario ocupado"}""") }
            r shouldBe Resultado.Error("Horario ocupado", 409)
        }
    }
})
