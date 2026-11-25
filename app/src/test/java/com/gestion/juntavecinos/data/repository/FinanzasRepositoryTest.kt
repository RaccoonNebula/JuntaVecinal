package com.gestion.juntavecinos.data.repository

import com.gestion.juntavecinos.data.remote.ApiService
import com.gestion.juntavecinos.data.session.SesionManager
import com.gestion.juntavecinos.model.Rol
import com.gestion.juntavecinos.pruebas.DatosPrueba
import io.kotest.core.spec.style.StringSpec
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest

class FinanzasRepositoryTest : StringSpec({

    "refrescar() no debe pedir movimientos para un vecino" {
        val api = mockk<ApiService>()
        val sesiones = mockk<SesionManager>()
        every { sesiones.sesion } returns DatosPrueba.sesion(Rol.VECINO)

        runTest { FinanzasRepository(api, mockk(relaxed = true), sesiones).refrescar() }
        coVerify(exactly = 0) { api.movimientos() }
    }
})
