package me.muriloscolari.ciclo.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import me.muriloscolari.ciclo.domain.SleepSettings
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ciclo_settings")

data class ActiveAlarmState(
    val triggerTimeMillis: Long,
    val wakeHour: Int,
    val wakeMinute: Int,
    val label: String,
    val cycles: Int,
    val isSimple: Boolean,
    val engine: AlarmEngine,
    val clockPackage: String? = null,
    val sunriseEnabled: Boolean = false,
    val sunriseLeadMinutes: Int = 0
)

data class UserSettings(
    val sleepSettings: SleepSettings = SleepSettings(),
    val skipUi: Boolean = true,
    val vibrate: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val blurEffects: Boolean = true,
    val useDynamicColors: Boolean = false,
    val is24HourFormat: Boolean = true,
    val alarmEngine: AlarmEngine = AlarmEngine.INTERNAL,
    val hasChosenEngine: Boolean = false,
    val snoozeDurationMinutes: Int = 10,
    val gentleWakeEnabled: Boolean = true,
    val sunriseEnabled: Boolean = true,
    val sunriseLeadTimeMinutes: Int = 5,
    val customRingtoneUri: String? = null,
    val activeAlarm: ActiveAlarmState? = null
)

enum class AlarmEngine {
    INTERNAL,
    NATIVE
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val CYCLE_MINUTES = intPreferencesKey("cycle_minutes")
        val FALL_ASLEEP_MINUTES = intPreferencesKey("fall_asleep_minutes")
        val RECOMMENDED_CYCLES = intPreferencesKey("recommended_cycles")
        val MIN_CYCLES = intPreferencesKey("min_cycles")
        val MAX_CYCLES = intPreferencesKey("max_cycles")
        val MAX_SLEEP_MINUTES = intPreferencesKey("max_sleep_minutes")
        val SKIP_UI = booleanPreferencesKey("skip_ui")
        val VIBRATE = booleanPreferencesKey("vibrate")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val BLUR_EFFECTS = booleanPreferencesKey("blur_effects")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val IS_24_HOUR = booleanPreferencesKey("is_24_hour")
        val ALARM_ENGINE = stringPreferencesKey("alarm_engine")
        val HAS_CHOSEN_ENGINE = booleanPreferencesKey("has_chosen_engine")
        val SNOOZE_DURATION_MINUTES = intPreferencesKey("snooze_duration_minutes")
        val GENTLE_WAKE_ENABLED = booleanPreferencesKey("gentle_wake_enabled")
        val SUNRISE_ENABLED = booleanPreferencesKey("sunrise_enabled")
        val SUNRISE_LEAD_TIME_MINUTES = intPreferencesKey("sunrise_lead_time_minutes")
        val CUSTOM_RINGTONE_URI = stringPreferencesKey("custom_ringtone_uri")
        val ACTIVE_ALARM_TRIGGER = longPreferencesKey("active_alarm_trigger")
        val ACTIVE_ALARM_HOUR = intPreferencesKey("active_alarm_hour")
        val ACTIVE_ALARM_MINUTE = intPreferencesKey("active_alarm_minute")
        val ACTIVE_ALARM_LABEL = stringPreferencesKey("active_alarm_label")
        val ACTIVE_ALARM_CYCLES = intPreferencesKey("active_alarm_cycles")
        val ACTIVE_ALARM_IS_SIMPLE = booleanPreferencesKey("active_alarm_is_simple")
        val ACTIVE_ALARM_ENGINE = stringPreferencesKey("active_alarm_engine")
        val ACTIVE_ALARM_CLOCK_PKG = stringPreferencesKey("active_alarm_clock_pkg")
        val ACTIVE_ALARM_SUNRISE_ENABLED = booleanPreferencesKey("active_alarm_sunrise_enabled")
        val ACTIVE_ALARM_SUNRISE_LEAD = intPreferencesKey("active_alarm_sunrise_lead")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val cycleMinutes = prefs[Keys.CYCLE_MINUTES] ?: 90
            val fallAsleepMinutes = prefs[Keys.FALL_ASLEEP_MINUTES] ?: 15
            val recommendedCycles = prefs[Keys.RECOMMENDED_CYCLES] ?: 5
            val minCycles = prefs[Keys.MIN_CYCLES] ?: 3
            val maxCycles = prefs[Keys.MAX_CYCLES] ?: 6
            val maxSleepMinutes = prefs[Keys.MAX_SLEEP_MINUTES] ?: 600

