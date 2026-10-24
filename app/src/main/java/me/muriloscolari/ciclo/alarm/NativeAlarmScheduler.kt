package me.muriloscolari.ciclo.alarm

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NativeAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    sealed interface AlarmResult {
        data object Sent : AlarmResult
        data object NoClockApp : AlarmResult
        data class Failed(val error: Throwable) : AlarmResult
    }

    /**
     * Envia o comando para criar um alarme no app de Relógio padrão do aparelho via [AlarmClock.ACTION_SET_ALARM].
     *
     * @param hour Hora do alarme (0..23).
     * @param minute Minuto do alarme (0..59).
     * @param label Título descritivo exibido no alarme.
     * @param skipUi Se verdadeiro, solicita ao app de relógio salvar sem abrir a tela de edição (sujeito à implementação do fabricante).
     * @param vibrate Se o alarme deve vibrar ao tocar.
     */
    fun setAlarm(
        hour: Int,
        minute: Int,
        label: String,
        skipUi: Boolean = true,
        vibrate: Boolean = true
    ): AlarmResult {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, skipUi)
            putExtra(AlarmClock.EXTRA_VIBRATE, vibrate)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val resolveInfo = intent.resolveActivity(context.packageManager)
        if (resolveInfo == null) {
            return AlarmResult.NoClockApp
        }

        return runCatching {
            context.startActivity(intent)
        }.fold(
            onSuccess = { AlarmResult.Sent },
            onFailure = { AlarmResult.Failed(it) }
        )
    }

    /**
     * Abre a lista de alarmes do app de Relógio nativo via [AlarmClock.ACTION_SHOW_ALARMS].
     */
    fun openClockAlarms(): Boolean {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) == null) {
            return false
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    /**
     * Retorna o nome do pacote do aplicativo de Relógio que responderá à ação de definir alarme.
     * Útil para diagnóstico em Ajustes > Sobre (ex.: "com.android.deskclock" na Xiaomi).
     */
    fun resolvedClockPackage(): String? {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM)
        val componentName = intent.resolveActivity(context.packageManager)
        return componentName?.packageName
    }
}
