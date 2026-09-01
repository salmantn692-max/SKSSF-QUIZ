package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = BentoPrimary,
    onPrimary = BentoOnPrimary,
    primaryContainer = BentoPrimaryContainer,
    onPrimaryContainer = BentoOnPrimaryContainer,
    secondary = BentoSecondary,
    onSecondary = BentoOnSecondary,
    secondaryContainer = BentoSecondaryContainer,
    onSecondaryContainer = BentoOnSecondaryContainer,
    tertiary = BentoTertiary,
    onTertiary = BentoOnTertiary,
    tertiaryContainer = BentoTertiaryContainer,
    onTertiaryContainer = BentoOnTertiaryContainer,
    background = BentoBackground,
    onBackground = BentoOnBackground,
    surface = BentoSurface,
    onSurface = BentoOnSurface,
    surfaceVariant = BentoSurfaceVariant,
    onSurfaceVariant = BentoOnSurfaceVariant,
    outline = BentoOutline,
    outlineVariant = BentoOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = BentoDarkPrimary,
    onPrimary = BentoDarkOnPrimary,
    primaryContainer = BentoDarkPrimaryContainer,
    onPrimaryContainer = BentoDarkOnPrimaryContainer,
    secondary = BentoDarkPrimary,
    onSecondary = BentoDarkOnPrimary,
    secondaryContainer = BentoDarkPrimaryContainer,
    onSecondaryContainer = BentoDarkOnPrimaryContainer,
    tertiary = BentoTertiaryContainer,
    onTertiary = BentoOnTertiaryContainer,
    background = BentoDarkBackground,
    onBackground = BentoDarkOnBackground,
    surface = BentoDarkSurface,
    onSurface = BentoDarkOnSurface,
    surfaceVariant = BentoDarkSurfaceVariant,
    onSurfaceVariant = BentoDarkOnSurfaceVariant,
    outline = BentoDarkOutline,
    outlineVariant = BentoDarkOutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
