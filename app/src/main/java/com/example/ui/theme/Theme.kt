package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Blue600,
    secondary = Amber500,
    tertiary = Blue100,
    background = Gray950,
    surface = Gray900,
    onPrimary = Gray50,
    onSecondary = Gray950,
    onBackground = Gray50,
    onSurface = Gray50,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray200
)

private val LightColorScheme = lightColorScheme(
    primary = Blue800,
    secondary = Amber700,
    tertiary = Blue900,
    background = Gray50,
    surface = com.example.ui.theme.Gray200,
    onPrimary = Gray50,
    onSecondary = Gray50,
    onBackground = Gray900,
    onSurface = Gray900,
    surfaceVariant = Gray50,
    onSurfaceVariant = Gray800
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to prioritize our majestic school colors!
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
