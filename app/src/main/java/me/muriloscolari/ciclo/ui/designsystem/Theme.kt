package me.muriloscolari.ciclo.ui.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import me.muriloscolari.ciclo.data.settings.ThemeMode

val LocalBlurEnabled = staticCompositionLocalOf { true }

private val CicloDarkColorScheme = darkColorScheme(
    primary = PrimaryLavender,
    onPrimary = Color(0xFF1E1035),
    primaryContainer = Color(0xFF38265E),
    onPrimaryContainer = Color(0xFFEADBFF),
    secondary = SecondaryViolet,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder
)

private val CicloLightColorScheme = lightColorScheme(
    primary = SecondaryViolet,
    onPrimary = Color.White,
    background = Color(0xFFF7F7FA),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    outline = Color(0xFFE2E2EA)
)

@Composable
fun CicloTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    dynamicColor: Boolean = false,
    blurEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CicloDarkColorScheme
        else -> CicloLightColorScheme
    }

    CompositionLocalProvider(
        LocalBlurEnabled provides blurEnabled
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CicloTypography,
            content = content
        )
    }
}
