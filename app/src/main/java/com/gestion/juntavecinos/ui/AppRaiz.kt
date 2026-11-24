package com.gestion.juntavecinos.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gestion.juntavecinos.data.session.EstadoSesion
import com.gestion.juntavecinos.di.Contenedor
import com.gestion.juntavecinos.ui.screen.LoginAcciones
import com.gestion.juntavecinos.ui.screen.LoginScreen
import com.gestion.juntavecinos.ui.screen.RegistroAcciones
import com.gestion.juntavecinos.ui.screen.RegistroScreen
import com.gestion.juntavecinos.viewmodel.SesionViewModel

/** Decide entre el acceso y la aplicacion segun la sesion. Sin sesion no existe ninguna otra ruta. */
@Composable
fun AppRaiz(contenedor: Contenedor) {
    val sesionVm: SesionViewModel = viewModel(
        factory = viewModelFactory { initializer { SesionViewModel(contenedor.auth) } }
    )
    PermisoRedLocal()

    val estado by sesionVm.estado.collectAsStateWithLifecycle()

    when (val e = estado) {
        EstadoSesion.Cargando -> Surface(Modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
        is EstadoSesion.SinSesion -> AccesoNavHost(sesionVm, e.mensaje)
        // key: al cambiar de cuenta se descarta todo el estado de la anterior
        is EstadoSesion.Activa -> key(e.sesion.token) { AppNavHost(contenedor, e.sesion, sesionVm) }
    }
}

/** Android 17 o superior bloquea la red local (donde esta el backend de desarrollo) sin este permiso */
@Composable
private fun PermisoRedLocal() {
    if (Build.VERSION.SDK_INT < 37) return
    val contexto = LocalContext.current
    val pedir = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        val concedido = ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_LOCAL_NETWORK) ==
                PackageManager.PERMISSION_GRANTED
        if (!concedido) pedir.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
    }
}

@Composable
private fun AccesoNavHost(vm: SesionViewModel, mensaje: String?) {
    val nav = rememberNavController()
    NavHost(nav, startDestination = Rutas.LOGIN) {
        composable(Rutas.LOGIN) {
            val form by vm.login.collectAsStateWithLifecycle()
            LoginScreen(
                form = form,
                mensaje = mensaje,
                acciones = LoginAcciones(
                    onEmail = vm::onEmail,
                    onPassword = vm::onPassword,
                    onIniciar = vm::iniciarSesion,
                    onCrearCuenta = { vm.limpiarFormularios(); nav.navigate(Rutas.REGISTRO) }
                )
            )
        }
        composable(Rutas.REGISTRO) {
            val form by vm.registro.collectAsStateWithLifecycle()
            RegistroScreen(
                form = form,
                acciones = RegistroAcciones(
                    onNombre = vm::onRegNombre,
                    onEmail = vm::onRegEmail,
                    onTelefono = vm::onRegTelefono,
                    onPassword = vm::onRegPassword,
                    onRepetir = vm::onRegRepetir,
                    onRegistrar = vm::registrar
                ),
                onBack = { vm.limpiarFormularios(); nav.popBackStack() }
            )
        }
    }
}
