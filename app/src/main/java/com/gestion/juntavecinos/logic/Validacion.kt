package com.gestion.juntavecinos.logic

import com.gestion.juntavecinos.model.Catalogo
import com.gestion.juntavecinos.model.Reserva
import com.gestion.juntavecinos.ui.state.MovimientoErrores
import com.gestion.juntavecinos.ui.state.MovimientoFormState
import com.gestion.juntavecinos.ui.state.ReservaErrores
import com.gestion.juntavecinos.ui.state.ReservaFormState
import com.gestion.juntavecinos.utils.FechaUtils

object Validacion {

    fun solicitante(v: String): String? {
        val t = v.trim()
        return when {
            t.isEmpty() -> "El nombre es obligatorio"
            t.length < 3 -> "Debe tener al menos 3 caracteres"
            !t.all { it.isLetter() || it == ' ' } -> "Solo se permiten letras y espacios"
            else -> null
        }
    }

    fun telefono(v: String): String? = when {
        v.isEmpty() -> "El teléfono es obligatorio"
        v.length != 9 -> "Debe tener 9 dígitos"
        !v.startsWith("9") -> "Debe comenzar con 9 (ej: 912345678)"
        else -> null
    }

    fun dependencia(v: String): String? = if (v.isBlank()) "Selecciona una dependencia" else null

    fun fechaReserva(v: String): String? {
        if (v.isBlank()) return "Selecciona una fecha"
        val millis = FechaUtils.conversionMs(v) ?: return "Fecha inválida"
        return if (millis < FechaUtils.fechaHoyMs()) "La fecha no puede ser anterior a hoy" else null
    }

    fun fechaMovimiento(v: String): String? {
        if (v.isBlank()) return "Selecciona una fecha"
        val millis = FechaUtils.conversionMs(v) ?: return "Fecha inválida"
        return if (millis > FechaUtils.fechaHoyMs()) "La fecha no puede ser futura" else null
    }

    fun horaInicio(v: String): String? = if (v.isBlank()) "Selecciona la hora de inicio" else null

    fun horaFin(inicio: String, fin: String): String? = when {
        fin.isBlank() -> "Selecciona la hora de término"
        inicio.isNotBlank() && fin <= inicio -> "Debe ser posterior al inicio"
        else -> null
    }

    fun personas(v: String, dependencia: String): String? {
        if (v.isBlank()) return "Indica la cantidad de personas"
        val n = v.toIntOrNull() ?: return "Ingresa un número válido"
        if (n <= 0) return "Debe ser al menos 1 persona"
        val capacidad = Catalogo.capacidadDe(dependencia)
        return if (capacidad in 1..<n) "Capacidad máxima: $capacidad personas" else null
    }

    fun motivo(v: String): String? = when {
        v.isBlank() -> "Indica el motivo del arriendo"
        v.trim().length < 5 -> "Describe el motivo (mínimo 5 caracteres)"
        else -> null
    }

    fun monto(v: String): String? {
        if (v.isBlank()) return "El monto es obligatorio"
        val n = v.toIntOrNull() ?: return "Ingresa un monto válido"
        return when {
            n <= 0 -> "El monto debe ser mayor a 0"
            n > 10_000_000 -> "El monto es demasiado alto"
            else -> null
        }
    }

    fun reglamento(aceptado: Boolean): String? = if (!aceptado) "Debes aceptar el reglamento de uso" else null

    fun categoria(v: String): String? = if (v.isBlank()) "Selecciona una categoría" else null

    fun descripcion(v: String): String? = when {
        v.isBlank() -> "La descripción es obligatoria"
        v.trim().length < 5 -> "Describe el movimiento (mínimo 5 caracteres)"
        else -> null
    }

    /** Metodos para evitar que dos reservas de la misma dependencia
     * en horarios que se pisan se guarden */
    fun hayConflicto(inicio: String, fin: String, existentes: List<Reserva>): Boolean =
        existentes.any { inicio < it.horaFin && fin > it.horaInicio }

    fun validarReserva(f: ReservaFormState) = ReservaErrores(
        solicitante = solicitante(f.solicitante),
        telefono = telefono(f.telefono),
        dependencia = dependencia(f.dependencia),
        fecha = fechaReserva(f.fecha),
        horaInicio = horaInicio(f.horaInicio),
        horaFin = horaFin(f.horaInicio, f.horaFin),
        personas = personas(f.personas, f.dependencia),
        motivo = motivo(f.motivo),
        monto = monto(f.monto),
        reglamento = reglamento(f.aceptaReglamento)
    )

    fun validarMovimiento(f: MovimientoFormState) = MovimientoErrores(
        categoria = categoria(f.categoria),
        descripcion = descripcion(f.descripcion),
        monto = monto(f.monto),
        fecha = fechaMovimiento(f.fecha)
    )

}