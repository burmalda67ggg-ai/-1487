package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SapphireAccent,
    onPrimary = TextLuxuryPrimary,
    primaryContainer = TitaniumElevated,
    onPrimaryContainer = SapphireLight,
    secondary = EmeraldLuxury,
    onSecondary = OnyxDeep,
    secondaryContainer = EmeraldLuxurySubtle,
    onSecondaryContainer = EmeraldLuxury,
    tertiary = ChampagneGold,
    background = OnyxDeep,
    onBackground = TextLuxuryPrimary,
    surface = TitaniumSurface,
    onSurface = TextLuxuryPrimary,
    surfaceVariant = TitaniumCard,
    onSurfaceVariant = TextLuxurySecondary,
    outline = TitaniumBorder,
    outlineVariant = TitaniumDivider,
    error = CoralAlert
)

private val LightColorScheme = lightColorScheme(
    primary = SapphireAccent,
    onPrimary = SteelSurface,
    primaryContainer = SteelElevated,
    onPrimaryContainer = SapphireAccent,
    secondary = EmeraldLuxury,
    onSecondary = SteelSurface,
    background = ArchitecturalWhite,
    onBackground = SteelTextPrimary,
    surface = SteelSurface,
    onSurface = SteelTextPrimary,
    surfaceVariant = SteelElevated,
    onSurfaceVariant = SteelTextSecondary,
    outline = SteelBorder,
    outlineVariant = SteelBorder,
    error = CoralAlert
)

@Composable
fun OmniVpnTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = colorScheme.background.toArgb()
                it.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
