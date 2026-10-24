package me.muriloscolari.ciclo.ui.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.NightlightRound
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import me.muriloscolari.ciclo.R
import me.muriloscolari.ciclo.domain.SleepMode
import me.muriloscolari.ciclo.ui.designsystem.GlassBorder
import me.muriloscolari.ciclo.ui.designsystem.GlassFill
import me.muriloscolari.ciclo.ui.designsystem.GlassSurface
import me.muriloscolari.ciclo.ui.designsystem.PrimaryLavender
import me.muriloscolari.ciclo.ui.designsystem.SegmentedPill
import me.muriloscolari.ciclo.ui.designsystem.TextPrimary
import me.muriloscolari.ciclo.ui.designsystem.TextSecondary
import me.muriloscolari.ciclo.ui.designsystem.TextTertiary
import java.time.format.DateTimeFormatter

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }

    val successMsg = stringResource(R.string.feedback_alarm_sent)
    val noClockMsg = stringResource(R.string.feedback_no_clock_app)
    val failedFormat = stringResource(R.string.feedback_alarm_failed)
    val labelFormat = stringResource(R.string.alarm_clock_label)
    val simpleLabel = stringResource(R.string.alarm_clock_label_simple)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CalculatorUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val currentTimeString = uiState.currentTime.format(timeFormatter)

    val modeLabels = mapOf(
        SleepMode.NOW to stringResource(R.string.mode_sleep_now),
        SleepMode.BEDTIME to stringResource(R.string.mode_sleep_at),
        SleepMode.WAKE_AT to stringResource(R.string.mode_wake_at)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Cabeçalho da aplicação
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Rounded.NightlightRound,
                        contentDescription = null,
                        tint = PrimaryLavender,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.current_time_format, currentTimeString),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                )
            }
        }

        // Seletor de Modo
        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
            SegmentedPill(
                selectedMode = uiState.selectedMode,
                onModeSelected = { viewModel.onModeSelected(it) },
                labels = modeLabels
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Seletor de Horário para BEDTIME ou WAKE_AT
        AnimatedVisibility(
            visible = uiState.selectedMode != SleepMode.NOW,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val selectedTime = if (uiState.selectedMode == SleepMode.BEDTIME) {
                uiState.chosenBedTime
            } else {
                uiState.chosenWakeTime
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp)
            ) {
                GlassSurface(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = GlassFill,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showTimePicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (uiState.selectedMode == SleepMode.BEDTIME) {
                                stringResource(R.string.go_to_bed_at_label)
                            } else {
                                stringResource(R.string.wake_up_at_label)
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AccessTime,
                                contentDescription = null,
                                tint = PrimaryLavender,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedTime.format(timeFormatter),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = PrimaryLavender,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }

        // Lista de Cards com as opções
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = if (uiState.selectedMode == SleepMode.WAKE_AT) {
                        stringResource(R.string.go_to_bed_at_label)
                    } else {
                        stringResource(R.string.wake_up_at_label)
                    },
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = TextTertiary,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }

            items(uiState.options) { option ->
                SleepOptionCard(
                    option = option,
                    onClick = { viewModel.onOptionClicked(option) }
                )
            }

            // Caso especial: Modo "Acordar às" sem tempo ou opção de alarme simples
            if (uiState.selectedMode == SleepMode.WAKE_AT) {
                item {
                    val wakeStr = uiState.chosenWakeTime.format(timeFormatter)
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            viewModel.onSimpleAlarmClicked(
                                uiState.chosenWakeTime.hour,
                                uiState.chosenWakeTime.minute
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlassFill,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                    ) {
                        Text(
                            text = stringResource(R.string.action_create_simple_alarm, wakeStr),
                            style = MaterialTheme.typography.bodyMedium.copy(color = PrimaryLavender)
                        )
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(
                            R.string.fall_asleep_notice,
                            uiState.userSettings.sleepSettings.fallAsleepMinutes
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextTertiary,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }

    // BottomSheet de Confirmação
    uiState.selectedOptionForConfirmation?.let { option ->
        AlarmConfirmationSheet(
            option = option,
            mode = uiState.selectedMode,
            isSimpleAlarm = uiState.isSimpleAlarmForConfirmation,
            duplicateWarning = uiState.duplicateWarningText,
            onConfirm = {
                viewModel.confirmAndSetAlarm(
                    labelFormat = labelFormat,
                    simpleLabel = simpleLabel,
                    successMessage = successMsg,
                    noClockMessage = noClockMsg,
                    failedFormat = failedFormat
                )
            },
            onDismiss = { viewModel.dismissConfirmation() }
        )
    }

    // Diálogo Seletor de Horário
    if (showTimePicker) {
        val initialTime = if (uiState.selectedMode == SleepMode.BEDTIME) {
            uiState.chosenBedTime
        } else {
            uiState.chosenWakeTime
        }

        CicloTimePickerDialog(
            initialTime = initialTime,
            onTimeSelected = { newTime ->
                if (uiState.selectedMode == SleepMode.BEDTIME) {
                    viewModel.onBedTimeChanged(newTime)
                } else {
                    viewModel.onWakeTimeChanged(newTime)
                }
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
            is24Hour = uiState.userSettings.is24HourFormat
        )
    }
}
