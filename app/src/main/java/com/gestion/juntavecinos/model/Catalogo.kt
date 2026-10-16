package com.gestion.juntavecinos.model

data class Dependencia(val nombre: String, val capacidad: Int)

/** Valores fijos para los formularios */
object Catalogo {
    val dependencias = listOf(
        Dependencia("Salon multiuso", 80),
        Dependencia("Sala de reuniones", 25),
        Dependencia("Patio techado", 120)
    )
    val horas: List<String> = (8..23).map { "%02d:00".format(it) }
    val categoriasIngreso = listOf("Arriendo de sede", "Cuota vecinal", "Otro ingreso")
    val categoriasGasto = listOf("Luz", "Agua", "Gas", "Internet", "Mantención", "Aseo", "Otro gasto")

    fun capacidadDe(nombre: String): Int = dependencias.firstOrNull { it.nombre == nombre }?.capacidad ?: 0
}
