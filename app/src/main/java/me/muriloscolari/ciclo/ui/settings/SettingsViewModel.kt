package me.muriloscolari.ciclo.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.muriloscolari.ciclo.alarm.CicloAlarmManager
import me.muriloscolari.ciclo.alarm.NativeAlarmScheduler
import me.muriloscolari.ciclo.data.settings.AlarmEngine
import me.muriloscolari.ciclo.data.settings.SettingsRepository
import me.muriloscolari.ciclo.data.settings.ThemeMode
import me.muriloscolari.ciclo.data.settings.UserSettings
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val alarmScheduler: NativeAlarmScheduler,
    private val cicloAlarmManager: CicloAlarmManager
) : ViewModel() {

    val userSettings: StateFlow<UserSettings> = settingsRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    val detectedClockPackage: String? = alarmScheduler.resolvedClockPackage()

    fun updateAlarmEngine(engine: AlarmEngine) {
        viewModelScope.launch {
            settingsRepository.updateAlarmEngine(engine)
        }
    }

    fun updateGentleWake(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateGentleWake(enabled)
        }
    }

    fun updateSunrise(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSunrise(enabled)
        }
    }

    fun updateSunriseLeadTime(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.updateSunriseLeadTime(minutes)
        }
    }

    fun updateSnoozeDuration(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.updateSnoozeDuration(minutes)
        }
    }

    fun updateCustomRingtone(uri: String?) {
        viewModelScope.launch {
            settingsRepository.updateCustomRingtone(uri)
        }
    }

    fun scheduleTestAlarm(seconds: Int = 10) {
        cicloAlarmManager.scheduleTestAlarm(seconds)
    }

    fun updateCycleMinutes(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.updateCycleMinutes(minutes)
        }
    }

    fun updateFallAsleepMinutes(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.updateFallAsleepMinutes(minutes)
        }
    }

    fun updateRecommendedCycles(cycles: Int) {
        viewModelScope.launch {
            settingsRepository.updateRecommendedCycles(cycles)
        }
    }

    fun updateSkipUi(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSkipUi(enabled)
        }
    }

    fun updateVibrate(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateVibrate(enabled)
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(mode)
        }
    }

    fun updateBlurEffects(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateBlurEffects(enabled)
        }
    }

    fun updateDynamicColors(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateDynamicColors(enabled)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            settingsRepository.resetToDefaults()
        }
    }
}
