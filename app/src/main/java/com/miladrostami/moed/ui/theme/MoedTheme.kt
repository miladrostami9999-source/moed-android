package com.miladrostami.moed.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

private val MoedColors = lightColorScheme(
    primary = Color(0xFF315BFF), onPrimary = Color.White,
    background = Color(0xFFF7F7F9), onBackground = Color(0xFF171923),
    surface = Color.White, onSurface = Color(0xFF171923),
    primaryContainer = Color(0xFFE8EEFF), onPrimaryContainer = Color(0xFF153A9B),
    secondaryContainer = Color(0xFFDDF5EE), onSecondaryContainer = Color(0xFF173F36),
    surfaceVariant = Color(0xFFEDF0F5), onSurfaceVariant = Color(0xFF596373),
    outline = Color(0xFFB6C0CE), outlineVariant = Color(0xFFE3E8F0),
    error = Color(0xFFBD4054)
)
private val MentoDarkColors = darkColorScheme(
    primary = Color(0xFF9BB1FF), onPrimary = Color(0xFF00277C),
    background = Color(0xFF10121A), onBackground = Color(0xFFF1F2F7),
    surface = Color(0xFF181B25), onSurface = Color(0xFFF1F2F7),
    primaryContainer = Color(0xFF25375C), onPrimaryContainer = Color(0xFFDCE5FF),
    secondaryContainer = Color(0xFF173E36), onSecondaryContainer = Color(0xFFB4F0DA),
    surfaceVariant = Color(0xFF222633), onSurfaceVariant = Color(0xFFC4C7D2),
    outline = Color(0xFF596273), outlineVariant = Color(0xFF303947), error = Color(0xFFFFB2BE)
)

@Composable fun MoedTheme(mode:String="system",content: @Composable () -> Unit) {
    val dark=when(mode){"dark"->true;"light"->false;else->isSystemInDarkTheme()}
    val view=LocalView.current
    if(!view.isInEditMode)SideEffect {
        (view.context as? android.app.Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window,view).apply {
                isAppearanceLightStatusBars=!dark
                isAppearanceLightNavigationBars=!dark
            }
            @Suppress("DEPRECATION")
            window.statusBarColor=(if(dark)MentoDarkColors else MoedColors).background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor=(if(dark)MentoDarkColors else MoedColors).surface.toArgb()
        }
    }
    MaterialTheme(colorScheme = if (dark) MentoDarkColors else MoedColors, content = content)
}
