package com.gestion.juntavecinos.ui

import android.Manifest
import android.app.Application
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gestion.juntavecinos.di.Contenedor
import com.gestion.juntavecinos.model.Sesion
import com.gestion.juntavecinos.ui.component.LocalEtiquetas
import com.gestion.juntavecinos.ui.screen.AccesoHome
import com.gestion.juntavecinos.ui.screen.AvisosScreen
import com.gestion.juntavecinos.ui.screen.CalendarioScreen
import com.gestion.juntavecinos.ui.screen.DetalleReservaScreen
import com.gestion.juntavecinos.ui.screen.FinanzasPublicasScreen
import com.gestion.juntavecinos.ui.screen.FinanzasScreen
import com.gestion.juntavecinos.ui.screen.HomeScreen
import com.gestion.juntavecinos.ui.screen.InformesScreen
import com.gestion.juntavecinos.ui.screen.MovimientoFormActiones
import com.gestion.juntavecinos.ui.screen.NuevaReservaScreen
import com.gestion.juntavecinos.ui.screen.NuevoMovimientoScreen
import com.gestion.juntavecinos.ui.screen.ReservaFormActiones
import com.gestion.juntavecinos.ui.screen.ReservasScreen
import com.gestion.juntavecinos.ui.screen.SedeScreen
import com.gestion.juntavecinos.ui.screen.VisibilidadScreen
import com.gestion.juntavecinos.ui.screen.admin.CatalogosScreen
import com.gestion.juntavecinos.ui.screen.admin.DependenciasScreen
import com.gestion.juntavecinos.ui.screen.admin.UsuariosScreen
import com.gestion.juntavecinos.utils.CamaraUtils
import com.gestion.juntavecinos.utils.NotificacionHelper
import com.gestion.juntavecinos.viewmodel.AdminViewModel
import com.gestion.juntavecinos.viewmodel.FinanzasViewModel
import com.gestion.juntavecinos.viewmodel.ReservasViewModel
import com.gestion.juntavecinos.viewmodel.SedeViewModel
import com.gestion.juntavecinos.viewmodel.SesionViewModel
import java.io.File

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val HOME = "home"
    const val RESERVAS = "reservas"
    const val RESERVA = "reserva"
    const val NUEVA_RESERVA = "nueva_reserva"
    const val CALENDARIO = "calendario"
    const val FINANZAS = "finanzas"
    const val FINANZAS_PUBLICAS = "finanzas_publicas"
    const val NUEVO_MOVIMIENTO = "nuevo_movimiento"
    const val INFORMES = "informes"
    const val VISIBILIDAD = "visibilidad"
    const val SEDE = "sede"
    const val AVISOS = "avisos"
    const val ADMIN_USUARIOS = "admin_usuarios"
    const val ADMIN_DEPENDENCIAS = "admin_dependencias"
    const val ADMIN_CATALOGOS = "admin_catalogos"
}

