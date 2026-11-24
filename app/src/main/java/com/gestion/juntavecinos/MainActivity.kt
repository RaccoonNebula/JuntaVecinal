package com.gestion.juntavecinos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gestion.juntavecinos.ui.AppRaiz
import com.gestion.juntavecinos.ui.theme.JuntaVecinosTheme
import com.gestion.juntavecinos.utils.NotificacionHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificacionHelper.crearCanal(this)

        setContent {
            JuntaVecinosTheme {
                AppRaiz((application as JuntaVecinosApp).contenedor)
            }
        }
    }
}
