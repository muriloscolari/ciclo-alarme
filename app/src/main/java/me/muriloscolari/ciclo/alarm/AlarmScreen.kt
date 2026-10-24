package me.muriloscolari.ciclo.alarm

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.muriloscolari.ciclo.R
import me.muriloscolari.ciclo.ui.designsystem.GlassSurface
import me.muriloscolari.ciclo.ui.designsystem.PrimaryLavender
import me.muriloscolari.ciclo.ui.designsystem.SunriseBackground
import kotlin.math.roundToInt

@Composable
fun AlarmScreen(
    currentTimeText: String,
    label: String,
    cycles: Int,
    isSimple: Boolean,
    isSunriseOnly: Boolean,
    sunriseProgress: Float,
    snoozeDurationMinutes: Int,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val dragOffsetY = remember { Animatable(0f) }
    val dismissThresholdPx = -300f

    // Animação sutil para a seta de deslize
    val infiniteTransition = rememberInfiniteTransition(label = "arrowBounce")
    val arrowBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowAnim"
    )

    SunriseBackground(
        sunriseProgress = sunriseProgress,
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp)
        ) {
            // Topo e Centro: Horário, ciclos e status
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Badge de ciclo ou amanhecer
                GlassSurface(
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = Color.White.copy(alpha = 0.08f),
                    borderColor = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bedtime,
                            contentDescription = null,
                            tint = PrimaryLavender,
                            modifier = Modifier.size(16.dp)
                        )
                        val badgeText = if (isSimple) {
                            stringResource(R.string.simple_alarm_title)
                        } else {
                            pluralStringResource(R.plurals.cycles_count, cycles, cycles)
                        }
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                // Relógio em destaque (tipografia grande e limpa)
                Text(
                    text = currentTimeText,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 84.sp,
                        fontWeight = FontWeight.ExtraLight,
                        letterSpacing = (-2).sp,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mensagem de status
                val statusText = if (isSunriseOnly) {
                    stringResource(R.string.alarm_sunrise_title)
                } else {
                    stringResource(R.string.alarm_ringing_title)
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Normal
                    )
                )

                if (label.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            // Base: Botão de Soneca e Controle de Deslize para Desligar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Botão de Soneca (Apenas se já estiver tocando)
                if (!isSunriseOnly) {
                    GlassSurface(
                        onClick = onSnooze,
                        shape = RoundedCornerShape(24.dp),
                        backgroundColor = Color.White.copy(alpha = 0.12f),
                        borderColor = Color.White.copy(alpha = 0.20f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Snooze,
                                contentDescription = null,
                                tint = PrimaryLavender,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.alarm_snooze_format, snoozeDurationMinutes),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                // Gesto: Deslize para cima para desligar
                Box(
                    modifier = Modifier
                        .offset { IntOffset(x = 0, y = dragOffsetY.value.roundToInt()) }
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    coroutineScope.launch {
                                        if (dragOffsetY.value <= dismissThresholdPx) {
                                            onDismiss()
                                        } else {
                                            dragOffsetY.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            )
                                        }
                                    }
                                },
                                onVerticalDrag = { _, dragAmount ->
                                    coroutineScope.launch {
                                        val newOffset = (dragOffsetY.value + dragAmount).coerceIn(-450f, 0f)
                                        dragOffsetY.snapTo(newOffset)
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Alça / Ícone que sobe com bounce
                        GlassSurface(
                            shape = CircleShape,
                            backgroundColor = Color.White.copy(alpha = 0.20f),
                            borderColor = Color.White.copy(alpha = 0.35f),
                            modifier = Modifier
                                .size(56.dp)
                                .offset(y = arrowBounce.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowUp,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // Label
                        Text(
                            text = stringResource(R.string.alarm_swipe_to_dismiss),
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Normal
                            ),
                            modifier = Modifier.alpha(
                                (1f - (-dragOffsetY.value / 300f)).coerceIn(0.2f, 1f)
                            )
                        )
                    }
                }
            }
        }
    }
}
