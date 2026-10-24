package me.muriloscolari.ciclo.alarm

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import me.muriloscolari.ciclo.data.settings.SettingsRepository
import me.muriloscolari.ciclo.ui.designsystem.CicloTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

    companion object {
        const val ACTION_RINGING = "me.muriloscolari.ciclo.ACTION_RINGING"
        const val ACTION_SUNRISE = "me.muriloscolari.ciclo.ACTION_SUNRISE"
        const val ACTION_FINISH_UI = "me.muriloscolari.ciclo.ACTION_FINISH_UI"
    }

    @Inject
    lateinit var internalAlarmScheduler: InternalAlarmScheduler

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val finishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_FINISH_UI) {
                finishAndRemoveTask()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupLockscreenFlags()

        val action = intent.action ?: ACTION_RINGING
        val label = intent.getStringExtra(InternalAlarmScheduler.EXTRA_LABEL) ?: ""
        val cycles = intent.getIntExtra(InternalAlarmScheduler.EXTRA_CYCLES, 5)
        val isSimple = intent.getBooleanExtra(InternalAlarmScheduler.EXTRA_IS_SIMPLE, false)
        val triggerMillis = intent.getLongExtra(InternalAlarmScheduler.EXTRA_TRIGGER_MILLIS, System.currentTimeMillis())
        val leadMinutes = intent.getIntExtra(InternalAlarmScheduler.EXTRA_SUNRISE_LEAD_MINUTES, 5)

        val isInitialSunrise = action == ACTION_SUNRISE

        // Registra o receiver para fechar a UI caso o usuário descarte pela notificação
        val filter = IntentFilter(ACTION_FINISH_UI)
        androidx.core.content.ContextCompat.registerReceiver(
            this,
            finishReceiver,
            filter,
            androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
        )

        setContent {
            var isSunriseOnly by remember { mutableStateOf(isInitialSunrise) }
            var sunriseProgress by remember { mutableFloatStateOf(if (isInitialSunrise) 0.05f else 1.0f) }
            var currentTimeText by remember { mutableStateOf(formatCurrentTime()) }
            var snoozeDuration by remember { mutableStateOf(10) }

            // Observa configurações para obter duração da soneca
            LaunchedEffect(Unit) {
                val settings = settingsRepository.settingsFlow.first()
                snoozeDuration = settings.snoozeDurationMinutes
            }

            // Atualiza relógio e progresso do amanhecer
            LaunchedEffect(isSunriseOnly) {
                while (true) {
                    currentTimeText = formatCurrentTime()

                    if (isSunriseOnly) {
                        val now = System.currentTimeMillis()
                        val totalDuration = (leadMinutes * 60 * 1000L).coerceAtLeast(1000L)
                        val elapsed = totalDuration - (triggerMillis - now)
                        val progress = (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0.05f, 1.0f)
                        sunriseProgress = progress

                        // Ajusta o brilho da janela suavemente com a luz do sol
                        setScreenBrightness(0.05f + (0.90f * progress))

                        // Se atingiu o horário do alarme, muda o estado para tocar
                        if (now >= triggerMillis) {
                            isSunriseOnly = false
                            sunriseProgress = 1.0f
                            setScreenBrightness(1.0f)
                        }
                    } else {
                        sunriseProgress = 1.0f
                        setScreenBrightness(1.0f)
                    }

                    delay(1000L)
                }
            }

            CicloTheme {
                AlarmScreen(
                    currentTimeText = currentTimeText,
                    label = label,
                    cycles = cycles,
                    isSimple = isSimple,
                    isSunriseOnly = isSunriseOnly,
                    sunriseProgress = sunriseProgress,
                    snoozeDurationMinutes = snoozeDuration,
                    onDismiss = {
                        val dismissIntent = Intent(this@AlarmActivity, AlarmReceiver::class.java).apply {
                            this.action = InternalAlarmScheduler.ACTION_DISMISS_ALARM
                        }
                        sendBroadcast(dismissIntent)
                        finishAndRemoveTask()
                    },
                    onSnooze = {
                        val snoozeIntent = Intent(this@AlarmActivity, AlarmReceiver::class.java).apply {
                            this.action = InternalAlarmScheduler.ACTION_SNOOZE_ALARM
                            putExtra(InternalAlarmScheduler.EXTRA_LABEL, label)
                            putExtra(InternalAlarmScheduler.EXTRA_CYCLES, cycles)
                            putExtra(InternalAlarmScheduler.EXTRA_IS_SIMPLE, isSimple)
                        }
                        sendBroadcast(snoozeIntent)
                        finishAndRemoveTask()
                    }
                )
            }
        }
    }

    private fun setupLockscreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KeyguardManager::class.java)
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun setScreenBrightness(brightness: Float) {
        runCatching {
            val lp = window.attributes
            lp.screenBrightness = brightness.coerceIn(0.05f, 1.0f)
            window.attributes = lp
        }
    }

    private fun formatCurrentTime(): String {
        val now = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return now.format(formatter)
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(finishReceiver)
        } catch (e: Exception) {
            // Já desregistrado
        }
        super.onDestroy()
    }
}
