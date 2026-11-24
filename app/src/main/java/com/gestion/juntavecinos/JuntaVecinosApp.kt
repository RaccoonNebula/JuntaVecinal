package com.gestion.juntavecinos

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.gestion.juntavecinos.di.Contenedor

class JuntaVecinosApp : Application(), ImageLoaderFactory {

    lateinit var contenedor: Contenedor
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = Contenedor(this)
    }

    /** Los comprobantes se descargan con el token de la sesion y no se guardan en disco */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .okHttpClient { contenedor.http }
        .diskCache(null)
        .build()
}
