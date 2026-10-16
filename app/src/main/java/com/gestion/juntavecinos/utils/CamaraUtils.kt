package com.gestion.juntavecinos.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/** Crea el archivo (foto comprobante) y entrega la URI para su busqueda */
object CamaraUtils {
    fun crearArchivoComprobante(context: Context): File {
        val carpeta = File(context.filesDir, "comprobantes").apply { mkdirs() }
        return File(carpeta, "comprobante_${System.currentTimeMillis()}.jpg")
    }

    fun uriArchivo(context: Context, archivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

}