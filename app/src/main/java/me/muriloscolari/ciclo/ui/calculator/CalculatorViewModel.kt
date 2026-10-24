package me.muriloscolari.ciclo.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.muriloscolari.ciclo.alarm.NativeAlarmScheduler
import me.muriloscolari.ciclo.alarm.NativeAlarmScheduler.AlarmResult
import me.muriloscolari.ciclo.data.history.AlarmHistoryEntity
import me.muriloscolari.ciclo.data.history.HistoryRepository
import me.muriloscolari.ciclo.data.settings.SettingsRepository
import me.muriloscolari.ciclo.data.settings.UserSettings
import me.muriloscolari.ciclo.domain.SleepCycleCalculator
import me.muriloscolari.ciclo.domain.SleepMode
import me.muriloscolari.ciclo.domain.SleepOption
import me.muriloscolari.ciclo.domain.WakeAtResult
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class CalculatorUiState(
    val selectedMode: SleepMode = SleepMode.NOW,
    val currentTime: LocalDateTime = LocalDateTime.now(),
    val chosenBedTime: LocalTime = LocalTime.of(23, 0),
    val chosenWakeTime: LocalTime = LocalTime.of(7, 0),
    val options: List<SleepOption> = emptyList(),
    val hasAvailableOptions: Boolean = true,
    val selectedOptionForConfirmation: SleepOption? = null,
    val duplicateWarningText: String? = null,
    val isSimpleAlarmForConfirmation: Boolean = false,
    val userSettings: UserSettings = UserSettings()
)

