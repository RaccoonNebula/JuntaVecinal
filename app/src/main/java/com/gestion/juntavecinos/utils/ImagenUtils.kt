package com.gestion.juntavecinos.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File

object ImagenUtils {
    private const val LADO_MAXIMO = 1600

    /** Reduce la foto y quita sus metadatos (por ejemplo, la ubicacion) antes de enviarla */
    fun prepararJpeg(archivo: File): ByteArray? = try {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(archivo.path, limites)
        var muestra = 1
        while (maxOf(limites.outWidth, limites.outHeight) / (muestra * 2) >= LADO_MAXIMO) muestra *= 2

        BitmapFactory.decodeFile(archivo.path, BitmapFactory.Options().apply { inSampleSize = muestra })?.let { original ->
            val escala = minOf(1f, LADO_MAXIMO.toFloat() / maxOf(original.width, original.height))
            val matriz = Matrix().apply {
                postScale(escala, escala)
                postRotate(giro(archivo))
            }
            val lista = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matriz, true)
            ByteArrayOutputStream().use {
                lista.compress(Bitmap.CompressFormat.JPEG, 85, it)
                it.toByteArray()
            }
        }
    } catch (e: Exception) {
        null
    }

    private fun giro(archivo: File): Float =
        when (ExifInterface(archivo.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
}
