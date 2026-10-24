package me.muriloscolari.ciclo.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.muriloscolari.ciclo.data.settings.SettingsRepository
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var internalAlarmScheduler: InternalAlarmScheduler

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        when (action) {
            InternalAlarmScheduler.ACTION_ALARM_TRIGGER -> {
                val label = intent.getStringExtra(InternalAlarmScheduler.EXTRA_LABEL) ?: ""
                val cycles = intent.getIntExtra(InternalAlarmScheduler.EXTRA_CYCLES, 5)
                val isSimple = intent.getBooleanExtra(InternalAlarmScheduler.EXTRA_IS_SIMPLE, false)
                val triggerMillis = intent.getLongExtra(InternalAlarmScheduler.EXTRA_TRIGGER_MILLIS, System.currentTimeMillis())

                // 1. Inicia o serviço em foreground para tocar som, vibrar e exibir notificação
                val serviceIntent = Intent(context, AlarmService::class.java).apply {
                    this.action = AlarmService.ACTION_START_RINGING
                    putExtra(InternalAlarmScheduler.EXTRA_LABEL, label)
                    putExtra(InternalAlarmScheduler.EXTRA_CYCLES, cycles)
                    putExtra(InternalAlarmScheduler.EXTRA_IS_SIMPLE, isSimple)
                    putExtra(InternalAlarmScheduler.EXTRA_TRIGGER_MILLIS, triggerMillis)
                }
                ContextCompat.startForegroundService(context, serviceIntent)

                // 2. Abre a tela de alarme sobre a tela de bloqueio
                val activityIntent = Intent(context, AlarmActivity::class.java).apply {
                    this.action = AlarmActivity.ACTION_RINGING
                    putExtra(InternalAlarmScheduler.EXTRA_LABEL, label)
                    putExtra(InternalAlarmScheduler.EXTRA_CYCLES, cycles)
                    putExtra(InternalAlarmScheduler.EXTRA_IS_SIMPLE, isSimple)
                    putExtra(InternalAlarmScheduler.EXTRA_TRIGGER_MILLIS, triggerMillis)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(activityIntent)
            }

            InternalAlarmScheduler.ACTION_SUNRISE_TRIGGER -> {
                val label = intent.getStringExtra(InternalAlarmScheduler.EXTRA_LABEL) ?: ""
                val cycles = intent.getIntExtra(InternalAlarmScheduler.EXTRA_CYCLES, 5)
                val isSimple = intent.getBooleanExtra(InternalAlarmScheduler.EXTRA_IS_SIMPLE, false)
                val triggerMillis = intent.getLongExtra(InternalAlarmScheduler.EXTRA_TRIGGER_MILLIS, System.currentTimeMillis())
                val leadMinutes = intent.getIntExtra(InternalAlarmScheduler.EXTRA_SUNRISE_LEAD_MINUTES, 5)

                // Abre a tela em modo nascer do sol suave (sem áudio ainda)
                val activityIntent = Intent(context, AlarmActivity::class.java).apply {
                    this.action = AlarmActivity.ACTION_SUNRISE
                    putExtra(InternalAlarmScheduler.EXTRA_LABEL, label)
                    putExtra(InternalAlarmScheduler.EXTRA_CYCLES, cycles)
                    putExtra(InternalAlarmScheduler.EXTRA_IS_SIMPLE, isSimple)
                    putExtra(InternalAlarmScheduler.EXTRA_TRIGGER_MILLIS, triggerMillis)
                    putExtra(InternalAlarmScheduler.EXTRA_SUNRISE_LEAD_MINUTES, leadMinutes)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(activityIntent)
            }

            InternalAlarmScheduler.ACTION_DISMISS_ALARM -> {
                // Para o serviço de áudio
                val serviceIntent = Intent(context, AlarmService::class.java).apply {
                    this.action = AlarmService.ACTION_STOP_RINGING
                }
                context.startService(serviceIntent)

                // Envia broadcast local para fechar a tela de alarme se ainda estiver aberta
                val closeIntent = Intent(AlarmActivity.ACTION_FINISH_UI).apply {
                    setPackage(context.packageName)
                }
                context.sendBroadcast(closeIntent)
            }

            InternalAlarmScheduler.ACTION_SNOOZE_ALARM -> {
                // Para o serviço atual
                val serviceIntent = Intent(context, AlarmService::class.java).apply {
                    this.action = AlarmService.ACTION_STOP_RINGING
                }
                context.startService(serviceIntent)

                val closeIntent = Intent(AlarmActivity.ACTION_FINISH_UI).apply {
                    setPackage(context.packageName)
                }
                context.sendBroadcast(closeIntent)

                val label = intent.getStringExtra(InternalAlarmScheduler.EXTRA_LABEL) ?: ""
                val cycles = intent.getIntExtra(InternalAlarmScheduler.EXTRA_CYCLES, 5)
                val isSimple = intent.getBooleanExtra(InternalAlarmScheduler.EXTRA_IS_SIMPLE, false)

                // Agenda novo disparo com a duração de soneca das configurações
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val settings = settingsRepository.settingsFlow.first()
                        internalAlarmScheduler.scheduleSnooze(
                            minutes = settings.snoozeDurationMinutes,
                            label = label,
                            cycles = cycles,
                            isSimple = isSimple
                        )
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                // Ao reiniciar o dispositivo, o sistema limpa os alarmes RTC.
                // Em implementações avançadas, pode recarregar o alarme ativo se desejado.
            }
        }
    }
}
