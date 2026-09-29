package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

val DarkPrimary = Color(0xFF00D2D3)
val DarkOnPrimary = Color(0xFF0A0F1D)
val DarkPrimaryContainer = Color(0xFF0E3E43)
val DarkOnPrimaryContainer = Color(0xFF8CEFF0)

val DarkBackground = Color(0xFF0A0F1D)
val DarkSurface = Color(0xFF151B2B)
val DarkSurfaceVariant = Color(0xFF1E263B)
val DarkOnSurface = Color(0xFFF1F5F9)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)

val DarkError = Color(0xFFFF6B6B)
val DarkSuccess = Color(0xFF10B981)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError
)

@Composable
fun BaarbargTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            content = content
        )
    }
}
