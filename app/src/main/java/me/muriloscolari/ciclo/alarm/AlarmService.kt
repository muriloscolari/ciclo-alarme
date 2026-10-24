package me.muriloscolari.ciclo.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.muriloscolari.ciclo.R
import me.muriloscolari.ciclo.data.settings.SettingsRepository
import javax.inject.Inject

@AndroidEntryPoint
class AlarmService : Service() {

    companion object {
        const val CHANNEL_ID = "ciclo_ringing_channel"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START_RINGING = "me.muriloscolari.ciclo.ACTION_START_RINGING"
        const val ACTION_STOP_RINGING = "me.muriloscolari.ciclo.ACTION_STOP_RINGING"

        var isRinging = false
            private set
    }

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var internalAlarmScheduler: InternalAlarmScheduler

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var volumeJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_RINGING -> {
                stopAlarm()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START_RINGING -> {
                val label = intent.getStringExtra(InternalAlarmScheduler.EXTRA_LABEL) ?: getString(R.string.alarm_clock_label_simple)
                val cycles = intent.getIntExtra(InternalAlarmScheduler.EXTRA_CYCLES, 5)
                val isSimple = intent.getBooleanExtra(InternalAlarmScheduler.EXTRA_IS_SIMPLE, false)
                val triggerMillis = intent.getLongExtra(InternalAlarmScheduler.EXTRA_TRIGGER_MILLIS, System.currentTimeMillis())

                startAlarmForeground(label, cycles, isSimple, triggerMillis)
            }
        }
        return START_STICKY
    }

    private fun startAlarmForeground(label: String, cycles: Int, isSimple: Boolean, triggerMillis: Long) {
        isRinging = true

        // 1. WakeLock para manter a CPU ativa durante o toque (máximo de 10 min por segurança)
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ciclo:alarm_service_wakelock").apply {
            acquire(10 * 60 * 1000L)
        }

        // 2. PendingIntents para Soneca e Desligar via Notificação
        val dismissIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = InternalAlarmScheduler.ACTION_DISMISS_ALARM
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            201,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = InternalAlarmScheduler.ACTION_SNOOZE_ALARM
            putExtra(InternalAlarmScheduler.EXTRA_LABEL, label)
            putExtra(InternalAlarmScheduler.EXTRA_CYCLES, cycles)
            putExtra(InternalAlarmScheduler.EXTRA_IS_SIMPLE, isSimple)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this,
            202,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Fullscreen Intent para abrir a AlarmActivity
        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            action = AlarmActivity.ACTION_RINGING
            putExtra(InternalAlarmScheduler.EXTRA_LABEL, label)
            putExtra(InternalAlarmScheduler.EXTRA_CYCLES, cycles)
            putExtra(InternalAlarmScheduler.EXTRA_IS_SIMPLE, isSimple)
            putExtra(InternalAlarmScheduler.EXTRA_TRIGGER_MILLIS, triggerMillis)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            203,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(label)
            .setContentText(getString(R.string.alarm_ringing_notification_text))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.action_dismiss_alarm), dismissPendingIntent)
            .addAction(android.R.drawable.ic_popup_sync, getString(R.string.action_snooze_alarm), snoozePendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // 4. Iniciar áudio e vibração assincronamente com as preferências do usuário
        serviceScope.launch {
            val userSettings = settingsRepository.settingsFlow.first()
            playAudio(userSettings.customRingtoneUri, userSettings.gentleWakeEnabled)
            if (userSettings.vibrate) {
                startVibration()
            }
        }
    }

    private fun playAudio(customUriString: String?, gentleWake: Boolean) {
        try {
            val soundUri = customUriString?.let { Uri.parse(it) }
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, soundUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                if (gentleWake) {
                    setVolume(0.05f, 0.05f)
                } else {
                    setVolume(1.0f, 1.0f)
                }
                prepare()
                start()
            }

            if (gentleWake) {
                volumeJob?.cancel()
                volumeJob = serviceScope.launch {
                    val totalSteps = 30
                    for (step in 1..totalSteps) {
                        delay(1000L)
                        val fraction = step.toFloat() / totalSteps.toFloat()
                        val volume = 0.05f + (0.95f * fraction)
                        runCatching {
                            mediaPlayer?.setVolume(volume, volume)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startVibration() {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            val pattern = longArrayOf(0, 500, 500, 500, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(pattern, 0)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAlarm() {
        isRinging = false
        volumeJob?.cancel()
        volumeJob = null

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            vibrator?.cancel()
            vibrator = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (wakeLock?.isHeld == true) {
            try {
                wakeLock?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        wakeLock = null

        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_alarm_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_alarm_desc)
                setSound(null, null) // Som é tocado diretamente pelo MediaPlayer via USAGE_ALARM
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
