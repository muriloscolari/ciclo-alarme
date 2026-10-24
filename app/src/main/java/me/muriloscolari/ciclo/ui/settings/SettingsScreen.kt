package me.muriloscolari.ciclo.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import me.muriloscolari.ciclo.ui.designsystem.GlassFill
import me.muriloscolari.ciclo.ui.designsystem.GlassSurface
import me.muriloscolari.ciclo.ui.designsystem.PrimaryLavender
import me.muriloscolari.ciclo.ui.designsystem.TextPrimary
import me.muriloscolari.ciclo.ui.designsystem.TextSecondary
import me.muriloscolari.ciclo.ui.designsystem.TextTertiary

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.userSettings.collectAsState()
    val clockPkg = viewModel.detectedClockPackage
    val context = LocalContext.current

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
                    hazeState = hazeState,
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

            // Seção: Relógio Nativo
            item {
                SectionHeader(stringResource(R.string.settings_section_alarm))
                Spacer(modifier = Modifier.height(8.dp))
                GlassSurface(
                    shape = RoundedCornerShape(24.dp),
                    hazeState = hazeState,
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

            // Seção: Aparência
            item {
                SectionHeader(stringResource(R.string.settings_section_appearance))
                Spacer(modifier = Modifier.height(8.dp))
                GlassSurface(
                    shape = RoundedCornerShape(24.dp),
                    hazeState = hazeState,
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
                    hazeState = hazeState,
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
