package com.gestion.juntavecinos.viewmodel

import com.gestion.juntavecinos.data.remote.Resultado
import com.gestion.juntavecinos.data.repository.AdminRepository
import com.gestion.juntavecinos.pruebas.DatosPrueba
import com.gestion.juntavecinos.pruebas.usarDispatcherDePrueba
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.mockk

class AdminViewModelTest : StringSpec({

    usarDispatcherDePrueba()

    "cambiarActivo() debe invertir el estado del usuario" {
        val repo = mockk<AdminRepository>()
        coEvery { repo.cambiarActivo(any(), any()) } returns Resultado.Exito(DatosPrueba.usuario())
        coEvery { repo.usuarios() } returns Resultado.Exito(emptyList())
        val vm = AdminViewModel(repo, mockk(relaxed = true))

        vm.cambiarActivo(DatosPrueba.usuario(activo = true))
        vm.mensaje.value shouldBe "Usuario desactivado"
        vm.cambiarActivo(DatosPrueba.usuario(activo = false))
        vm.mensaje.value shouldBe "Usuario activado"

        coVerifyOrder {
            repo.cambiarActivo(1, false)
            repo.cambiarActivo(1, true)
        }
    }
})
