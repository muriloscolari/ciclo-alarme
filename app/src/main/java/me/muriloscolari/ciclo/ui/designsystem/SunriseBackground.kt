package me.muriloscolari.ciclo.ui.designsystem

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Paleta de amanhecer circadiano quente
val DawnNightSky = Color(0xFF090A10)
val DawnDeepAmber = Color(0xFF4A1E08)
val DawnWarmOrange = Color(0xFFFF7A18)
val DawnGoldenSun = Color(0xFFFFB347)
val DawnSoftGlow = Color(0xFFFFE8B6)

@Composable
fun SunriseBackground(
    sunriseProgress: Float, // 0.0f (noite) a 1.0f (alvorecer pleno)
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sunrisePulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnim"
    )

    val clampedProgress = sunriseProgress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DawnNightSky)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Orbe Noturno base (desaparece conforme o sol sobe)
            val nightAlpha = (1f - clampedProgress) * 0.4f
            if (nightAlpha > 0.01f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            DeepIndigo.copy(alpha = nightAlpha),
                            DeepViolet.copy(alpha = nightAlpha * 0.5f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.5f, height * 0.3f),
                        radius = width * 0.8f
                    ),
                    center = Offset(width * 0.5f, height * 0.3f),
                    radius = width * 0.8f
                )
            }

            // 2. Orbe de Alvorecer Primário (Luz Solar nascendo do centro-inferior)
            val sunAlpha = clampedProgress.coerceIn(0f, 1f)
            val sunRadius = (width * 0.8f + (width * 0.6f * clampedProgress)) * pulse
            val sunCenter = Offset(
                x = width * 0.5f,
                y = height * (0.85f - (clampedProgress * 0.25f)) // O sol sobe suavemente
            )

            // Luz suave difusa que preenche a tela
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        DawnWarmOrange.copy(alpha = sunAlpha * 0.45f),
                        DawnDeepAmber.copy(alpha = sunAlpha * 0.30f),
                        Color.Transparent
                    ),
                    center = sunCenter,
                    radius = sunRadius
                ),
                center = sunCenter,
                radius = sunRadius
            )

            // Núcleo Dourado Quente e Brilhante (estilo amanhecer orgânico Apple)
            val coreRadius = (width * 0.45f + (width * 0.35f * clampedProgress)) * pulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        DawnSoftGlow.copy(alpha = sunAlpha * 0.70f),
                        DawnGoldenSun.copy(alpha = sunAlpha * 0.50f),
                        Color.Transparent
                    ),
                    center = sunCenter,
                    radius = coreRadius
                ),
                center = sunCenter,
                radius = coreRadius
            )
        }

        content()
    }
}
