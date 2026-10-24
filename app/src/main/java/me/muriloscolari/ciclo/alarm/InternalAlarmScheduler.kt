package me.muriloscolari.ciclo.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import me.muriloscolari.ciclo.MainActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InternalAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val ACTION_ALARM_TRIGGER = "me.muriloscolari.ciclo.ACTION_ALARM_TRIGGER"
        const val ACTION_SUNRISE_TRIGGER = "me.muriloscolari.ciclo.ACTION_SUNRISE_TRIGGER"
        const val ACTION_DISMISS_ALARM = "me.muriloscolari.ciclo.ACTION_DISMISS_ALARM"
        const val ACTION_SNOOZE_ALARM = "me.muriloscolari.ciclo.ACTION_SNOOZE_ALARM"

        const val EXTRA_LABEL = "extra_label"
        const val EXTRA_CYCLES = "extra_cycles"
        const val EXTRA_IS_SIMPLE = "extra_is_simple"
        const val EXTRA_TRIGGER_MILLIS = "extra_trigger_millis"
        const val EXTRA_SUNRISE_LEAD_MINUTES = "extra_sunrise_lead_minutes"

        private const val REQUEST_SHOW_ALARM = 1001
        private const val REQUEST_TRIGGER_ALARM = 1002
        private const val REQUEST_SUNRISE_ALARM = 1003
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun scheduleAlarm(
        triggerAtMillis: Long,
        label: String,
        cycles: Int,
        isSimple: Boolean,
        sunriseEnabled: Boolean = true,
        sunriseLeadMinutes: Int = 5
    ) {
        // 1. Agenda o alarme principal com AlarmClockInfo (acorda o hardware RTC)
        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_SHOW_ALARM,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
            putExtra(EXTRA_LABEL, label)
            putExtra(EXTRA_CYCLES, cycles)
            putExtra(EXTRA_IS_SIMPLE, isSimple)
            putExtra(EXTRA_TRIGGER_MILLIS, triggerAtMillis)
        }
        val triggerPendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_TRIGGER_ALARM,
            triggerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent),
            triggerPendingIntent
        )

        // 2. Se o simulador de amanhecer estiver ativo, agenda o acendimento suave antes
        if (sunriseEnabled && sunriseLeadMinutes > 0) {
            val sunriseMillis = triggerAtMillis - (sunriseLeadMinutes * 60 * 1000L)
            val now = System.currentTimeMillis()
            if (sunriseMillis > now) {
                val sunriseIntent = Intent(context, AlarmReceiver::class.java).apply {
                    action = ACTION_SUNRISE_TRIGGER
                    putExtra(EXTRA_LABEL, label)
                    putExtra(EXTRA_CYCLES, cycles)
                    putExtra(EXTRA_IS_SIMPLE, isSimple)
                    putExtra(EXTRA_TRIGGER_MILLIS, triggerAtMillis)
                    putExtra(EXTRA_SUNRISE_LEAD_MINUTES, sunriseLeadMinutes)
                }
                val sunrisePendingIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_SUNRISE_ALARM,
                    sunriseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    sunriseMillis,
                    sunrisePendingIntent
                )
            }
        }
    }

    fun scheduleSnooze(
        minutes: Int,
        label: String,
        cycles: Int,
        isSimple: Boolean
    ) {
        val triggerAtMillis = System.currentTimeMillis() + (minutes * 60 * 1000L)
        // Soneca não precisa de amanhecer antecipado, toca direto
        scheduleAlarm(
            triggerAtMillis = triggerAtMillis,
            label = label,
            cycles = cycles,
            isSimple = isSimple,
            sunriseEnabled = false,
            sunriseLeadMinutes = 0
        )
    }

    fun scheduleTestAlarm(seconds: Int = 10) {
        val triggerAtMillis = System.currentTimeMillis() + (seconds * 1000L)
        scheduleAlarm(
            triggerAtMillis = triggerAtMillis,
            label = "Teste de 10s · Ciclo",
            cycles = 5,
            isSimple = false,
            sunriseEnabled = false,
            sunriseLeadMinutes = 0
        )
    }

    fun cancelAlarm() {
        val triggerIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
        }
        val triggerPendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_TRIGGER_ALARM,
            triggerIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (triggerPendingIntent != null) {
            alarmManager.cancel(triggerPendingIntent)
            triggerPendingIntent.cancel()
        }

        val sunriseIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SUNRISE_TRIGGER
        }
        val sunrisePendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_SUNRISE_ALARM,
            sunriseIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (sunrisePendingIntent != null) {
            alarmManager.cancel(sunrisePendingIntent)
            sunrisePendingIntent.cancel()
        }
    }
}
