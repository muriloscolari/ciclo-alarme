package me.muriloscolari.ciclo.ui.armed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.muriloscolari.ciclo.alarm.CicloAlarmManager
import me.muriloscolari.ciclo.data.alarm.ActiveAlarmEntity
import me.muriloscolari.ciclo.data.alarm.ActiveAlarmRepository
import me.muriloscolari.ciclo.data.settings.SettingsRepository
import javax.inject.Inject

sealed interface ArmedAlarmsUiEvent {
    data class ShowSnackbar(val message: String) : ArmedAlarmsUiEvent
}

@HiltViewModel
class ArmedAlarmsViewModel @Inject constructor(
    private val activeAlarmRepository: ActiveAlarmRepository,
    private val settingsRepository: SettingsRepository,
    private val cicloAlarmManager: CicloAlarmManager
) : ViewModel() {

    val armedAlarms: StateFlow<List<ActiveAlarmEntity>> = activeAlarmRepository.activeAlarmsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _events = MutableSharedFlow<ArmedAlarmsUiEvent>()
    val events: SharedFlow<ArmedAlarmsUiEvent> = _events.asSharedFlow()

    init {
        // Limpa automaticamente alarmes passados (com mais de 1h de atraso)
        viewModelScope.launch {
            activeAlarmRepository.cleanupExpired(System.currentTimeMillis() - 3600000L)
        }
    }

    fun disarmAlarm(alarm: ActiveAlarmEntity, feedbackMessage: String) {
        viewModelScope.launch {
            if (alarm.engine == "INTERNAL") {
                cicloAlarmManager.cancelInternalAlarm()
            }
            activeAlarmRepository.removeActiveAlarm(alarm.id)
            settingsRepository.clearActiveAlarm()
            _events.emit(ArmedAlarmsUiEvent.ShowSnackbar(feedbackMessage))
        }
    }

    fun openSystemClock(): Boolean {
        return cicloAlarmManager.openNativeClock()
    }
}