sealed interface CalculatorUiEvent {
    data class ShowSnackbar(val message: String) : CalculatorUiEvent
}

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val historyRepository: HistoryRepository,
    private val alarmScheduler: NativeAlarmScheduler,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CalculatorUiState(
            currentTime = LocalDateTime.now(clock)
        )
    )
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CalculatorUiEvent>()
    val events: SharedFlow<CalculatorUiEvent> = _events.asSharedFlow()

    private val tickerFlow = MutableStateFlow(LocalDateTime.now(clock))

    init {
        // Atualiza a cada minuto para o modo "Dormir agora" refletir a passagem do tempo
        viewModelScope.launch {
            while (isActive) {
                delay(15000L) // Checa a cada 15 segundos
                val now = LocalDateTime.now(clock)
                if (now.minute != tickerFlow.value.minute) {
                    tickerFlow.value = now
                }
            }
        }

        // Combina configurações e tempo para recalcular as opções
        viewModelScope.launch {
            combine(
                settingsRepository.settingsFlow,
                tickerFlow,
                _uiState
            ) { settings, now, state ->
                Triple(settings, now, state)
            }.collect { (settings, now, state) ->
                recalculateOptions(settings, now, state.selectedMode, state.chosenBedTime, state.chosenWakeTime)
            }
        }
    }

    private fun recalculateOptions(
        settings: UserSettings,
        now: LocalDateTime,
        mode: SleepMode,
        bedTimeInput: LocalTime,
        wakeTimeInput: LocalTime
    ) {
        val (options, hasAny) = when (mode) {
            SleepMode.NOW -> {
                val opts = SleepCycleCalculator.wakeTimesFor(now, settings.sleepSettings)
                Pair(opts, true)
            }
            SleepMode.BEDTIME -> {
                var bedDateTime = now.toLocalDate().atTime(bedTimeInput)
                if (bedDateTime.isBefore(now)) {
                    bedDateTime = bedDateTime.plusDays(1)
                }
                val opts = SleepCycleCalculator.wakeTimesFor(bedDateTime, settings.sleepSettings)
                Pair(opts, true)
            }
            SleepMode.WAKE_AT -> {
                var wakeDateTime = now.toLocalDate().atTime(wakeTimeInput)
                if (wakeDateTime.isBefore(now)) {
                    wakeDateTime = wakeDateTime.plusDays(1)
                }
                val result: WakeAtResult = SleepCycleCalculator.bedTimesFor(wakeDateTime, now, settings.sleepSettings)
                Pair(result.options, result.hasAnyAvailable)
            }
        }

        _uiState.update { current ->
            current.copy(
                currentTime = now,
                userSettings = settings,
                options = options,
                hasAvailableOptions = hasAny
            )
        }
    }

    fun onModeSelected(mode: SleepMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun onBedTimeChanged(time: LocalTime) {
        _uiState.update { it.copy(chosenBedTime = time) }
    }

    fun onWakeTimeChanged(time: LocalTime) {
        _uiState.update { it.copy(chosenWakeTime = time) }
    }

    fun onOptionClicked(option: SleepOption) {
        viewModelScope.launch {
            val duplicate = historyRepository.findRecentDuplicate(
                wakeHour = option.wakeTime.hour,
                wakeMinute = option.wakeTime.minute
            )

            val warning = duplicate?.let {
                val diffMinutes = ((System.currentTimeMillis() - it.createdAtMillis) / 60000L).coerceAtLeast(1)
                if (diffMinutes < 60) "${diffMinutes} min" else "${diffMinutes / 60} h"
            }

            _uiState.update {
                it.copy(
                    selectedOptionForConfirmation = option,
                    duplicateWarningText = warning,
                    isSimpleAlarmForConfirmation = false
                )
            }
        }
    }

    fun onSimpleAlarmClicked(wakeHour: Int, wakeMinute: Int) {
        viewModelScope.launch {
            val duplicate = historyRepository.findRecentDuplicate(wakeHour, wakeMinute)
            val warning = duplicate?.let {
                val diffMinutes = ((System.currentTimeMillis() - it.createdAtMillis) / 60000L).coerceAtLeast(1)
                if (diffMinutes < 60) "${diffMinutes} min" else "${diffMinutes / 60} h"
            }

            val fakeOption = SleepOption(
                cycles = 0,
                bedTime = LocalDateTime.now(clock),
                wakeTime = LocalDateTime.now(clock).toLocalDate().atTime(wakeHour, wakeMinute),
                sleepDuration = Duration.ZERO,
                isRecommended = false,
                isAvailable = true
            )

            _uiState.update {
                it.copy(
                    selectedOptionForConfirmation = fakeOption,
                    duplicateWarningText = warning,
                    isSimpleAlarmForConfirmation = true
                )
            }
        }
    }

    fun dismissConfirmation() {
        _uiState.update {
            it.copy(
                selectedOptionForConfirmation = null,
                duplicateWarningText = null,
                isSimpleAlarmForConfirmation = false
            )
        }
    }

    fun confirmAndSetAlarm(
        labelFormat: String,
        simpleLabel: String,
        successMessage: String,
        noClockMessage: String,
        failedFormat: String
    ) {
        val option = _uiState.value.selectedOptionForConfirmation ?: return
        val isSimple = _uiState.value.isSimpleAlarmForConfirmation
        val settings = _uiState.value.userSettings

        val wakeHour = option.wakeTime.hour
        val wakeMinute = option.wakeTime.minute
        val label = if (isSimple) simpleLabel else String.format(labelFormat, option.cycles)

        val result = alarmScheduler.setAlarm(
            hour = wakeHour,
            minute = wakeMinute,
            label = label,
            skipUi = settings.skipUi,
            vibrate = settings.vibrate
        )

        dismissConfirmation()

        viewModelScope.launch {
            when (result) {
                is AlarmResult.Sent -> {
                    // Salva no histórico do Room
                    historyRepository.recordAlarm(
                        AlarmHistoryEntity(
                            createdAtMillis = System.currentTimeMillis(),
                            mode = if (isSimple) "SIMPLE" else _uiState.value.selectedMode.name,
                            bedTimeMillis = option.bedTime.atZone(clock.zone).toInstant().toEpochMilli(),
                            wakeTimeMillis = option.wakeTime.atZone(clock.zone).toInstant().toEpochMilli(),
                            wakeHour = wakeHour,
                            wakeMinute = wakeMinute,
                            cycles = option.cycles,
                            cycleMinutes = settings.sleepSettings.cycleMinutes,
                            fallAsleepMinutes = settings.sleepSettings.fallAsleepMinutes,
                            wasRecommended = option.isRecommended,
                            clockPackage = alarmScheduler.resolvedClockPackage()
                        )
                    )
                    _events.emit(CalculatorUiEvent.ShowSnackbar(successMessage))
                }
                is AlarmResult.NoClockApp -> {
                    _events.emit(CalculatorUiEvent.ShowSnackbar(noClockMessage))
                }
                is AlarmResult.Failed -> {
                    _events.emit(
                        CalculatorUiEvent.ShowSnackbar(
                            String.format(failedFormat, result.error.localizedMessage ?: "")
                        )
                    )
                }
            }
        }
    }
}
