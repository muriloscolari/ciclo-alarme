package me.muriloscolari.ciclo.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
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

data class UserSettings(
    val sleepSettings: SleepSettings = SleepSettings(),
    val skipUi: Boolean = true,
    val vibrate: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val blurEffects: Boolean = true,
    val useDynamicColors: Boolean = false,
    val is24HourFormat: Boolean = true
)

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
                is24HourFormat = is24Hour
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

    suspend fun resetToDefaults() {
        context.dataStore.edit { it.clear() }
    }
}
