package com.gestion.juntavecinos.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class FechaUtilsTest : StringSpec({

    "aIso() y desdeIso() deben convertir entre la app y el backend" {
        FechaUtils.aIso("15/12/2026") shouldBe "2026-12-15"
        FechaUtils.desdeIso("2026-12-15") shouldBe "15/12/2026"
    }
})
