package me.muriloscolari.ciclo.ui.designsystem

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF0A0A12)
val DarkSurface = Color(0xFF141420)
val DarkSurfaceElevated = Color(0xFF1E1E2E)

val GlassFill = Color(0x14FFFFFF) // 8% white
val GlassFillActive = Color(0x24FFFFFF)
val GlassBorder = Color(0x1FFFFFFF) // ~12% white hairline
val GlassBorderActive = Color(0x40A78BFA) // Lavender hairline border

val PrimaryLavender = Color(0xFFA78BFA)
val SecondaryViolet = Color(0xFF8B5CF6)
val AccentIndigo = Color(0xFF6366F1)
val DeepIndigo = Color(0xFF3730A3)
val DeepViolet = Color(0xFF7C3AED)

val TextPrimary = Color(0xFFF2F2F7)
val TextSecondary = Color(0x99F2F2F7) // 60%
val TextTertiary = Color(0x66F2F2F7)  // 40%

val RecommendedGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFA78BFA),
        Color(0xFF6366F1)
    )
)

val RecommendedCardBorder = Brush.linearGradient(
    colors = listOf(
        Color(0x99A78BFA),
        Color(0x666366F1),
        Color(0x22A78BFA)
    )
)
