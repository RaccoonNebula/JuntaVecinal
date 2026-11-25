package com.gestion.juntavecinos.viewmodel

import com.gestion.juntavecinos.data.remote.Resultado
import com.gestion.juntavecinos.data.repository.FinanzasRepository
import com.gestion.juntavecinos.data.repository.ReservasRepository
import com.gestion.juntavecinos.model.EstadoReserva
import com.gestion.juntavecinos.model.Movimiento
import com.gestion.juntavecinos.model.Reserva
import com.gestion.juntavecinos.model.Rol
import com.gestion.juntavecinos.model.TipoMovimiento
import com.gestion.juntavecinos.pruebas.DatosPrueba
import com.gestion.juntavecinos.pruebas.usarDispatcherDePrueba
import com.gestion.juntavecinos.ui.state.ResumenFinanciero
import com.gestion.juntavecinos.utils.NotificacionHelper
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class FinanzasViewModelTest : StringSpec({

    usarDispatcherDePrueba()
    beforeTest {
        mockkObject(NotificacionHelper)
        every { NotificacionHelper.notificar(any(), any(), any(), any()) } just runs
    }
    afterTest { unmockkObject(NotificacionHelper) }

    fun movimiento(id: Int, tipo: TipoMovimiento, monto: Int, reservaId: Int? = null) =
        Movimiento(id, tipo, "Categoría", "Descripción", monto, "20/11/2026", 0L, reservaId)

    fun repositorio(vararg movimientos: Movimiento): FinanzasRepository {
        val repo = mockk<FinanzasRepository>(relaxed = true)
        every { repo.movimientos } returns flowOf(movimientos.toList())
        coEvery { repo.refrescar() } returns Resultado.Exito(Unit)
        coEvery { repo.categorias() } returns Resultado.Exito(listOf(DatosPrueba.categoriaIngreso))
        return repo
    }

    fun crearViewModel(repo: FinanzasRepository, reservas: List<Reserva> = emptyList()): FinanzasViewModel {
        val reservasRepo = mockk<ReservasRepository>(relaxed = true)
        every { reservasRepo.reservas } returns flowOf(reservas)
        return FinanzasViewModel(mockk(relaxed = true), repo, reservasRepo, DatosPrueba.sesion(Rol.TESORERIA))
    }

    fun FinanzasViewModel.completarFormulario() {
        refrescar()
        onTipo(TipoMovimiento.INGRESO)
        onCategoria("Cuota vecinal")
        onDescripcion("Cuota de prueba")
        onMonto("1000")
    }

    "resumen debe sumar ingresos y gastos para tesoreria" {
        val vm = crearViewModel(
            repositorio(movimiento(1, TipoMovimiento.INGRESO, 5000), movimiento(2, TipoMovimiento.GASTO, 2000))
        )
        runTest { vm.resumen.first { it?.ingresos == 5000L } shouldBe ResumenFinanciero(5000, 2000) }
    }

    "reservasSinPago debe mostrar solo confirmadas sin pago" {
        val confirmada = DatosPrueba.reserva(id = 1, estado = EstadoReserva.CONFIRMADA)
        val pagada = DatosPrueba.reserva(id = 2, estado = EstadoReserva.CONFIRMADA)
        val pendiente = DatosPrueba.reserva(id = 3)
        val vm = crearViewModel(
            repositorio(movimiento(1, TipoMovimiento.INGRESO, 20000, reservaId = 2)),
            reservas = listOf(confirmada, pagada, pendiente)
        )
        runTest { vm.reservasSinPago.first { it.isNotEmpty() } shouldBe listOf(confirmada) }
    }

    "guardar() debe reintentar solo la foto si el movimiento ya se guardo" {
        val repo = repositorio()
        coEvery { repo.registrar(any()) } returns Resultado.Exito(DatosPrueba.movimientoDto(id = 8))
        coEvery { repo.adjuntar(8, any()) } returnsMany listOf(Resultado.Error("Sin conexión"), Resultado.Exito(Unit))
        val vm = crearViewModel(repo)

        vm.completarFormulario()
        vm.onComprobante("foto-inexistente.jpg")
        vm.guardar()
        vm.form.value.movimientoCreadoId shouldBe 8
        vm.form.value.errorGeneral!! shouldContain "no se pudo adjuntar el comprobante"

        vm.guardar()
        vm.form.value.guardadoOk shouldBe true
        coVerify(exactly = 1) { repo.registrar(any()) }
    }
})
