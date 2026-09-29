package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import com.example.R

private val Primary = Color(0xFF128591)
private val PrimaryDark = Color(0xFF0C626A)
private val ActionBlue = Color(0xFF4BA8EB)
private val Canvas = Color(0xFFF4F7F7)
private val TextDark = Color(0xFF333333)

private val PersianSans = FontFamily(
    Font(R.font.iran_sans, FontWeight.Normal),
    Font(R.font.iran_sans_bold, FontWeight.Bold)
)

private val BaseTypography = Typography()
private val PersianTypography = Typography(
    displayLarge = BaseTypography.displayLarge.withPersianFont(),
    displayMedium = BaseTypography.displayMedium.withPersianFont(),
    displaySmall = BaseTypography.displaySmall.withPersianFont(),
    headlineLarge = BaseTypography.headlineLarge.withPersianFont(),
    headlineMedium = BaseTypography.headlineMedium.withPersianFont(),
    headlineSmall = BaseTypography.headlineSmall.withPersianFont(),
    titleLarge = BaseTypography.titleLarge.withPersianFont(),
    titleMedium = BaseTypography.titleMedium.withPersianFont(),
    titleSmall = BaseTypography.titleSmall.withPersianFont(),
    bodyLarge = BaseTypography.bodyLarge.withPersianFont(),
    bodyMedium = BaseTypography.bodyMedium.withPersianFont(),
    bodySmall = BaseTypography.bodySmall.withPersianFont(),
    labelLarge = BaseTypography.labelLarge.withPersianFont(),
    labelMedium = BaseTypography.labelMedium.withPersianFont(),
    labelSmall = BaseTypography.labelSmall.withPersianFont()
)

private fun TextStyle.withPersianFont() = copy(fontFamily = PersianSans)

private val BaarbargColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2F1F1),
    onPrimaryContainer = PrimaryDark,
    secondary = ActionBlue,
    onSecondary = Color.White,
    background = Canvas,
    onBackground = TextDark,
    surface = Color.White,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFE8EEEE),
    onSurfaceVariant = Color(0xFF5C6769),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

@Composable
fun BaarbargTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = BaarbargColorScheme,
            typography = PersianTypography,
            content = content
        )
    }
}
