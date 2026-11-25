package com.gestion.juntavecinos.data.remote

import com.gestion.juntavecinos.data.session.SesionManager
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response

class AuthInterceptorTest : StringSpec({

    /** Ejecuta el interceptor y entrega la peticion que se habria enviado */
    fun enviar(sesiones: SesionManager, ruta: String, codigo: Int = 200): Request {
        lateinit var enviada: Request
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns Request.Builder().url("http://localhost/$ruta").build()
        every { chain.proceed(any()) } answers {
            enviada = firstArg()
            Response.Builder().request(enviada).protocol(Protocol.HTTP_1_1).code(codigo).message("").build()
        }
        AuthInterceptor(sesiones).intercept(chain)
        return enviada
    }

    fun sesionesCon(token: String): SesionManager {
        val sesiones = mockk<SesionManager>(relaxed = true)
        every { sesiones.token } returns token
        return sesiones
    }

    "debe agregar el token a las peticiones" {
        enviar(sesionesCon("abc"), "reservas/mias").header("Authorization") shouldBe "Bearer abc"
    }

    "no debe enviar el token en login ni registro" {
        enviar(sesionesCon("abc"), "auth/login").header("Authorization").shouldBeNull()
        enviar(sesionesCon("abc"), "auth/register").header("Authorization").shouldBeNull()
    }

    "debe expirar la sesion cuando el backend responde 401" {
        val sesiones = sesionesCon("abc")
        enviar(sesiones, "reservas/mias", codigo = 401)
        verify { sesiones.expirar("abc") }
    }
})
