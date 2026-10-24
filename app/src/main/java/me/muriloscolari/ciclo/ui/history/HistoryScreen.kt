package me.muriloscolari.ciclo.ui.history

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Launch
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.NightlightRound
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import me.muriloscolari.ciclo.R
import me.muriloscolari.ciclo.data.history.AlarmHistoryEntity
import me.muriloscolari.ciclo.ui.designsystem.DarkSurface
import me.muriloscolari.ciclo.ui.designsystem.GlassBorder
import me.muriloscolari.ciclo.ui.designsystem.GlassFill
import me.muriloscolari.ciclo.ui.designsystem.GlassSurface
import me.muriloscolari.ciclo.ui.designsystem.PrimaryLavender
import me.muriloscolari.ciclo.ui.designsystem.TextPrimary
import me.muriloscolari.ciclo.ui.designsystem.TextSecondary
import me.muriloscolari.ciclo.ui.designsystem.TextTertiary

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    hazeState: HazeState,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val groupedHistory by viewModel.groupedHistory.collectAsState()
    val showClearDialog by viewModel.showClearDialog.collectAsState()

    val labelFormat = stringResource(R.string.alarm_clock_label)
    val simpleLabel = stringResource(R.string.alarm_clock_label_simple)
    val successMsg = stringResource(R.string.feedback_alarm_sent)
    val noClockMsg = stringResource(R.string.feedback_no_clock_app)
    val failedFormat = stringResource(R.string.feedback_alarm_failed)
    val itemDeletedMsg = stringResource(R.string.history_item_deleted)
    val undoLabel = stringResource(R.string.action_undo)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HistoryUiEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        event.onAction?.invoke()
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Top Bar com Título e Ação de Limpar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.history_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-0.5).sp
                )
            )

            if (groupedHistory.isNotEmpty()) {
                IconButton(onClick = { viewModel.promptClearHistory() }) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = stringResource(R.string.action_clear_all),
                        tint = TextSecondary
                    )
                }
            }
        }

        // Botão de atalho para abrir o Relógio Nativo do sistema
        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)) {
            GlassSurface(
                shape = RoundedCornerShape(16.dp),
                hazeState = hazeState,
                backgroundColor = GlassFill,
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.openNativeClock() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Alarm,
                            contentDescription = null,
                            tint = PrimaryLavender,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.action_open_clock),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Launch,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (groupedHistory.isEmpty()) {
            // Estado Vazio
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0x1AA78BFA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NightlightRound,
                            contentDescription = null,
                            tint = PrimaryLavender,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = stringResource(R.string.history_empty_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.history_empty_subtitle),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextTertiary,
                            lineHeight = 20.sp
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedHistory.forEach { group ->
                    item {
                        Text(
                            text = group.title,
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = PrimaryLavender,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                        )
                    }

                    items(group.items, key = { it.id }) { item ->
                        HistoryItemCard(
                            item = item,
                            hazeState = hazeState,
                            onRecreate = {
                                viewModel.recreateAlarm(
                                    entry = item,
                                    labelFormat = labelFormat,
                                    simpleLabel = simpleLabel,
                                    successMsg = successMsg,
                                    noClockMsg = noClockMsg,
                                    failedFormat = failedFormat
                                )
                            },
                            onDelete = {
                                viewModel.deleteItem(item, itemDeletedMsg, undoLabel)
                            }
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmação para limpar histórico
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissClearDialog() },
            title = {
                Text(
                    text = stringResource(R.string.dialog_clear_history_title),
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.dialog_clear_history_message),
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmClearHistory() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text(text = stringResource(R.string.dialog_action_clear))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissClearDialog() }) {
                    Text(text = stringResource(R.string.action_cancel), color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun HistoryItemCard(
    item: AlarmHistoryEntity,
    hazeState: HazeState,
    onRecreate: () -> Unit,
    onDelete: () -> Unit
) {
    val timeFormatted = String.format("%02d:%02d", item.wakeHour, item.wakeMinute)
    val cyclesText = if (item.mode == "SIMPLE") {
        stringResource(R.string.simple_alarm_title)
    } else {
        pluralStringResource(R.plurals.cycles_count, item.cycles, item.cycles)
    }

    GlassSurface(
        shape = RoundedCornerShape(20.dp),
        hazeState = hazeState,
        backgroundColor = GlassFill,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = TextPrimary,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Light
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = cyclesText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRecreate) {
                    Icon(
                        imageVector = Icons.Rounded.Replay,
                        contentDescription = stringResource(R.string.action_recreate_alarm),
                        tint = PrimaryLavender
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = stringResource(R.string.action_delete),
                        tint = TextTertiary
                    )
                }
            }
        }
    }
}
