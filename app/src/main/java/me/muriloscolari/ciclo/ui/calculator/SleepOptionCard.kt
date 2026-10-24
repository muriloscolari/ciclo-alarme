package me.muriloscolari.ciclo.ui.calculator

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import me.muriloscolari.ciclo.R
import me.muriloscolari.ciclo.domain.SleepOption
import me.muriloscolari.ciclo.ui.designsystem.GlassBorder
import me.muriloscolari.ciclo.ui.designsystem.GlassFill
import me.muriloscolari.ciclo.ui.designsystem.GlassSurface
import me.muriloscolari.ciclo.ui.designsystem.PrimaryLavender
import me.muriloscolari.ciclo.ui.designsystem.RecommendedCardBorder
import me.muriloscolari.ciclo.ui.designsystem.RecommendedGradient
import me.muriloscolari.ciclo.ui.designsystem.TextPrimary
import me.muriloscolari.ciclo.ui.designsystem.TextSecondary
import me.muriloscolari.ciclo.ui.designsystem.TextTertiary
import java.time.format.DateTimeFormatter

@Composable
fun SleepOptionCard(
    option: SleepOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRecommended = option.isRecommended && option.isAvailable
    val isAvailable = option.isAvailable

    val cardAlpha by animateFloatAsState(
        targetValue = if (isAvailable) 1f else 0.40f,
        animationSpec = tween(durationMillis = 250),
        label = "cardAlpha"
    )

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val timeDisplay = option.wakeTime.format(timeFormatter)

    val hours = option.sleepDuration.toHours()
    val minutes = option.sleepDuration.toMinutes() % 60
    val durationText = if (minutes == 0L) {
        stringResource(R.string.duration_only_hours, hours)
    } else {
        stringResource(R.string.duration_hours_minutes, hours, minutes)
    }

    val cyclesCountText = pluralStringResource(
        R.plurals.cycles_count,
        option.cycles,
        option.cycles
    )

    val subtitle = stringResource(
        R.string.cycle_and_duration_format,
        cyclesCountText,
        durationText
    )

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha),
        shape = RoundedCornerShape(26.dp),
        backgroundColor = if (isRecommended) Color(0x1EA78BFA) else GlassFill,
        borderStroke = if (!isRecommended) BorderStroke(1.dp, GlassBorder) else null,
        borderBrush = if (isRecommended) RecommendedCardBorder else null,
        borderWidth = if (isRecommended) 1.5.dp else 1.dp,
        onClick = if (isAvailable) onClick else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            if (isRecommended) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(RecommendedGradient)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.recommended_badge),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF130E26),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = timeDisplay,
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = if (isRecommended) PrimaryLavender else TextPrimary,
                            fontWeight = FontWeight.Light,
                            fontSize = 54.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (isAvailable) subtitle else stringResource(R.string.past_time_unavailable),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isAvailable) TextSecondary else TextTertiary,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    }
}