            val skipUi = prefs[Keys.SKIP_UI] ?: true
            val vibrate = prefs[Keys.VIBRATE] ?: true

            val themeModeString = prefs[Keys.THEME_MODE] ?: ThemeMode.DARK.name
            val themeMode = runCatching { ThemeMode.valueOf(themeModeString) }.getOrDefault(ThemeMode.DARK)

            val blurEffects = prefs[Keys.BLUR_EFFECTS] ?: true
            val dynamicColors = prefs[Keys.DYNAMIC_COLORS] ?: false
            val is24Hour = prefs[Keys.IS_24_HOUR] ?: true

            val alarmEngineString = prefs[Keys.ALARM_ENGINE] ?: AlarmEngine.INTERNAL.name
            val alarmEngine = runCatching { AlarmEngine.valueOf(alarmEngineString) }.getOrDefault(AlarmEngine.INTERNAL)
            val hasChosenEngine = prefs[Keys.HAS_CHOSEN_ENGINE] ?: false
            val snoozeDuration = prefs[Keys.SNOOZE_DURATION_MINUTES] ?: 10
            val gentleWake = prefs[Keys.GENTLE_WAKE_ENABLED] ?: true
            val sunrise = prefs[Keys.SUNRISE_ENABLED] ?: true
            val sunriseLeadTime = prefs[Keys.SUNRISE_LEAD_TIME_MINUTES] ?: 5
            val customRingtone = prefs[Keys.CUSTOM_RINGTONE_URI]

            val activeTrigger = prefs[Keys.ACTIVE_ALARM_TRIGGER]
            val activeAlarm = if (activeTrigger != null && activeTrigger > 0L) {
                val activeEngineStr = prefs[Keys.ACTIVE_ALARM_ENGINE] ?: AlarmEngine.INTERNAL.name
                val activeEngine = runCatching { AlarmEngine.valueOf(activeEngineStr) }.getOrDefault(AlarmEngine.INTERNAL)
                ActiveAlarmState(
                    triggerTimeMillis = activeTrigger,
                    wakeHour = prefs[Keys.ACTIVE_ALARM_HOUR] ?: 0,
                    wakeMinute = prefs[Keys.ACTIVE_ALARM_MINUTE] ?: 0,
                    label = prefs[Keys.ACTIVE_ALARM_LABEL] ?: "",
                    cycles = prefs[Keys.ACTIVE_ALARM_CYCLES] ?: 5,
                    isSimple = prefs[Keys.ACTIVE_ALARM_IS_SIMPLE] ?: false,
                    engine = activeEngine,
                    clockPackage = prefs[Keys.ACTIVE_ALARM_CLOCK_PKG],
                    sunriseEnabled = prefs[Keys.ACTIVE_ALARM_SUNRISE_ENABLED] ?: false,
                    sunriseLeadMinutes = prefs[Keys.ACTIVE_ALARM_SUNRISE_LEAD] ?: 5
                )
            } else {
                null
            }

