package com.miladrostami.moed.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

private val MoedColors = lightColorScheme(
    primary = Color(0xFF315BFF), onPrimary = Color.White,
    background = Color(0xFFF7F7F9), onBackground = Color(0xFF171923),
    surface = Color.White, onSurface = Color(0xFF171923),
    error = Color(0xFFBD4054)
)
private val MentoDarkColors = darkColorScheme(
    primary = Color(0xFF9BB1FF), onPrimary = Color(0xFF00277C),
    background = Color(0xFF10121A), onBackground = Color(0xFFF1F2F7),
    surface = Color(0xFF181B25), onSurface = Color(0xFFF1F2F7),
    surfaceVariant = Color(0xFF222633), onSurfaceVariant = Color(0xFFC4C7D2),
    outline = Color(0xFF3D4250), error = Color(0xFFFFB2BE)
)

@Composable fun MoedTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) MentoDarkColors else MoedColors, content = content)
}
