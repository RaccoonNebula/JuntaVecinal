package com.gestion.juntavecinos

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gestion.juntavecinos.ui.AppNavHost
import com.gestion.juntavecinos.ui.theme.JuntaVecinosTheme
import com.gestion.juntavecinos.utils.NotificacionHelper
import com.gestion.juntavecinos.viewmodel.ReservasViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificacionHelper.crearCanal(this)

        setContent {
            JuntaVecinosTheme {
                val reservaVm: ReservasViewModel = viewModel()

                // Permiso de notificaciones
                val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                    reservaVm.revisarRecordatorios()
                }
                LaunchedEffect(Unit) {
                    if (NotificacionHelper.tienePermiso(this@MainActivity)) {
                        reservaVm.revisarRecordatorios()
                    } else {
                        pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                AppNavHost(reservaVm = reservaVm)
            }
        }
    }
}
