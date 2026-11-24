package com.gestion.juntavecinos.model

/** Valores fijos para los formularios. Dependencias y categorias se obtienen del backend. */
object Catalogo {
    val horas: List<String> = (8..23).map { "%02d:00".format(it) }
    val mediosPago = listOf("Efectivo", "Transferencia", "Otro")
}
