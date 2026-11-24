package com.gestion.juntavecinos.model

/** Perfiles de acceso. El backend vuelve a validar cada permiso; aqui solo se ajusta la interfaz. */
enum class Rol(val etiqueta: String) {
    VECINO("Vecino/a"),
    DIRECTIVA("Directiva / Secretaría"),
    TESORERIA("Tesorería"),
    ADMIN("Administrador");

    val solicitaReservas: Boolean get() = this == VECINO
    val resuelveReservas: Boolean get() = this == DIRECTIVA
    val veReservas: Boolean get() = this != ADMIN
    val veCalendario: Boolean get() = this == DIRECTIVA
    val veFinanzas: Boolean get() = this == DIRECTIVA || this == TESORERIA
    val veFinanzasPublicas: Boolean get() = this == VECINO
    val registraMovimientos: Boolean get() = this == TESORERIA
    val generaInformes: Boolean get() = this == TESORERIA
    val defineVisibilidad: Boolean get() = this == DIRECTIVA
    val administra: Boolean get() = this == ADMIN
}

/** Datos minimos de la sesion iniciada. El token se guarda cifrado y la contraseña nunca se guarda. */
data class Sesion(
    val id: Int,
    val nombre: String,
    val email: String,
    val telefono: String,
    val rol: Rol,
    val token: String,
    val expiraEn: Long
)
