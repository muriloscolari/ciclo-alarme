package me.muriloscolari.ciclo.alarm

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.muriloscolari.ciclo.data.settings.AlarmEngine
import me.muriloscolari.ciclo.data.settings.UserSettings
import javax.inject.Inject
import javax.inject.Singleton

sealed interface AlarmScheduleResult {
    data object Scheduled : AlarmScheduleResult
    data object NoClockApp : AlarmScheduleResult
    data class Failed(val error: Throwable) : AlarmScheduleResult
}

@Singleton
class CicloAlarmManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val nativeScheduler: NativeAlarmScheduler,
    private val internalScheduler: InternalAlarmScheduler
) {

    fun scheduleAlarm(
        hour: Int,
        minute: Int,
        triggerAtMillis: Long,
        label: String,
        cycles: Int,
        isSimple: Boolean,
        settings: UserSettings
    ): AlarmScheduleResult {
        return when (settings.alarmEngine) {
            AlarmEngine.INTERNAL -> {
                try {
                    internalScheduler.scheduleAlarm(
                        triggerAtMillis = triggerAtMillis,
                        label = label,
                        cycles = cycles,
                        isSimple = isSimple,
                        sunriseEnabled = settings.sunriseEnabled,
                        sunriseLeadMinutes = settings.sunriseLeadTimeMinutes
                    )
                    AlarmScheduleResult.Scheduled
                } catch (e: Exception) {
                    AlarmScheduleResult.Failed(e)
                }
            }
            AlarmEngine.NATIVE -> {
                when (val result = nativeScheduler.setAlarm(
                    hour = hour,
                    minute = minute,
                    label = label,
                    skipUi = settings.skipUi,
                    vibrate = settings.vibrate
                )) {
                    is NativeAlarmScheduler.AlarmResult.Sent -> AlarmScheduleResult.Scheduled
                    is NativeAlarmScheduler.AlarmResult.NoClockApp -> AlarmScheduleResult.NoClockApp
                    is NativeAlarmScheduler.AlarmResult.Failed -> AlarmScheduleResult.Failed(result.error)
                }
            }
        }
    }

    fun openNativeClock(): Boolean = nativeScheduler.openClockAlarms()

    fun resolvedClockPackage(): String? = nativeScheduler.resolvedClockPackage()

    fun scheduleTestAlarm(seconds: Int = 10) {
        internalScheduler.scheduleTestAlarm(seconds)
    }

    fun cancelInternalAlarm() {
        internalScheduler.cancelAlarm()
    }
}
