package com.gestion.juntavecinos.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.gestion.juntavecinos.MainActivity

object NotificacionHelper {
    private const val CANAL_ID = "arriendos_canal"

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID, "Arriendos y pagos", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Confirmaciones de reservas, pagos y recordatorios" }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }

    /** Funciones para permitir activar notificaciones (TIRAMISU=Version 13 Android)*/
    fun requierePermiso(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun tienePermiso(context: Context): Boolean =
        !NotificacionHelper.requierePermiso() ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // se revisa con tienePermiso()
    fun notificar(context: Context, id: Int, titulo: String, mensaje: String) {
        if (!NotificacionHelper.tienePermiso(context)) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendiente = PendingIntent.getActivity(
            context, id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificacion = NotificationCompat.Builder(context,
            NotificacionHelper.CANAL_ID
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setContentIntent(pendiente)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notificacion)
    }

}