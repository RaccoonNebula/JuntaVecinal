package com.gestion.juntavecinos.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gestion.juntavecinos.ui.component.BotonAnimado
import com.gestion.juntavecinos.ui.component.CampoPassword
import com.gestion.juntavecinos.ui.component.CampoTexto
import com.gestion.juntavecinos.ui.component.MensajeError
import com.gestion.juntavecinos.ui.component.PantallaBase
import com.gestion.juntavecinos.ui.state.LoginErrores
import com.gestion.juntavecinos.ui.state.LoginFormState
import com.gestion.juntavecinos.ui.state.RegistroFormState
import com.gestion.juntavecinos.ui.theme.JuntaVecinosTheme

data class LoginAcciones(
    val onEmail: (String) -> Unit = {},
    val onPassword: (String) -> Unit = {},
    val onIniciar: () -> Unit = {},
    val onCrearCuenta: () -> Unit = {}
)

@Composable
fun LoginScreen(form: LoginFormState, mensaje: String? = null, acciones: LoginAcciones = LoginAcciones()) {
    PantallaBase(titulo = "Junta de Vecinos Zaror") { padding ->
        Column(
            Modifier.padding(padding).consumeWindowInsets(padding).imePadding().fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.Home, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp)
            )
            Text("Iniciar sesión", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Ingresa con tu cuenta para acceder según tu perfil.",
                textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 12.dp)
            )
            MensajeError(mensaje.takeIf { form.errorGeneral == null && !form.enviando })
            Column(Modifier.widthIn(max = 480.dp).fillMaxWidth()) {
                CampoTexto(form.email, acciones.onEmail, "Correo", Icons.Default.Email, form.errores.email,
                    teclado = KeyboardType.Email)
                CampoPassword(form.password, acciones.onPassword, "Contraseña", form.errores.password)
                MensajeError(form.errorGeneral)
                if (form.enviando) CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally).padding(8.dp))
                else BotonAnimado("Ingresar", Icons.AutoMirrored.Filled.Login, acciones.onIniciar, Modifier.fillMaxWidth())
                TextButton(onClick = acciones.onCrearCuenta, enabled = !form.enviando, modifier = Modifier.fillMaxWidth()) {
                    Text("¿Eres vecino/a y no tienes cuenta? Crear cuenta", textAlign = TextAlign.Center)
                }
            }
        }
    }
}

data class RegistroAcciones(
    val onNombre: (String) -> Unit = {},
    val onEmail: (String) -> Unit = {},
    val onTelefono: (String) -> Unit = {},
    val onPassword: (String) -> Unit = {},
    val onRepetir: (String) -> Unit = {},
    val onRegistrar: () -> Unit = {}
)

@Composable
fun RegistroScreen(form: RegistroFormState, acciones: RegistroAcciones = RegistroAcciones(), onBack: () -> Unit = {}) {
    PantallaBase(titulo = "Crear cuenta", onBack = onBack) { padding ->
        Column(
            Modifier.padding(padding).consumeWindowInsets(padding).imePadding().fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(Modifier.widthIn(max = 480.dp).fillMaxWidth()) {
                Text("La cuenta se crea con perfil Vecino/a. Usa solo datos ficticios.", Modifier.padding(bottom = 8.dp))
                CampoTexto(form.nombre, acciones.onNombre, "Nombre", Icons.Default.Person, form.errores.nombre)
                CampoTexto(form.email, acciones.onEmail, "Correo", Icons.Default.Email, form.errores.email,
                    teclado = KeyboardType.Email)
                CampoTexto(form.telefono, acciones.onTelefono, "Teléfono (9 dígitos)", Icons.Default.Phone,
                    form.errores.telefono, teclado = KeyboardType.Phone)
                CampoPassword(form.password, acciones.onPassword, "Contraseña (mínimo 8)", form.errores.password)
                CampoPassword(form.repetir, acciones.onRepetir, "Repetir contraseña", form.errores.repetir)
                MensajeError(form.errorGeneral)
                if (form.enviando) CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally).padding(8.dp))
                else BotonAnimado("Crear cuenta", Icons.Default.PersonAdd, acciones.onRegistrar, Modifier.fillMaxWidth())
            }
        }
    }
}

@Preview(showBackground = true, name = "Login")
@Composable
private fun LoginPreview() {
    JuntaVecinosTheme { LoginScreen(LoginFormState(errores = LoginErrores(email = "Ingresa un correo válido"))) }
}

@Preview(showBackground = true, name = "Registro")
@Composable
private fun RegistroPreview() {
    JuntaVecinosTheme { RegistroScreen(RegistroFormState()) }
}
