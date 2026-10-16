package com.gestion.juntavecinos.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object FechaUtils {
    private const val FORMATO = "dd/MM/yyyy"
    private val formatoLocalChile: Locale = Locale.forLanguageTag("es-CL")

    /** Entrega la fecha en (milisegundos) */
    fun desdeDatePicker(ms: Long): String {
        val f = SimpleDateFormat(FORMATO, formatoLocalChile)
        f.timeZone = TimeZone.getTimeZone("UTC")
        return f.format(Date(ms))
    }

    /** Convierte formato dd/MM/yyyy a milisegundos (usando formato Chile) */
    fun conversionMs(fecha: String): Long? = try {
        val f = SimpleDateFormat(FORMATO, formatoLocalChile)
        f.isLenient = false
        f.parse(fecha)?.time
    } catch (e: Exception) {
        null
    }

    fun fechaHoyMs(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun fechaHoyTexto(): String = SimpleDateFormat(FORMATO, formatoLocalChile).format(Date())

    const val UN_DIA_MS = 24L * 60 * 60 * 1000
}

/** Transforma Int a formato pesos */
fun Int.conversionPesos(): String = "$" + NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CL")).format(this)
