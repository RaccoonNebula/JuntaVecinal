package com.gestion.juntavecinos.pruebas

import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/** Los ViewModel usan Dispatchers.Main, que no existe en el PC: se reemplaza por uno de prueba */
@OptIn(ExperimentalCoroutinesApi::class)
fun StringSpec.usarDispatcherDePrueba() {
    beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterTest { Dispatchers.resetMain() }
}
