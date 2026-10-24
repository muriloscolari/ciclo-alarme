package me.muriloscolari.ciclo.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import me.muriloscolari.ciclo.BuildConfig
import me.muriloscolari.ciclo.R
import me.muriloscolari.ciclo.data.settings.AlarmEngine
import me.muriloscolari.ciclo.ui.designsystem.DarkBackgroundCard
import me.muriloscolari.ciclo.ui.designsystem.DawnWarmOrange
import me.muriloscolari.ciclo.ui.designsystem.GlassBorder
import me.muriloscolari.ciclo.ui.designsystem.GlassFill
import me.muriloscolari.ciclo.ui.designsystem.GlassSurface
import me.muriloscolari.ciclo.ui.designsystem.PrimaryLavender
import me.muriloscolari.ciclo.ui.designsystem.SegmentedPill
import me.muriloscolari.ciclo.ui.designsystem.TextPrimary
import me.muriloscolari.ciclo.ui.designsystem.TextSecondary
import me.muriloscolari.ciclo.ui.designsystem.TextTertiary

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.userSettings.collectAsState()
    val clockPkg = viewModel.detectedClockPackage
    val context = LocalContext.current
    var showTestDialog by remember { mutableStateOf(false) }

    val ringtoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            viewModel.updateCustomRingtone(uri?.toString())
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-0.5).sp
                )
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Seção: Ciclos de Sono
            item {
                SectionHeader(stringResource(R.string.settings_section_sleep))
                Spacer(modifier = Modifier.height(8.dp))
                GlassSurface(
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = GlassFill,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Slider: Duração do ciclo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.settings_cycle_duration_label),
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                            )
                            Text(
                                text = stringResource(
                                    R.string.settings_cycle_duration_value,
                                    settings.sleepSettings.cycleMinutes
                                ),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = PrimaryLavender,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Slider(
                            value = settings.sleepSettings.cycleMinutes.toFloat(),
                            onValueChange = { viewModel.updateCycleMinutes(it.toInt()) },
                            valueRange = 90f..120f,
                            steps = 5, // 90, 95, 100, 105, 110, 115, 120
                            colors = SliderDefaults.colors(
                                thumbColor = PrimaryLavender,
                                activeTrackColor = PrimaryLavender
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Slider: Latência para adormecer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.settings_latency_label),
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                            )
                            Text(
                                text = stringResource(
                                    R.string.settings_latency_value,
                                    settings.sleepSettings.fallAsleepMinutes
                                ),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = PrimaryLavender,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Slider(
                            value = settings.sleepSettings.fallAsleepMinutes.toFloat(),
                            onValueChange = { viewModel.updateFallAsleepMinutes(it.toInt()) },
                            valueRange = 0f..60f,
                            steps = 11, // múltiplos de 5min
                            colors = SliderDefaults.colors(
                                thumbColor = PrimaryLavender,
                                activeTrackColor = PrimaryLavender
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Slider: Ciclos Recomendados
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.settings_recommended_cycles_label),
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                            )
                            Text(
                                text = stringResource(
                                    R.string.settings_recommended_cycles_value,
                                    settings.sleepSettings.recommendedCycles
                                ),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = PrimaryLavender,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Slider(
                            value = settings.sleepSettings.recommendedCycles.toFloat(),
                            onValueChange = { viewModel.updateRecommendedCycles(it.toInt()) },
                            valueRange = 3f..6f,
                            steps = 2,
                            colors = SliderDefaults.colors(
                                thumbColor = PrimaryLavender,
                                activeTrackColor = PrimaryLavender
                            )
                        )
                    }
                }
            }

            // Seção: Mecanismo de Alarme (Alarme Próprio vs Relógio Nativo)
            item {
                SectionHeader(stringResource(R.string.settings_section_internal_alarm))
                Spacer(modifier = Modifier.height(8.dp))
                GlassSurface(
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = GlassFill,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = stringResource(R.string.settings_alarm_engine_label),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Seletor de mecanismo de alarme
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isInternal = settings.alarmEngine == AlarmEngine.INTERNAL
                            GlassSurface(
                                shape = RoundedCornerShape(14.dp),
                                backgroundColor = if (isInternal) PrimaryLavender.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                                borderColor = if (isInternal) PrimaryLavender.copy(alpha = 0.6f) else GlassBorder,
                                onClick = { viewModel.updateAlarmEngine(AlarmEngine.INTERNAL) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_engine_internal),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (isInternal) PrimaryLavender else TextSecondary,
                                            fontWeight = if (isInternal) FontWeight.SemiBold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            val isNative = settings.alarmEngine == AlarmEngine.NATIVE
                            GlassSurface(
                                shape = RoundedCornerShape(14.dp),
                                backgroundColor = if (isNative) PrimaryLavender.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                                borderColor = if (isNative) PrimaryLavender.copy(alpha = 0.6f) else GlassBorder,
                                onClick = { viewModel.updateAlarmEngine(AlarmEngine.NATIVE) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_engine_native),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (isNative) PrimaryLavender else TextSecondary,
                                            fontWeight = if (isNative) FontWeight.SemiBold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Opções específicas de acordo com o mecanismo ativo
                        if (settings.alarmEngine == AlarmEngine.INTERNAL) {
                            // 1. Simulador de Amanhecer (Sunrise)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.WbSunny,
                                            contentDescription = null,
                                            tint = DawnWarmOrange,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.settings_sunrise_label),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(R.string.settings_sunrise_desc),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TextTertiary,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = settings.sunriseEnabled,
                                    onCheckedChange = { viewModel.updateSunrise(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF130E26),
                                        checkedTrackColor = DawnWarmOrange
                                    )
                                )
                            }

                            if (settings.sunriseEnabled) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_sunrise_lead_label),
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                    )
                                    Text(
                                        text = stringResource(R.string.settings_cycle_duration_value, settings.sunriseLeadTimeMinutes),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = DawnWarmOrange,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                                Slider(
                                    value = settings.sunriseLeadTimeMinutes.toFloat(),
                                    onValueChange = { viewModel.updateSunriseLeadTime(it.toInt()) },
                                    valueRange = 3f..15f,
                                    steps = 3, // 3, 6, 9, 12, 15
                                    colors = SliderDefaults.colors(
                                        thumbColor = DawnWarmOrange,
                                        activeTrackColor = DawnWarmOrange
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 2. Despertar Suave (Volume Crescente)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.settings_gentle_wake_label),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(R.string.settings_gentle_wake_desc),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TextTertiary,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = settings.gentleWakeEnabled,
                                    onCheckedChange = { viewModel.updateGentleWake(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF130E26),
                                        checkedTrackColor = PrimaryLavender
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 3. Duração da Soneca
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_snooze_duration_label),
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                                )
                                Text(
                                    text = stringResource(R.string.settings_cycle_duration_value, settings.snoozeDurationMinutes),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = PrimaryLavender,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                            Slider(
                                value = settings.snoozeDurationMinutes.toFloat(),
                                onValueChange = { viewModel.updateSnoozeDuration(it.toInt()) },
                                valueRange = 5f..15f,
                                steps = 1, // 5, 10, 15
                                colors = SliderDefaults.colors(
                                    thumbColor = PrimaryLavender,
                                    activeTrackColor = PrimaryLavender
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // 4. Seletor de Toque do Alarme
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                            val currentUri = settings.customRingtoneUri?.let { Uri.parse(it) }
                                                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri)
                                        }
                                        ringtoneLauncher.launch(intent)
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MusicNote,
                                        contentDescription = null,
                                        tint = PrimaryLavender,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = stringResource(R.string.settings_ringtone_label),
                                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                                        )
                                        Text(
                                            text = if (settings.customRingtoneUri == null) {
                                                stringResource(R.string.settings_ringtone_default)
                                            } else {
                                                stringResource(R.string.settings_ringtone_custom)
                                            },
                                            style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                    contentDescription = null,
                                    tint = TextTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 5. Vibração ao Despertar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_vibrate_label),
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                                )
                                Switch(
                                    checked = settings.vibrate,
                                    onCheckedChange = { viewModel.updateVibrate(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF130E26),
                                        checkedTrackColor = PrimaryLavender
                                    )
                                )
                            }
                        } else {
                            // Relógio Nativo selecionado
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.settings_skip_ui_label),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(R.string.settings_skip_ui_desc),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TextTertiary,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Switch(
                                    checked = settings.skipUi,
                                    onCheckedChange = { viewModel.updateSkipUi(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF130E26),
                                        checkedTrackColor = PrimaryLavender
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_vibrate_label),
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                                )
                                Switch(
                                    checked = settings.vibrate,
                                    onCheckedChange = { viewModel.updateVibrate(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF130E26),
                                        checkedTrackColor = PrimaryLavender
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Seção: Confiabilidade Xiaomi / HyperOS & Teste de 10s (exibido para Alarme Próprio)
            if (settings.alarmEngine == AlarmEngine.INTERNAL) {
                item {
                    GlassSurface(
                        shape = RoundedCornerShape(24.dp),
                        backgroundColor = Color(0xFF161226).copy(alpha = 0.85f),
                        borderColor = PrimaryLavender.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Security,
                                    contentDescription = null,
                                    tint = PrimaryLavender,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.settings_xiaomi_card_title),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.settings_xiaomi_card_desc),
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Passo 1: Início Automático
                            XiaomiStepRow(
                                title = stringResource(R.string.settings_xiaomi_step_autostart),
                                onConfigure = { openAutostartSettings(context) }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Passo 2: Bateria Sem Restrições
                            XiaomiStepRow(
                                title = stringResource(R.string.settings_xiaomi_step_battery),
                                onConfigure = { openBatterySettings(context) }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Passo 3: Exibir na Tela de Bloqueio
                            XiaomiStepRow(
                                title = stringResource(R.string.settings_xiaomi_step_lockscreen),
                                onConfigure = { openAppDetails(context) }
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Botão: Testar alarme em 10 segundos
                            Button(
                                onClick = {
                                    viewModel.scheduleTestAlarm(10)
                                    showTestDialog = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryLavender
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.test_alarm_button),
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = Color.Black,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Seção: Aparência
            item {
                SectionHeader(stringResource(R.string.settings_section_appearance))
                Spacer(modifier = Modifier.height(8.dp))
                GlassSurface(
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = GlassFill,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_blur_label),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.settings_blur_desc),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextTertiary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = settings.blurEffects,
                                onCheckedChange = { viewModel.updateBlurEffects(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF130E26),
                                    checkedTrackColor = PrimaryLavender
                                )
                            )
                        }
                    }
                }
            }

            // Seção: Sobre
            item {
                SectionHeader(stringResource(R.string.settings_section_about))
                Spacer(modifier = Modifier.height(8.dp))
                GlassSurface(
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = GlassFill,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Relógio detectado
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.settings_clock_detected_label),
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                            )
                            Text(
                                text = clockPkg ?: stringResource(R.string.settings_clock_detected_none),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = PrimaryLavender,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Versão
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.settings_version_label),
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                            )
                            Text(
                                text = BuildConfig.VERSION_NAME,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Repositório GitHub
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.settings_source_code_label),
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                            )
                            Button(
                                onClick = {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://github.com/muriloscolari/ciclo-alarme")
                                    )
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = PrimaryLavender
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(text = "GitHub", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.settings_license_label),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextTertiary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            // Botão Restaurar Padrões
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.resetToDefaults() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x15FFFFFF),
                        contentColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.settings_reset_button))
                }
            }
        }
    }

    // Diálogo de Teste do Alarme em 10 Segundos
    if (showTestDialog) {
        AlertDialog(
            onDismissRequest = { showTestDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.test_alarm_dialog_title),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.test_alarm_dialog_message),
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showTestDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryLavender)
                ) {
                    Text(text = stringResource(R.string.action_ok), color = Color.Black)
                }
            },
            containerColor = DarkBackgroundCard,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun XiaomiStepRow(
    title: String,
    onConfigure: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        GlassSurface(
            shape = RoundedCornerShape(10.dp),
            backgroundColor = PrimaryLavender.copy(alpha = 0.15f),
            borderColor = PrimaryLavender.copy(alpha = 0.40f),
            onClick = onConfigure
        ) {
            Text(
                text = stringResource(R.string.action_open_setting),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = PrimaryLavender,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

private fun openAutostartSettings(context: Context) {
    val intents = listOf(
        Intent().setComponent(android.content.ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
        Intent().setComponent(android.content.ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings")),
        Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    )
    for (i in intents) {
        if (i.resolveActivity(context.packageManager) != null) {
            context.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        }
    }
    context.startActivity(intents.last().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

private fun openBatterySettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    } else {
        Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }
    runCatching {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }.onFailure {
        val fallback = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

private fun openAppDetails(context: Context) {
    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:${context.packageName}")
    }
    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            color = PrimaryLavender,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        ),
        modifier = Modifier.padding(start = 4.dp)
    )
}
