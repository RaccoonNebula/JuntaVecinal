package com.gestion.juntavecinos.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gestion.juntavecinos.model.EstadoReserva
import com.gestion.juntavecinos.ui.screen.FinanzasScreen
import com.gestion.juntavecinos.ui.screen.HomeScreen
import com.gestion.juntavecinos.ui.screen.MovimientoFormActiones
import com.gestion.juntavecinos.ui.screen.NuevaReservaScreen
import com.gestion.juntavecinos.ui.screen.NuevoMovimientoScreen
import com.gestion.juntavecinos.ui.screen.ReservaFormActiones
import com.gestion.juntavecinos.ui.screen.ReservasScreen
import com.gestion.juntavecinos.utils.CamaraUtils
import com.gestion.juntavecinos.viewmodel.FinanzasViewModel
import com.gestion.juntavecinos.viewmodel.ReservasViewModel
import java.io.File

object Rutas {
    const val HOME = "home"
    const val RESERVAS = "reservas"
    const val NUEVA_RESERVA = "nueva_reserva"
    const val FINANZAS = "finanzas"
    const val NUEVO_MOVIMIENTO = "nuevo_movimiento"
}

@Composable
fun AppNavHost(
    reservaVm: ReservasViewModel,
    finanzasVm: FinanzasViewModel = viewModel()
) {
    val nav = rememberNavController()

    val entrar = slideInHorizontally(tween(300)) { it } + fadeIn(tween(300))
    val salir = slideOutHorizontally(tween(300)) { -it / 3 } + fadeOut(tween(300))
    val volverEntrar = slideInHorizontally(tween(300)) { -it / 3 } + fadeIn(tween(300))
    val volverSalir = slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300))

    NavHost(
        navController = nav,
        startDestination = Rutas.HOME,
        enterTransition = { entrar },
        exitTransition = { salir },
        popEnterTransition = { volverEntrar },
        popExitTransition = { volverSalir }
    ) {
        composable(Rutas.HOME) {
            val resumen by finanzasVm.resumen.collectAsStateWithLifecycle()
            val proximas by reservaVm.proximas.collectAsStateWithLifecycle()
            HomeScreen(
                resumen = resumen,
                proximas = proximas,
                onReservas = { nav.navigate(Rutas.RESERVAS) },
                onNuevaReserva = { nav.navigate(Rutas.NUEVA_RESERVA) },
                onFinanzas = { nav.navigate(Rutas.FINANZAS) },
                onNuevoMovimiento = { nav.navigate(Rutas.NUEVO_MOVIMIENTO) }
            )
        }

        composable(Rutas.RESERVAS) {
            val reservas by reservaVm.reservasFiltradas.collectAsStateWithLifecycle()
            val busqueda by reservaVm.busqueda.collectAsStateWithLifecycle()
            ReservasScreen(
                reservas = reservas,
                busqueda = busqueda,
                onBusqueda = reservaVm::onBusqueda,
                onNueva = { nav.navigate(Rutas.NUEVA_RESERVA) },
                onConfirmar = { reservaVm.cambiarEstado(it, EstadoReserva.CONFIRMADA) },
                onCancelar = { reservaVm.cambiarEstado(it, EstadoReserva.CANCELADA) },
                onBack = { nav.popBackStack() }
            )
        }

        composable(Rutas.NUEVA_RESERVA) { NuevaReservaRoute(reservaVm, nav) }

        composable(Rutas.FINANZAS) {
            val movimientos by finanzasVm.movimientos.collectAsStateWithLifecycle()
            val resumen by finanzasVm.resumen.collectAsStateWithLifecycle()
            FinanzasScreen(
                movimientos = movimientos,
                resumen = resumen,
                onNuevo = { nav.navigate(Rutas.NUEVO_MOVIMIENTO) },
                onEliminar = finanzasVm::eliminar,
                onBack = { nav.popBackStack() }
            )
        }

        composable(Rutas.NUEVO_MOVIMIENTO) { NuevoMovimientoRoute(finanzasVm, nav) }
    }
}


/** Conexion con el ViewModel y la pantalla del formulario de reserva */
@Composable
private fun NuevaReservaRoute(vm: ReservasViewModel, nav: NavHostController) {
    val form by vm.form.collectAsStateWithLifecycle()
    val salir = { vm.limpiarFormulario(); nav.popBackStack(); Unit }

    LaunchedEffect(form.guardadoOk) { if (form.guardadoOk) salir() }
    BackHandler { salir() }

    NuevaReservaScreen(
        form = form,
        acciones = ReservaFormActiones(
            onSolicitante = vm::onSolicitante,
            onTelefono = vm::onTelefono,
            onDependencia = vm::onDependencia,
            onFecha = vm::onFecha,
            onHoraInicio = vm::onHoraInicio,
            onHoraFin = vm::onHoraFin,
            onPersonas = vm::onPersonas,
            onMotivo = vm::onMotivo,
            onMonto = vm::onMonto,
            onUsaCocina = vm::onUsaCocina,
            onReglamento = vm::onReglamento,
            onGuardar = vm::guardar
        ),
        onBack = salir
    )
}


/** Conexion de formulario de movimiento con el ViewModel y con la camara */
@Composable
private fun NuevoMovimientoRoute(vm: FinanzasViewModel, nav: NavHostController) {
    val contexto = LocalContext.current
    val form by vm.form.collectAsStateWithLifecycle()
    val salir = {
        // Si se sale sin guardar, se borra la foto tomada
        if (!form.guardadoOk) vm.quitarComprobante()
        vm.limpiarFormulario()
        nav.popBackStack()
        Unit
    }

    // Permite mantener funciones cuando la pantalla rota
    var rutaPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var mensajeCamara by rememberSaveable { mutableStateOf<String?>(null) }

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val ruta = rutaPendiente
        if (exito && ruta != null) {
            vm.quitarComprobante() // para reemplazar la foto si ya se tomó
            vm.onComprobante(ruta)
        } else {
            ruta?.let { File(it).delete() }
        }
        rutaPendiente = null
    }

    fun abrirCamara() {
        try {
            val archivo = CamaraUtils.crearArchivoComprobante(contexto)
            rutaPendiente = archivo.absolutePath
            camara.launch(CamaraUtils.uriArchivo(contexto, archivo))
        } catch (e: ActivityNotFoundException) {
            mensajeCamara = "No se encontró una aplicación de cámara en el dispositivo"
        }
    }

    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) { mensajeCamara = null; abrirCamara() }
        else mensajeCamara = "Sin permiso de cámara no se puede adjuntar el comprobante"
    }

    LaunchedEffect(form.guardadoOk) { if (form.guardadoOk) { vm.limpiarFormulario(); nav.popBackStack() } }
    BackHandler { salir() }

    NuevoMovimientoScreen(
        form = form,
        mensajeCamara = mensajeCamara,
        acciones = MovimientoFormActiones(
            onTipo = vm::onTipo,
            onCategoria = vm::onCategoria,
            onDescripcion = vm::onDescripcion,
            onMonto = vm::onMonto,
            onFecha = vm::onFecha,
            onTomarFoto = {
                val tienePermiso =
                    ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_GRANTED
                if (tienePermiso) abrirCamara() else permisoCamara.launch(Manifest.permission.CAMERA)
            },
            onQuitarFoto = vm::quitarComprobante,
            onGuardar = vm::guardar
        ),
        onBack = salir
    )
}
