package com.gestion.juntavecinos.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/** Crea el archivo (foto comprobante) y entrega la URI para su busqueda */
object CamaraUtils {
    private fun carpeta(context: Context) = File(context.filesDir, "comprobantes")

    fun crearArchivoComprobante(context: Context): File =
        File(carpeta(context).apply { mkdirs() }, "comprobante_${System.currentTimeMillis()}.jpg")

    fun uriArchivo(context: Context, archivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

    fun borrarTodo(context: Context) {
        carpeta(context).listFiles()?.forEach { it.delete() }
    }

}
