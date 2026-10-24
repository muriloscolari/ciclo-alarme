package me.muriloscolari.ciclo.ui.calculator

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AlarmOff
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.muriloscolari.ciclo.R
import me.muriloscolari.ciclo.data.alarm.ActiveAlarmEntity
import me.muriloscolari.ciclo.data.settings.ActiveAlarmState
import me.muriloscolari.ciclo.data.settings.AlarmEngine
import me.muriloscolari.ciclo.ui.designsystem.DawnWarmOrange
import me.muriloscolari.ciclo.ui.designsystem.GlassBorder
import me.muriloscolari.ciclo.ui.designsystem.GlassSurface
import me.muriloscolari.ciclo.ui.designsystem.PrimaryLavender
import me.muriloscolari.ciclo.ui.designsystem.TextPrimary
import me.muriloscolari.ciclo.ui.designsystem.TextSecondary
import me.muriloscolari.ciclo.ui.designsystem.TextTertiary
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun ActiveAlarmEntity.toState(): ActiveAlarmState = ActiveAlarmState(
    triggerTimeMillis = triggerTimeMillis,
    wakeHour = wakeHour,
    wakeMinute = wakeMinute,
    label = label,
    cycles = cycles,
    isSimple = isSimple,
    engine = runCatching { AlarmEngine.valueOf(engine) }.getOrDefault(AlarmEngine.INTERNAL),
    clockPackage = clockPackage,
    sunriseEnabled = sunriseEnabled,
    sunriseLeadMinutes = sunriseLeadMinutes
)

@Composable
fun ActiveAlarmCard(
    alarm: ActiveAlarmEntity,
    onDisarm: () -> Unit,
    onOpenSystemClock: () -> Unit,
    modifier: Modifier = Modifier
) {
    ActiveAlarmCard(
        activeAlarm = alarm.toState(),
        onDisarm = onDisarm,
        onOpenSystemClock = onOpenSystemClock,
        modifier = modifier
    )
}

@Composable
fun ActiveAlarmCard(
    activeAlarm: ActiveAlarmState,
    onDisarm: () -> Unit,
    onOpenSystemClock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isInternal = activeAlarm.engine == AlarmEngine.INTERNAL

    // Animação de pulso suave para a luz de status "Armado"
    val infiniteTransition = rememberInfiniteTransition(label = "pulseArmed")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnim"
    )

    // Cálculo do tempo restante
    val now = System.currentTimeMillis()
    val diffMillis = (activeAlarm.triggerTimeMillis - now).coerceAtLeast(0L)
    val diffMinutes = (diffMillis / (60 * 1000L))
    val diffHours = diffMinutes / 60
    val remainingMinutes = diffMinutes % 60

    val countdownText = when {
        diffHours > 0 -> stringResource(R.string.armed_alarm_countdown_hours_minutes, diffHours, remainingMinutes)
        diffMinutes > 0 -> stringResource(R.string.armed_alarm_countdown_only_minutes, diffMinutes)
        else -> stringResource(R.string.armed_alarm_countdown_soon)
    }

    val wakeFormatted = String.format(Locale.getDefault(), "%02d:%02d", activeAlarm.wakeHour, activeAlarm.wakeMinute)

    // Cálculo do horário do nascer do sol se ativo
    val sunriseTimeString = if (activeAlarm.sunriseEnabled && activeAlarm.sunriseLeadMinutes > 0) {
        val sunriseMillis = activeAlarm.triggerTimeMillis - (activeAlarm.sunriseLeadMinutes * 60 * 1000L)
        val sunriseLocalTime = Instant.ofEpochMilli(sunriseMillis).atZone(ZoneId.systemDefault()).toLocalTime()
        sunriseLocalTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    } else {
        null
    }

    val borderGradient = if (isInternal) {
        Brush.linearGradient(
            colors = listOf(
                PrimaryLavender.copy(alpha = 0.80f),
                Color(0xFF6366F1).copy(alpha = 0.40f),
                PrimaryLavender.copy(alpha = 0.20f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF60A5FA).copy(alpha = 0.80f),
                Color(0xFF3B82F6).copy(alpha = 0.40f),
                Color(0xFF60A5FA).copy(alpha = 0.20f)
            )
        )
    }

    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        backgroundColor = if (isInternal) Color(0xFF161226).copy(alpha = 0.90f) else Color(0xFF0F172A).copy(alpha = 0.90f),
        borderBrush = borderGradient,
        borderWidth = 1.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Linha Superior: Status "Alarme Armado" + Contagem Regressiva
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(if (isInternal) Color(0xFF34D399).copy(alpha = 0.35f) else Color(0xFF60A5FA).copy(alpha = 0.35f))
                        )
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isInternal) Color(0xFF34D399) else Color(0xFF60A5FA))
                        )
                    }

                    Text(
                        text = stringResource(R.string.armed_alarm_title).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 11.sp
                        )
                    )
                }

                // Tag de tempo restante
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = countdownText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PrimaryLavender,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Horário do Alarme em Destaque
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = wakeFormatted,
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Light,
                            fontSize = 48.sp,
                            letterSpacing = (-1).sp
                        )
                    )

                    val cycleInfo = if (activeAlarm.isSimple) {
                        stringResource(R.string.simple_alarm_title)
                    } else {
                        val cyclesText = pluralStringResource(R.plurals.cycles_count, activeAlarm.cycles, activeAlarm.cycles)
                        "$cyclesText · ${activeAlarm.label}"
                    }

                    Text(
                        text = cycleInfo,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Badge e Descrição do Mecanismo (Onde está armado)
            GlassSurface(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = if (isInternal) PrimaryLavender.copy(alpha = 0.12f) else Color(0xFF3B82F6).copy(alpha = 0.12f),
                borderColor = if (isInternal) PrimaryLavender.copy(alpha = 0.30f) else Color(0xFF60A5FA).copy(alpha = 0.30f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isInternal) Icons.Rounded.Bedtime else Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = if (isInternal) PrimaryLavender else Color(0xFF60A5FA),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isInternal) {
                                stringResource(R.string.armed_badge_internal)
                            } else {
                                stringResource(R.string.armed_badge_native)
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isInternal) PrimaryLavender else Color(0xFF93C5FD),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (isInternal) {
                            stringResource(R.string.armed_badge_internal_desc)
                        } else {
                            stringResource(R.string.armed_badge_native_desc)
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextTertiary,
                            fontSize = 12.sp
                        )
                    )

                    // Informação extra do nascer do sol se ativado
                    if (sunriseTimeString != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.WbSunny,
                                contentDescription = null,
                                tint = DawnWarmOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.armed_sunrise_info, sunriseTimeString),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DawnWarmOrange,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botões de Ação: Desarmar / Abrir Relógio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Botão Desarmar
                Button(
                    onClick = onDisarm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x28EF4444),
                        contentColor = Color(0xFFFCA5A5)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AlarmOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.action_disarm_alarm),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                // Se for Nativo, oferece também botão para abrir o relógio do sistema
                if (!isInternal) {
                    Button(
                        onClick = onOpenSystemClock,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.08f),
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.action_open_system_clock),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
