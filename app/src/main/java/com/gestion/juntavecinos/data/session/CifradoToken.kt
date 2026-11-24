package com.gestion.juntavecinos.data.session

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Cifra el token con una clave AES que nunca sale de Android Keystore */
class CifradoToken {

    fun cifrar(texto: String): String {
        val cipher = Cipher.getInstance(TRANSFORMACION)
        cipher.init(Cipher.ENCRYPT_MODE, clave())
        val datos = cipher.iv + cipher.doFinal(texto.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(datos, Base64.NO_WRAP)
    }

    /** null si la clave ya no existe o el dato no es valido */
    fun descifrar(texto: String): String? = try {
        val datos = Base64.decode(texto, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMACION)
        cipher.init(Cipher.DECRYPT_MODE, clave(), GCMParameterSpec(128, datos, 0, LARGO_IV))
        String(cipher.doFinal(datos, LARGO_IV, datos.size - LARGO_IV), Charsets.UTF_8)
    } catch (e: Exception) {
        null
    }

    private fun clave(): SecretKey {
        val almacen = KeyStore.getInstance(PROVEEDOR).apply { load(null) }
        (almacen.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVEEDOR).apply {
            init(
                KeyGenParameterSpec.Builder(
                    ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
        }.generateKey()
    }

    private companion object {
        const val PROVEEDOR = "AndroidKeyStore"
        const val ALIAS = "junta_vecinos_sesion"
        const val TRANSFORMACION = "AES/GCM/NoPadding"
        const val LARGO_IV = 12
    }
}
