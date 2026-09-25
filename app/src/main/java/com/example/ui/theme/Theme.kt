package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val IndustrialDarkColorScheme = darkColorScheme(
    primary = CopperPrimary,
    onPrimary = FaenaBg,
    primaryContainer = CopperPrimaryContainer,
    onPrimaryContainer = OnCopperPrimaryContainer,
    secondary = TelemetryCyan,
    onSecondary = OnTelemetryCyan,
    secondaryContainer = TelemetryCyanContainer,
    onSecondaryContainer = Color.White,
    tertiary = IndustrialEmerald,
    onTertiary = OnIndustrialEmerald,
    tertiaryContainer = IndustrialEmeraldContainer,
    onTertiaryContainer = Color.White,
    background = SurfaceDark,
    onBackground = OnSurface,
    surface = SurfaceDark,
    onSurface = OnSurface,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = OnSurfaceVariant,
    error = AlertRed,
    onError = Color.Black,
    errorContainer = AlertRedContainer,
    onErrorContainer = OnAlertRedContainer,
    outline = OutlineColor,
    outlineVariant = BorderIndustrial
)

val ModoFaenaColorScheme = darkColorScheme(
    primary = FaenaYellow,
    onPrimary = Color.Black,
    primaryContainer = FaenaYellow,
    onPrimaryContainer = Color.Black,
    secondary = Color.White,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF222222),
    onSecondaryContainer = FaenaYellow,
    tertiary = FaenaYellow,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF333333),
    onTertiaryContainer = FaenaYellow,
    background = FaenaBg,
    onBackground = Color.White,
    surface = FaenaBg,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF141414),
    onSurfaceVariant = Color(0xFFE0E0E0),
    error = Color(0xFFFF4444),
    onError = Color.White,
    errorContainer = Color(0xFF660000),
    onErrorContainer = Color.White,
    outline = FaenaYellow,
    outlineVariant = Color.White
)

@Composable
fun AdnMineroTheme(
    isModoFaena: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isModoFaena) ModoFaenaColorScheme else IndustrialDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
