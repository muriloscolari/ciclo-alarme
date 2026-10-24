package me.muriloscolari.ciclo.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.muriloscolari.ciclo.alarm.NativeAlarmScheduler
import me.muriloscolari.ciclo.alarm.NativeAlarmScheduler.AlarmResult
import me.muriloscolari.ciclo.data.history.AlarmHistoryEntity
import me.muriloscolari.ciclo.data.history.HistoryRepository
import me.muriloscolari.ciclo.data.settings.SettingsRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class HistoryGroup(
    val title: String,
    val items: List<AlarmHistoryEntity>
)

sealed interface HistoryUiEvent {
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val onAction: (() -> Unit)? = null
    ) : HistoryUiEvent
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val alarmScheduler: NativeAlarmScheduler
) : ViewModel() {

    private val _events = MutableSharedFlow<HistoryUiEvent>()
    val events: SharedFlow<HistoryUiEvent> = _events.asSharedFlow()

    private val _showClearDialog = MutableStateFlow(false)
    val showClearDialog: StateFlow<Boolean> = _showClearDialog.asStateFlow()

    private var recentlyDeletedItem: AlarmHistoryEntity? = null

    val groupedHistory: StateFlow<List<HistoryGroup>> = historyRepository.historyFlow
        .map { entries ->
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            val dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("pt", "BR"))

            entries.groupBy { entry ->
                val date = Instant.ofEpochMilli(entry.createdAtMillis).atZone(zone).toLocalDate()
                when (date) {
                    today -> "Hoje"
                    yesterday -> "Ontem"
                    else -> date.format(dateFormatter)
                }
            }.map { (title, items) ->
                HistoryGroup(title = title, items = items)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun openNativeClock(): Boolean {
        return alarmScheduler.openClockAlarms()
    }

    fun promptClearHistory() {
        _showClearDialog.value = true
    }

    fun dismissClearDialog() {
        _showClearDialog.value = false
    }

    fun confirmClearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
            _showClearDialog.value = false
        }
    }

    fun deleteItem(item: AlarmHistoryEntity, deletedMsg: String, undoLabel: String) {
        recentlyDeletedItem = item
        viewModelScope.launch {
            historyRepository.deleteEntry(item.id)
            _events.emit(
                HistoryUiEvent.ShowSnackbar(
                    message = deletedMsg,
                    actionLabel = undoLabel,
                    onAction = {
                        recentlyDeletedItem?.let { restored ->
                            viewModelScope.launch {
                                historyRepository.recordAlarm(restored)
                            }
                        }
                    }
                )
            )
        }
    }

    fun recreateAlarm(
        entry: AlarmHistoryEntity,
        labelFormat: String,
        simpleLabel: String,
        successMsg: String,
        noClockMsg: String,
        failedFormat: String
    ) {
        viewModelScope.launch {
            val label = if (entry.mode == "SIMPLE") {
                simpleLabel
            } else {
                String.format(labelFormat, entry.cycles)
            }

            val result = alarmScheduler.setAlarm(
                hour = entry.wakeHour,
                minute = entry.wakeMinute,
                label = label,
                skipUi = true,
                vibrate = true
            )

            when (result) {
                is AlarmResult.Sent -> {
                    // Atualiza o registro adicionando novo evento no histórico
                    historyRepository.recordAlarm(
                        entry.copy(
                            id = 0,
                            createdAtMillis = System.currentTimeMillis()
                        )
                    )
                    _events.emit(HistoryUiEvent.ShowSnackbar(successMsg))
                }
                is AlarmResult.NoClockApp -> {
                    _events.emit(HistoryUiEvent.ShowSnackbar(noClockMsg))
                }
                is AlarmResult.Failed -> {
                    _events.emit(
                        HistoryUiEvent.ShowSnackbar(
                            String.format(failedFormat, result.error.localizedMessage ?: "")
                        )
                    )
                }
            }
        }
    }
}
