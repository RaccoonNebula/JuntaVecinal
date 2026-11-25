package com.gestion.juntavecinos.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RolTest {

    @Test
    fun `el vecino solicita reservas y ve finanzas publicas`() {
        assertTrue(Rol.VECINO.solicitaReservas)
        assertTrue(Rol.VECINO.veFinanzasPublicas)
        assertFalse(Rol.VECINO.resuelveReservas)
        assertFalse(Rol.VECINO.veFinanzas)
    }

    @Test
    fun `la directiva resuelve reservas y define la visibilidad`() {
        assertTrue(Rol.DIRECTIVA.resuelveReservas)
        assertTrue(Rol.DIRECTIVA.veCalendario)
        assertTrue(Rol.DIRECTIVA.defineVisibilidad)
        assertFalse(Rol.DIRECTIVA.registraMovimientos)
    }

    @Test
    fun `la tesoreria registra movimientos y genera informes`() {
        assertTrue(Rol.TESORERIA.registraMovimientos)
        assertTrue(Rol.TESORERIA.generaInformes)
        assertFalse(Rol.TESORERIA.resuelveReservas)
    }

    @Test
    fun `solo el admin administra y no ve reservas`() {
        assertEquals(listOf(Rol.ADMIN), Rol.entries.filter { it.administra })
        assertFalse(Rol.ADMIN.veReservas)
    }
}
