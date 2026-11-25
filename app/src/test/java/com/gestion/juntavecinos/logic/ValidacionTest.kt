package com.gestion.juntavecinos.logic

import com.gestion.juntavecinos.model.Horario
import com.gestion.juntavecinos.pruebas.DatosPrueba
import com.gestion.juntavecinos.ui.state.ReservaFormState
import com.gestion.juntavecinos.utils.FechaUtils
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class ValidacionTest : StringSpec({

    "telefono() debe tener 9 digitos y comenzar con 9" {
        Validacion.telefono("912345678").shouldBeNull()
        Validacion.telefono("") shouldBe "El teléfono es obligatorio"
        Validacion.telefono("9123") shouldBe "Debe tener 9 dígitos"
        Validacion.telefono("212345678") shouldBe "Debe comenzar con 9 (ej: 912345678)"
    }

    "fechaReserva() debe rechazar fechas pasadas e invalidas" {
        val ayer = FechaUtils.textoDesdeMs(FechaUtils.fechaHoyMs() - FechaUtils.UN_DIA_MS)
        Validacion.fechaReserva(DatosPrueba.fechaFutura).shouldBeNull()
        Validacion.fechaReserva(FechaUtils.fechaHoyTexto()).shouldBeNull()
        Validacion.fechaReserva("") shouldBe "Selecciona una fecha"
        Validacion.fechaReserva("31/02/2026") shouldBe "Fecha inválida"
        Validacion.fechaReserva(ayer) shouldBe "La fecha no puede ser anterior a hoy"
    }

    "personas() debe respetar la capacidad de la dependencia" {
        Validacion.personas("50", 50).shouldBeNull()
        Validacion.personas("", 50) shouldBe "Indica la cantidad de personas"
        Validacion.personas("abc", 50) shouldBe "Ingresa un número válido"
        Validacion.personas("0", 50) shouldBe "Debe ser al menos 1 persona"
        Validacion.personas("51", 50) shouldBe "Capacidad máxima: 50 personas"
    }

    "hayConflicto() debe detectar horarios superpuestos" {
        val ocupados = listOf(Horario("10:00", "12:00"))
        Validacion.hayConflicto("11:00", "13:00", ocupados) shouldBe true
        Validacion.hayConflicto("12:00", "13:00", ocupados) shouldBe false
        Validacion.hayConflicto("10:00", "12:00", emptyList()) shouldBe false
    }

    "validarReserva() debe aceptar datos correctos y exigir el reglamento" {
        val form = ReservaFormState(
            solicitante = "Ana Soto", telefono = "912345678", dependencia = "Salón", fecha = DatosPrueba.fechaFutura,
            horaInicio = "10:00", horaFin = "12:00", personas = "10", motivo = "Taller vecinal", monto = "20000",
            aceptaReglamento = true
        )
        Validacion.validarReserva(form, 50).hayErrores() shouldBe false
        Validacion.validarReserva(form.copy(aceptaReglamento = false), 50).reglamento shouldBe
                "Debes aceptar el reglamento de uso"
        Validacion.validarReserva(ReservaFormState(), 50).dependencia shouldBe "Selecciona una dependencia"
    }
})