/** Cada perfil solo registra sus rutas: las demas no existen para esa sesion */
@Composable
fun AppNavHost(contenedor: Contenedor, sesion: Sesion, sesionVm: SesionViewModel) {
    val rol = sesion.rol
    val app = LocalContext.current.applicationContext as Application
    val clave = "${sesion.id}_${sesion.expiraEn}"

    val reservaVm: ReservasViewModel = viewModel(key = "reservas_$clave", factory = viewModelFactory {
        initializer { ReservasViewModel(app, contenedor.reservas, contenedor.sede, contenedor.avisos, sesion) }
    })
    val finanzasVm: FinanzasViewModel = viewModel(key = "finanzas_$clave", factory = viewModelFactory {
        initializer { FinanzasViewModel(app, contenedor.finanzas, contenedor.reservas, sesion) }
    })
    val sedeVm: SedeViewModel = viewModel(key = "sede_$clave", factory = viewModelFactory {
        initializer { SedeViewModel(contenedor.sede, contenedor.reservas, contenedor.avisos) }
    })
    val adminVm: AdminViewModel = viewModel(key = "admin_$clave", factory = viewModelFactory {
        initializer { AdminViewModel(contenedor.admin, contenedor.sede) }
    })

    PermisoNotificaciones { sedeVm.refrescarAvisos() }

    // Al volver a la app se confirma la cuenta y se actualizan los datos
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        sesionVm.validarSesion()
        if (rol.veReservas) reservaVm.refrescar()
        finanzasVm.refrescar()
        sedeVm.refrescarAvisos()
    }

    val nav = rememberNavController()
    val volver: () -> Unit = { nav.popBackStack() }
    val etiquetas by reservaVm.etiquetas.collectAsStateWithLifecycle()

    val entrar = slideInHorizontally(tween(300)) { it } + fadeIn(tween(300))
    val salir = slideOutHorizontally(tween(300)) { -it / 3 } + fadeOut(tween(300))
    val volverEntrar = slideInHorizontally(tween(300)) { -it / 3 } + fadeIn(tween(300))
    val volverSalir = slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300))

    CompositionLocalProvider(LocalEtiquetas provides etiquetas) {
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
                val avisos by sedeVm.avisos.collectAsStateWithLifecycle()
                val cargandoReservas by reservaVm.cargando.collectAsStateWithLifecycle()
                val cargandoFinanzas by finanzasVm.cargando.collectAsStateWithLifecycle()
                val mensajeReservas by reservaVm.mensaje.collectAsStateWithLifecycle()
                val mensajeFinanzas by finanzasVm.mensaje.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { sedeVm.refrescarAvisos() }
                HomeScreen(
                    sesion = sesion,
                    resumen = resumen,
                    proximas = proximas,
                    accesos = accesosDe(sesion, reservaVm) { nav.navigate(it) },
                    avisosSinLeer = avisos.count { !it.leida },
                    cargando = cargandoReservas || cargandoFinanzas,
                    mensaje = mensajeReservas ?: mensajeFinanzas,
                    onMensajeMostrado = { reservaVm.limpiarMensaje(); finanzasVm.limpiarMensaje() },
                    onAvisos = { nav.navigate(Rutas.AVISOS) },
                    onRefrescar = {
                        if (rol.veReservas) reservaVm.refrescar()
                        finanzasVm.refrescar()
                        sedeVm.refrescarAvisos()
                    },
                    onCerrarSesion = sesionVm::cerrarSesion
                )
            }

            composable(Rutas.SEDE) {
                val sede by sedeVm.sede.collectAsStateWithLifecycle()
                val dependencias by sedeVm.dependencias.collectAsStateWithLifecycle()
                val ocupados by sedeVm.ocupados.collectAsStateWithLifecycle()
                val cargando by sedeVm.cargando.collectAsStateWithLifecycle()
                val mensaje by sedeVm.mensaje.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { sedeVm.limpiarDisponibilidad(); sedeVm.refrescar() }
                SedeScreen(
                    sede = sede,
                    dependencias = dependencias.filter { it.activa },
                    ocupados = ocupados,
                    cargando = cargando,
                    mensaje = mensaje,
                    onMensajeMostrado = sedeVm::limpiarMensaje,
                    onConsultar = sedeVm::consultarDisponibilidad,
                    onCambio = sedeVm::limpiarDisponibilidad,
                    onBack = volver
                )
            }

            composable(Rutas.AVISOS) {
                val avisos by sedeVm.avisos.collectAsStateWithLifecycle()
                val cargando by sedeVm.cargando.collectAsStateWithLifecycle()
                val mensaje by sedeVm.mensaje.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { sedeVm.refrescarAvisosConError() }
                AvisosScreen(
                    avisos = avisos,
                    cargando = cargando,
                    mensaje = mensaje,
                    onMensajeMostrado = sedeVm::limpiarMensaje,
                    onAbrir = { aviso ->
                        if (!aviso.leida) sedeVm.marcarLeido(aviso.id)
                        val id = aviso.reservaId
                        if (id != null && rol.veReservas) {
                            reservaVm.refrescar()
                            nav.navigate("${Rutas.RESERVA}/$id")
                        }
                    },
                    onBack = volver
                )
            }

            if (rol.veReservas) {
                composable(Rutas.RESERVAS) {
                    val reservas by reservaVm.reservasFiltradas.collectAsStateWithLifecycle()
                    val busqueda by reservaVm.busqueda.collectAsStateWithLifecycle()
                    val cargando by reservaVm.cargando.collectAsStateWithLifecycle()
                    val mensaje by reservaVm.mensaje.collectAsStateWithLifecycle()
                    LaunchedEffect(Unit) { reservaVm.refrescar() }
                    ReservasScreen(
                        reservas = reservas,
                        busqueda = busqueda,
                        rol = rol,
                        cargando = cargando,
                        mensaje = mensaje,
                        onMensajeMostrado = reservaVm::limpiarMensaje,
                        onBusqueda = reservaVm::onBusqueda,
                        onNueva = { reservaVm.limpiarFormulario(); nav.navigate(Rutas.NUEVA_RESERVA) },
                        onAbrir = { nav.navigate("${Rutas.RESERVA}/${it.id}") },
                        onEstado = reservaVm::cambiarEstado,
                        onCorregir = { reservaVm.corregir(it); nav.navigate(Rutas.NUEVA_RESERVA) },
                        onBack = volver
                    )
                }

                composable(
                    "${Rutas.RESERVA}/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { entrada ->
                    val id = entrada.arguments?.getInt("id")
                    val reservas by reservaVm.reservas.collectAsStateWithLifecycle()
                    val sede by sedeVm.sede.collectAsStateWithLifecycle()
                    val cargando by reservaVm.cargando.collectAsStateWithLifecycle()
                    val mensaje by reservaVm.mensaje.collectAsStateWithLifecycle()
                    LaunchedEffect(Unit) { sedeVm.refrescar() }
                    DetalleReservaScreen(
                        reserva = reservas.firstOrNull { it.id == id },
                        rol = rol,
                        instruccionesPago = sede?.instruccionesPago,
                        cargando = cargando,
                        mensaje = mensaje,
                        onMensajeMostrado = reservaVm::limpiarMensaje,
                        onEstado = reservaVm::cambiarEstado,
                        onObservar = reservaVm::observar,
                        onCorregir = { reservaVm.corregir(it); nav.navigate(Rutas.NUEVA_RESERVA) },
                        onBack = volver
                    )
                }
            }

            if (rol.solicitaReservas) {
                composable(Rutas.NUEVA_RESERVA) { NuevaReservaRoute(reservaVm, nav) }
            }

            if (rol.veCalendario) {
                composable(Rutas.CALENDARIO) {
                    val reservas by reservaVm.reservas.collectAsStateWithLifecycle()
                    val cargando by reservaVm.cargando.collectAsStateWithLifecycle()
                    LaunchedEffect(Unit) { reservaVm.refrescar() }
                    CalendarioScreen(
                        reservas = reservas,
                        cargando = cargando,
                        onAbrir = { nav.navigate("${Rutas.RESERVA}/${it.id}") },
                        onBack = volver
                    )
                }
            }

            if (rol.veFinanzas) {
                composable(Rutas.FINANZAS) {
                    val movimientos by finanzasVm.movimientos.collectAsStateWithLifecycle()
                    val resumen by finanzasVm.resumen.collectAsStateWithLifecycle()
                    val cargando by finanzasVm.cargando.collectAsStateWithLifecycle()
                    val mensaje by finanzasVm.mensaje.collectAsStateWithLifecycle()
                    LaunchedEffect(Unit) { finanzasVm.refrescar() }
                    FinanzasScreen(
                        movimientos = movimientos,
                        resumen = resumen,
                        puedeRegistrar = rol.registraMovimientos,
                        cargando = cargando,
                        mensaje = mensaje,
                        onMensajeMostrado = finanzasVm::limpiarMensaje,
                        onNuevo = { nav.navigate(Rutas.NUEVO_MOVIMIENTO) },
                        onEliminar = finanzasVm::anular,
                        onBack = volver
                    )
                }
            }

            if (rol.veFinanzasPublicas) {
                composable(Rutas.FINANZAS_PUBLICAS) {
                    val publicas by finanzasVm.publicas.collectAsStateWithLifecycle()
                    val cargando by finanzasVm.cargando.collectAsStateWithLifecycle()
                    val mensaje by finanzasVm.mensaje.collectAsStateWithLifecycle()
                    LaunchedEffect(Unit) { finanzasVm.refrescar() }
                    FinanzasPublicasScreen(publicas, cargando, mensaje, finanzasVm::limpiarMensaje, volver)
                }
            }

            if (rol.registraMovimientos) {
                composable(Rutas.NUEVO_MOVIMIENTO) {
                    LaunchedEffect(Unit) { finanzasVm.refrescar(); reservaVm.refrescar() }
                    NuevoMovimientoRoute(finanzasVm, nav)
                }
            }

            if (rol.generaInformes) {
                composable(Rutas.INFORMES) {
                    val informe by finanzasVm.informe.collectAsStateWithLifecycle()
                    val cargando by finanzasVm.cargando.collectAsStateWithLifecycle()
                    val mensaje by finanzasVm.mensaje.collectAsStateWithLifecycle()
                    InformesScreen(
                        informe, cargando, mensaje, finanzasVm::limpiarMensaje, finanzasVm::generarInforme, volver
                    )
                }
            }

            if (rol.defineVisibilidad) {
                composable(Rutas.VISIBILIDAD) {
                    val visibilidad by finanzasVm.visibilidad.collectAsStateWithLifecycle()
                    val cargando by finanzasVm.cargando.collectAsStateWithLifecycle()
                    val mensaje by finanzasVm.mensaje.collectAsStateWithLifecycle()
                    LaunchedEffect(Unit) { finanzasVm.cargarVisibilidad() }
                    VisibilidadScreen(
                        visibilidad, cargando, mensaje, finanzasVm::limpiarMensaje, finanzasVm::cambiarVisibilidad, volver
                    )
                }
            }

            if (rol.administra) {
                composable(Rutas.ADMIN_USUARIOS) { UsuariosScreen(adminVm, sesion.id, volver) }
                composable(Rutas.ADMIN_DEPENDENCIAS) { DependenciasScreen(adminVm, volver) }
                composable(Rutas.ADMIN_CATALOGOS) { CatalogosScreen(adminVm, volver) }
            }
        }
    }
}

