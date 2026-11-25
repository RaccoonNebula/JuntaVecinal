package com.gestion.juntavecinos.viewmodel

import com.gestion.juntavecinos.data.remote.Resultado
import com.gestion.juntavecinos.data.repository.AuthRepository
import com.gestion.juntavecinos.pruebas.usarDispatcherDePrueba
import com.gestion.juntavecinos.ui.state.RegistroFormState
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class SesionViewModelTest : StringSpec({

    usarDispatcherDePrueba()

    fun SesionViewModel.completarRegistro() {
        onRegNombre("Ana Soto")
        onRegEmail("ana@zaror.test")
        onRegTelefono("9-1234-5678")
        onRegPassword("Clave.123")
        onRegRepetir("Clave.123")
    }

    "iniciarSesion() debe mostrar el error del backend" {
        val auth = mockk<AuthRepository>(relaxed = true)
        coEvery { auth.login(any(), any()) } returns Resultado.Error("Correo o contraseña incorrectos", 401)
        val vm = SesionViewModel(auth)

        vm.onEmail("vecino@zaror.test")
        vm.onPassword("mala")
        vm.iniciarSesion()

        vm.login.value.errorGeneral shouldBe "Correo o contraseña incorrectos"
        vm.login.value.enviando shouldBe false
    }

    "registrar() debe crear la cuenta con datos correctos" {
        val auth = mockk<AuthRepository>(relaxed = true)
        coEvery { auth.registrar(any(), any(), any(), any()) } returns Resultado.Exito(Unit)
        val vm = SesionViewModel(auth)

        vm.completarRegistro()
        vm.registrar()

        coVerify { auth.registrar("Ana Soto", "ana@zaror.test", "912345678", "Clave.123") }
        vm.registro.value shouldBe RegistroFormState()
    }
})
