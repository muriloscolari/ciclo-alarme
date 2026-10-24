package me.muriloscolari.ciclo.ui.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import me.muriloscolari.ciclo.domain.SleepMode

@Composable
fun SegmentedPill(
    selectedMode: SleepMode,
    onModeSelected: (SleepMode) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    labels: Map<SleepMode, String>
) {
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(999.dp),
        hazeState = hazeState,
        backgroundColor = GlassFill
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val modes = listOf(SleepMode.NOW, SleepMode.BEDTIME, SleepMode.WAKE_AT)

            modes.forEach { mode ->
                val isSelected = (mode == selectedMode)

                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) PrimaryLavender else Color.Transparent,
                    animationSpec = tween(durationMillis = 250),
                    label = "pillBgColor"
                )

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF130E26) else TextSecondary,
                    animationSpec = tween(durationMillis = 250),
                    label = "pillTextColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .background(backgroundColor)
                        .clickable { onModeSelected(mode) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labels[mode] ?: "",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = textColor,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 13.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}