/** Accesos de la pantalla de inicio segun el perfil */
private fun accesosDe(sesion: Sesion, reservaVm: ReservasViewModel, ir: (String) -> Unit): List<AccesoHome> {
    val rol = sesion.rol
    return buildList {
        if (rol.solicitaReservas) add(AccesoHome(Icons.Default.Add, "Solicitar reserva") {
            reservaVm.limpiarFormulario(); ir(Rutas.NUEVA_RESERVA)
        })
        if (rol.veReservas) add(
            AccesoHome(
                Icons.Default.EventAvailable,
                when {
                    rol.solicitaReservas -> "Mis reservas"
                    rol.resuelveReservas -> "Solicitudes"
                    else -> "Reservas confirmadas"
                }
            ) { ir(Rutas.RESERVAS) }
        )
        if (rol.veCalendario) add(AccesoHome(Icons.Default.CalendarMonth, "Calendario") { ir(Rutas.CALENDARIO) })
        if (rol.registraMovimientos) add(AccesoHome(Icons.Default.Paid, "Registrar pago/gasto") { ir(Rutas.NUEVO_MOVIMIENTO) })
        if (rol.veFinanzas) add(AccesoHome(Icons.Default.AccountBalanceWallet, "Ver finanzas") { ir(Rutas.FINANZAS) })
        if (rol.generaInformes) add(AccesoHome(Icons.Default.Assessment, "Informes") { ir(Rutas.INFORMES) })
        if (rol.veFinanzasPublicas) add(AccesoHome(Icons.Default.AccountBalanceWallet, "Finanzas") { ir(Rutas.FINANZAS_PUBLICAS) })
        if (rol.defineVisibilidad) add(AccesoHome(Icons.Default.Visibility, "Finanzas visibles") { ir(Rutas.VISIBILIDAD) })
        if (rol.administra) {
            add(AccesoHome(Icons.Default.Groups, "Usuarios") { ir(Rutas.ADMIN_USUARIOS) })
            add(AccesoHome(Icons.Default.MeetingRoom, "Dependencias") { ir(Rutas.ADMIN_DEPENDENCIAS) })
            add(AccesoHome(Icons.Default.Category, "Catálogos y sede") { ir(Rutas.ADMIN_CATALOGOS) })
        }
        add(AccesoHome(Icons.Default.Info, "Información de la sede") { ir(Rutas.SEDE) })
    }
}

