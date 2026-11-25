package com.gestion.juntavecinos.data.repository

import com.gestion.juntavecinos.data.remote.ApiService
import com.gestion.juntavecinos.data.remote.Resultado
import com.gestion.juntavecinos.data.remote.errorHttp
import com.gestion.juntavecinos.data.session.SesionManager
import com.gestion.juntavecinos.model.Horario
import com.gestion.juntavecinos.model.ReservaDao
import com.gestion.juntavecinos.model.Rol
import com.gestion.juntavecinos.pruebas.DatosPrueba
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest

class ReservasRepositoryTest : StringSpec({

    fun sesionesDeVecino(): SesionManager {
        val sesiones = mockk<SesionManager>(relaxed = true)
        every { sesiones.sesion } returns DatosPrueba.sesion(Rol.VECINO)
        every { sesiones.token } returns "token"
        return sesiones
    }

    "refrescar() debe pedir al vecino solo sus reservas y guardarlas" {
        val api = mockk<ApiService>()
        val dao = mockk<ReservaDao>(relaxed = true)
        coEvery { api.misReservas() } returns listOf(DatosPrueba.reservaDto())

        runTest { ReservasRepository(api, dao, sesionesDeVecino()).refrescar() shouldBe Resultado.Exito(Unit) }
        coVerify { dao.reemplazar(listOf(DatosPrueba.reservaDto().aModelo())) }
    }

    "refrescar() no debe guardar datos si la sesion cambio" {
        val api = mockk<ApiService>()
        val dao = mockk<ReservaDao>(relaxed = true)
        val sesiones = sesionesDeVecino()
        every { sesiones.token } returns "token de otra cuenta"
        coEvery { api.misReservas() } returns listOf(DatosPrueba.reservaDto())

        runTest { ReservasRepository(api, dao, sesiones).refrescar() }
        coVerify(exactly = 0) { dao.reemplazar(any()) }
    }

    "crear() debe informar si el horario esta ocupado" {
        val api = mockk<ApiService>()
        coEvery { api.crearReserva(any()) } throws errorHttp(409, """{"message":"Horario ocupado"}""")
        coEvery { api.misReservas() } returns emptyList()

        runTest {
            ReservasRepository(api, mockk(relaxed = true), sesionesDeVecino()).crear(mockk()) shouldBe
                    Resultado.Error("Horario ocupado", 409)
        }
    }
})
