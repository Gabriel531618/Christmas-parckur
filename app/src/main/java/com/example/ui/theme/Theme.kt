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

private val CosmicDarkColorScheme = darkColorScheme(
    primary = CosmicCyan,
    onPrimary = CosmicInk,
    secondary = CosmicPurple,
    onSecondary = Color.White,
    tertiary = CosmicPink,
    onTertiary = Color.White,
    background = CosmicVoid,
    onBackground = Color.White,
    surface = CosmicDeep,
    onSurface = Color.White
)

private val CosmicLightColorScheme = lightColorScheme(
    primary = CosmicViolet,
    onPrimary = Color.White,
    secondary = CosmicCyan,
    onSecondary = CosmicInk,
    tertiary = CosmicPink,
    onTertiary = Color.White,
    background = CosmicSurface,
    onBackground = CosmicInk,
    surface = Color.White,
    onSurface = CosmicInk
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CosmicDarkColorScheme
        else -> CosmicLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