/** Pide el permiso de notificaciones una vez iniciada la sesion */
@Composable
private fun PermisoNotificaciones(alConceder: () -> Unit) {
    val contexto = LocalContext.current
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) alConceder()
    }
    LaunchedEffect(Unit) {
        if (!NotificacionHelper.tienePermiso(contexto)) pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}


/** Conexion con el ViewModel y la pantalla del formulario de reserva */
@Composable
private fun NuevaReservaRoute(vm: ReservasViewModel, nav: NavHostController) {
    val form by vm.form.collectAsStateWithLifecycle()
    val dependencias by vm.dependencias.collectAsStateWithLifecycle()
    val salir = { vm.limpiarFormulario(); nav.popBackStack(); Unit }

    LaunchedEffect(Unit) { vm.refrescar() }
    LaunchedEffect(form.guardadoOk) { if (form.guardadoOk) salir() }
    BackHandler { salir() }

    NuevaReservaScreen(
        form = form,
        dependencias = dependencias.filter { it.activa },
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
    val categorias by vm.categorias.collectAsStateWithLifecycle()
    val reservasSinPago by vm.reservasSinPago.collectAsStateWithLifecycle()
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
        categorias = categorias.filter { it.tipo == form.tipo }.map { it.nombre },
        reservas = reservasSinPago,
        mensajeCamara = mensajeCamara,
        acciones = MovimientoFormActiones(
            onTipo = vm::onTipo,
            onCategoria = vm::onCategoria,
            onDescripcion = vm::onDescripcion,
            onMonto = vm::onMonto,
            onFecha = vm::onFecha,
            onReserva = vm::onReserva,
            onMedioPago = vm::onMedioPago,
            onReferencia = vm::onReferencia,
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
