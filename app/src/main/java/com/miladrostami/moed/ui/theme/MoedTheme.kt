package com.miladrostami.moed.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MoedColors = lightColorScheme(
    primary = Color(0xFF5B5CE2), onPrimary = Color.White,
    background = Color(0xFFF7F7F9), onBackground = Color(0xFF171923),
    surface = Color.White, onSurface = Color(0xFF171923),
    error = Color(0xFFBD4054)
)

@Composable fun MoedTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = MoedColors, content = content)
}
