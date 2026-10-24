package me.muriloscolari.ciclo.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import me.muriloscolari.ciclo.R
import me.muriloscolari.ciclo.ui.designsystem.DarkSurface
import me.muriloscolari.ciclo.ui.designsystem.DarkSurfaceElevated
import me.muriloscolari.ciclo.ui.designsystem.PrimaryLavender
import me.muriloscolari.ciclo.ui.designsystem.TextPrimary
import me.muriloscolari.ciclo.ui.designsystem.TextSecondary
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CicloTimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
    is24Hour: Boolean = true
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = is24Hour
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(DarkSurface)
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.time_picker_title),
                    style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = DarkSurfaceElevated,
                        selectorColor = PrimaryLavender,
                        timeSelectorSelectedContainerColor = PrimaryLavender,
                        timeSelectorUnselectedContainerColor = DarkSurfaceElevated,
                        timeSelectorSelectedContentColor = Color(0xFF130E26),
                        timeSelectorUnselectedContentColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

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

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryLavender,
                            contentColor = Color(0xFF130E26)
                        )
                    ) {
                        Text(text = stringResource(R.string.time_picker_confirm))
                    }
                }
            }
        }
    }
}
