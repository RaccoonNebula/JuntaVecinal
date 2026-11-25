package com.gestion.juntavecinos.data.repository

import com.gestion.juntavecinos.data.remote.ApiService
import com.gestion.juntavecinos.data.remote.LoginRequest
import com.gestion.juntavecinos.data.remote.LoginResponse
import com.gestion.juntavecinos.data.remote.Resultado
import com.gestion.juntavecinos.data.remote.errorHttp
import com.gestion.juntavecinos.data.session.SesionManager
import com.gestion.juntavecinos.model.Rol
import com.gestion.juntavecinos.pruebas.DatosPrueba
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest

class AuthRepositoryTest : StringSpec({

    val respuesta = LoginResponse("ok", "token", 1000L, DatosPrueba.usuario())

    "login() debe iniciar la sesion con los datos del backend" {
        val api = mockk<ApiService>()
        val sesiones = mockk<SesionManager>(relaxed = true)
        coEvery { api.login(LoginRequest("vecina@zaror.test", "clave")) } returns respuesta

        runTest {
            AuthRepository(api, sesiones).login(" vecina@zaror.test ", "clave") shouldBe Resultado.Exito(Unit)
        }
        coVerify { sesiones.iniciar(match { it.token == "token" && it.rol == Rol.VECINO }) }
    }

    "login() no debe iniciar sesion con credenciales incorrectas" {
        val api = mockk<ApiService>()
        val sesiones = mockk<SesionManager>(relaxed = true)
        coEvery { api.login(any()) } throws errorHttp(401, """{"message":"Correo o contraseña incorrectos"}""")

        runTest {
            AuthRepository(api, sesiones).login("a@zaror.test", "mala") shouldBe
                    Resultado.Error("Correo o contraseña incorrectos", 401)
        }
        coVerify(exactly = 0) { sesiones.iniciar(any()) }
    }
})
