package com.gestion.juntavecinos.viewmodel

import com.gestion.juntavecinos.data.remote.Resultado
import com.gestion.juntavecinos.data.repository.AvisosRepository
import com.gestion.juntavecinos.data.repository.ReservasRepository
import com.gestion.juntavecinos.data.repository.SedeRepository
import com.gestion.juntavecinos.model.EstadoReserva
import com.gestion.juntavecinos.model.Horario
import com.gestion.juntavecinos.model.Reserva
import com.gestion.juntavecinos.pruebas.DatosPrueba
import com.gestion.juntavecinos.pruebas.usarDispatcherDePrueba
import com.gestion.juntavecinos.utils.NotificacionHelper
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf

class ReservasViewModelTest : StringSpec({

    usarDispatcherDePrueba()
    beforeTest {
        mockkObject(NotificacionHelper)
        every { NotificacionHelper.notificar(any(), any(), any(), any()) } just runs
    }
    afterTest { unmockkObject(NotificacionHelper) }

    fun repositorio(): ReservasRepository {
        val repo = mockk<ReservasRepository>(relaxed = true)
        every { repo.reservas } returns flowOf(emptyList())
        coEvery { repo.disponibilidad(any(), any()) } returns Resultado.Exito(emptyList())
        return repo
    }

    fun crearViewModel(repo: ReservasRepository, avisos: AvisosRepository = mockk(relaxed = true)): ReservasViewModel {
        val sede = mockk<SedeRepository>(relaxed = true)
        every { sede.dependencias } returns flowOf(listOf(DatosPrueba.salon))
        return ReservasViewModel(mockk(relaxed = true), repo, sede, avisos, DatosPrueba.sesion())
    }

    fun ReservasViewModel.completarFormulario() {
        onSolicitante("Ana Soto")
        onTelefono("912345678")
        onDependencia("Salón")
        onFecha(DatosPrueba.fechaFutura)
        onHoraInicio("10:00")
        onHoraFin("12:00")
        onPersonas("10")
        onMotivo("Taller vecinal")
        onMonto("20000")
        onUsaCocina(true)
        onReglamento(true)
    }

    "guardar() debe enviar la solicitud y actualizar los avisos" {
        val repo = repositorio()
        val avisos = mockk<AvisosRepository>(relaxed = true)
        coEvery { repo.crear(any()) } returns Resultado.Exito(DatosPrueba.reservaDto())
        val vm = crearViewModel(repo, avisos)

        vm.completarFormulario()
        vm.guardar()

        vm.form.value.guardadoOk shouldBe true
        coVerify { repo.crear(match { it.dependenciaId == 1 && it.personas == 10 && it.usaCocina }) }
        coVerify { avisos.refrescar() }
    }

    "guardar() no debe enviar si el horario aparece ocupado" {
        val repo = repositorio()
        coEvery { repo.disponibilidad(any(), any()) } returns Resultado.Exito(listOf(Horario("11:00", "13:00")))
        val vm = crearViewModel(repo)

        vm.completarFormulario()
        vm.guardar()

        vm.form.value.errores.horaInicio shouldBe "La dependencia ya está reservada en ese horario"
        coVerify(exactly = 0) { repo.crear(any()) }
    }

    "cambiarEstado() debe informar y notificar el cambio" {
        val repo = repositorio()
        coEvery { repo.cambiarEstado(1, EstadoReserva.CONFIRMADA, "") } returns
                Resultado.Exito(DatosPrueba.reservaDto())
        val vm = crearViewModel(repo)

        vm.cambiarEstado(DatosPrueba.reserva(), EstadoReserva.CONFIRMADA)

        vm.mensaje.value shouldBe "Reserva: confirmada"
        verify { NotificacionHelper.notificar(any(), 1, "Reserva: confirmada", any()) }
    }
})
