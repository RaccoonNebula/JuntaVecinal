package com.gestion.juntavecinos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8CD4C0),
    onPrimary = Color(0xFF00382D),
    primaryContainer = Color(0xFF00513F),
    onPrimaryContainer = Color(0xFFB7F0DE),
    secondary = Color(0xFFB1CCC2),
    tertiary = Color(0xFFA7CBE0)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1B6B5A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7F0DE),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = Color(0xFF4A635B),
    tertiary = Color(0xFF3F6375)
)

@Composable
fun JuntaVecinosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}