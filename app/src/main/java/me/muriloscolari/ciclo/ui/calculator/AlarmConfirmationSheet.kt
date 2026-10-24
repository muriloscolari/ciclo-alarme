package me.muriloscolari.ciclo.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.muriloscolari.ciclo.R
import me.muriloscolari.ciclo.domain.SleepMode
import me.muriloscolari.ciclo.domain.SleepOption
import me.muriloscolari.ciclo.ui.designsystem.DarkSurface
import me.muriloscolari.ciclo.ui.designsystem.PrimaryLavender
import me.muriloscolari.ciclo.ui.designsystem.TextPrimary
import me.muriloscolari.ciclo.ui.designsystem.TextSecondary
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmConfirmationSheet(
    option: SleepOption,
    mode: SleepMode,
    isSimpleAlarm: Boolean,
    duplicateWarning: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val wakeDisplay = option.wakeTime.format(timeFormatter)
    val bedDisplay = option.bedTime.format(timeFormatter)

    val hours = option.sleepDuration.toHours()
    val minutes = option.sleepDuration.toMinutes() % 60
    val durationText = if (minutes == 0L) {
        stringResource(R.string.duration_only_hours, hours)
    } else {
        stringResource(R.string.duration_hours_minutes, hours, minutes)
    }

    val summaryText = when {
        isSimpleAlarm -> stringResource(R.string.simple_alarm_desc, wakeDisplay)
        mode == SleepMode.NOW -> stringResource(R.string.confirm_alarm_summary_now, option.cycles, durationText)
        mode == SleepMode.BEDTIME -> stringResource(R.string.confirm_alarm_summary_bedtime, bedDisplay, wakeDisplay, option.cycles)
        mode == SleepMode.WAKE_AT -> stringResource(R.string.confirm_alarm_summary_wakeat, wakeDisplay, bedDisplay, option.cycles)
        else -> ""
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0x33FFFFFF))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.confirm_alarm_title, wakeDisplay),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = summaryText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    lineHeight = 22.sp
                )
            )

            if (duplicateWarning != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x22F59E0B))
                        .padding(14.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.confirm_alarm_duplicate_warning,
                            wakeDisplay,
                            duplicateWarning
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFFBBF24),
                            fontSize = 13.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryLavender,
                        contentColor = Color(0xFF130E26)
                    ),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Alarm,
                        contentDescription = null,
                        tint = Color(0xFF130E26)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.confirm_alarm_action),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
