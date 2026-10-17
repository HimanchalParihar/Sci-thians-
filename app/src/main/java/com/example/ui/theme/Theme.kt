package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = UtopiaRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4C0815),
    onPrimaryContainer = Color(0xFFFFD9DF),
    secondary = UtopiaSaffron,
    onSecondary = Color.Black,
    secondaryContainer = UtopiaSurfaceElevatedDark,
    onSecondaryContainer = UtopiaTextWhite,
    tertiary = CivicGreen,
    onTertiary = Color.Black,
    background = UtopiaBlackBg,
    onBackground = UtopiaTextWhite,
    surface = UtopiaSurfaceDark,
    onSurface = UtopiaTextWhite,
    surfaceVariant = UtopiaSurfaceElevatedDark,
    onSurfaceVariant = UtopiaTextMuted,
    outline = UtopiaBorderDark,
    error = UtopiaRedLight,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = UtopiaRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9DF),
    onPrimaryContainer = Color(0xFF41000E),
    secondary = UtopiaOrange,
    onSecondary = Color.White,
    secondaryContainer = UtopiaSurfaceElevatedLight,
    onSecondaryContainer = UtopiaTextBlack,
    tertiary = CivicGreen,
    onTertiary = Color.Black,
    background = UtopiaLightBg,
    onBackground = UtopiaTextBlack,
    surface = UtopiaSurfaceLight,
    onSurface = UtopiaTextBlack,
    surfaceVariant = UtopiaSurfaceElevatedLight,
    onSurfaceVariant = UtopiaTextMutedLight,
    outline = UtopiaBorderLight,
    error = UtopiaRedDark,
    onError = Color.White
)

@Composable
fun UtopiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // Utopia defaults to vibrant dark aesthetic for maximum visual energy
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
