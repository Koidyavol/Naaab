package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NaraDarkColorScheme = darkColorScheme(
    primary = NaraCyanAccent,
    onPrimary = NaraDarkBackground,
    primaryContainer = NaraSurfaceVariant,
    onPrimaryContainer = NaraTextPrimary,
    secondary = NaraCyanDark,
    onSecondary = NaraDarkBackground,
    background = NaraDarkBackground,
    onBackground = NaraTextPrimary,
    surface = NaraSurface,
    onSurface = NaraTextPrimary,
    surfaceVariant = NaraSurfaceVariant,
    onSurfaceVariant = NaraTextSecondary,
    outline = NaraBorder,
    error = NaraErrorRed,
    onError = NaraTextPrimary
)

private val NaraLightColorScheme = lightColorScheme(
    primary = NaraCyanDark,
    onPrimary = NaraTextPrimary,
    background = NaraDarkBackground,
    onBackground = NaraTextPrimary,
    surface = NaraSurface,
    onSurface = NaraTextPrimary
)

@Composable
fun NaraBrowserTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NaraDarkColorScheme else NaraLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
