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

@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "auroraTransition")

    val floatAnim1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auroraFloat1"
    )

    val floatAnim2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auroraFloat2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Orbe Superior Direito (Índigo Profundo)
            val center1 = Offset(
                x = width * (0.85f - (floatAnim1 * 0.15f)),
                y = height * (0.15f + (floatAnim2 * 0.12f))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        DeepIndigo.copy(alpha = 0.40f),
                        DeepViolet.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = center1,
                    radius = width * 0.75f
                ),
                center = center1,
                radius = width * 0.75f
            )

            // Orbe Inferior Esquerdo (Violeta / Lavanda)
            val center2 = Offset(
                x = width * (0.15f + (floatAnim2 * 0.15f)),
                y = height * (0.65f - (floatAnim1 * 0.15f))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        DeepViolet.copy(alpha = 0.35f),
                        PrimaryLavender.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center2,
                    radius = width * 0.70f
                ),
                center = center2,
                radius = width * 0.70f
            )

            // Orbe Central Sutil
            val center3 = Offset(
                x = width * (0.5f + (floatAnim1 * 0.1f)),
                y = height * (0.4f + (floatAnim2 * 0.1f))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AccentIndigo.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = center3,
                    radius = width * 0.5f
                ),
                center = center3,
                radius = width * 0.5f
            )
        }

        content()
    }
}