            UserSettings(
                sleepSettings = SleepSettings(
                    cycleMinutes = cycleMinutes.coerceIn(90, 120),
                    fallAsleepMinutes = fallAsleepMinutes.coerceIn(0, 60),
                    minCycles = minCycles.coerceIn(1, 4),
                    maxCycles = maxCycles.coerceIn(5, 8),
                    recommendedCycles = recommendedCycles.coerceIn(minCycles, maxCycles),
                    maxSleepMinutes = maxSleepMinutes
                ),
                skipUi = skipUi,
                vibrate = vibrate,
                themeMode = themeMode,
                blurEffects = blurEffects,
                useDynamicColors = dynamicColors,
                is24HourFormat = is24Hour,
                alarmEngine = alarmEngine,
                hasChosenEngine = hasChosenEngine,
                snoozeDurationMinutes = snoozeDuration.coerceIn(5, 30),
                gentleWakeEnabled = gentleWake,
                sunriseEnabled = sunrise,
                sunriseLeadTimeMinutes = sunriseLeadTime.coerceIn(1, 30),
                customRingtoneUri = customRingtone,
                activeAlarm = activeAlarm
            )
        }

    suspend fun updateCycleMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.CYCLE_MINUTES] = minutes.coerceIn(90, 120) }
    }

    suspend fun updateFallAsleepMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.FALL_ASLEEP_MINUTES] = minutes.coerceIn(0, 60) }
    }

    suspend fun updateRecommendedCycles(cycles: Int) {
        context.dataStore.edit { it[Keys.RECOMMENDED_CYCLES] = cycles.coerceIn(1, 8) }
    }

    suspend fun updateSkipUi(skip: Boolean) {
        context.dataStore.edit { it[Keys.SKIP_UI] = skip }
    }

    suspend fun updateVibrate(vibrate: Boolean) {
        context.dataStore.edit { it[Keys.VIBRATE] = vibrate }
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun updateBlurEffects(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BLUR_EFFECTS] = enabled }
    }

    suspend fun updateDynamicColors(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLORS] = enabled }
    }

    suspend fun update24HourFormat(is24Hour: Boolean) {
        context.dataStore.edit { it[Keys.IS_24_HOUR] = is24Hour }
    }

    suspend fun updateAlarmEngine(engine: AlarmEngine) {
        context.dataStore.edit {
            it[Keys.ALARM_ENGINE] = engine.name
            it[Keys.HAS_CHOSEN_ENGINE] = true
        }
    }

    suspend fun setHasChosenEngine(hasChosen: Boolean) {
        context.dataStore.edit { it[Keys.HAS_CHOSEN_ENGINE] = hasChosen }
    }

    suspend fun updateSnoozeDuration(minutes: Int) {
        context.dataStore.edit { it[Keys.SNOOZE_DURATION_MINUTES] = minutes.coerceIn(5, 30) }
    }

    suspend fun updateGentleWake(enabled: Boolean) {
        context.dataStore.edit { it[Keys.GENTLE_WAKE_ENABLED] = enabled }
    }

    suspend fun updateSunrise(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SUNRISE_ENABLED] = enabled }
    }

    suspend fun updateSunriseLeadTime(minutes: Int) {
        context.dataStore.edit { it[Keys.SUNRISE_LEAD_TIME_MINUTES] = minutes.coerceIn(1, 30) }
    }

    suspend fun updateCustomRingtone(uri: String?) {
        context.dataStore.edit {
            if (uri != null) {
                it[Keys.CUSTOM_RINGTONE_URI] = uri
            } else {
                it.remove(Keys.CUSTOM_RINGTONE_URI)
            }
        }
    }

    suspend fun setActiveAlarm(alarm: ActiveAlarmState) {
        context.dataStore.edit {
            it[Keys.ACTIVE_ALARM_TRIGGER] = alarm.triggerTimeMillis
            it[Keys.ACTIVE_ALARM_HOUR] = alarm.wakeHour
            it[Keys.ACTIVE_ALARM_MINUTE] = alarm.wakeMinute
            it[Keys.ACTIVE_ALARM_LABEL] = alarm.label
            it[Keys.ACTIVE_ALARM_CYCLES] = alarm.cycles
            it[Keys.ACTIVE_ALARM_IS_SIMPLE] = alarm.isSimple
            it[Keys.ACTIVE_ALARM_ENGINE] = alarm.engine.name
            if (alarm.clockPackage != null) {
                it[Keys.ACTIVE_ALARM_CLOCK_PKG] = alarm.clockPackage
            } else {
                it.remove(Keys.ACTIVE_ALARM_CLOCK_PKG)
            }
            it[Keys.ACTIVE_ALARM_SUNRISE_ENABLED] = alarm.sunriseEnabled
            it[Keys.ACTIVE_ALARM_SUNRISE_LEAD] = alarm.sunriseLeadMinutes
        }
    }

    suspend fun clearActiveAlarm() {
        context.dataStore.edit {
            it.remove(Keys.ACTIVE_ALARM_TRIGGER)
            it.remove(Keys.ACTIVE_ALARM_HOUR)
            it.remove(Keys.ACTIVE_ALARM_MINUTE)
            it.remove(Keys.ACTIVE_ALARM_LABEL)
            it.remove(Keys.ACTIVE_ALARM_CYCLES)
            it.remove(Keys.ACTIVE_ALARM_IS_SIMPLE)
            it.remove(Keys.ACTIVE_ALARM_ENGINE)
            it.remove(Keys.ACTIVE_ALARM_CLOCK_PKG)
            it.remove(Keys.ACTIVE_ALARM_SUNRISE_ENABLED)
            it.remove(Keys.ACTIVE_ALARM_SUNRISE_LEAD)
        }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit { it.clear() }
    }
}
