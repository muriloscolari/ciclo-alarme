package me.muriloscolari.ciclo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import me.muriloscolari.ciclo.data.settings.SettingsRepository
import me.muriloscolari.ciclo.data.settings.UserSettings
import me.muriloscolari.ciclo.ui.designsystem.CicloTheme
import me.muriloscolari.ciclo.ui.navigation.CicloNavHost
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userSettings by settingsRepository.settingsFlow.collectAsState(initial = UserSettings())

            CicloTheme(
                themeMode = userSettings.themeMode,
                dynamicColor = userSettings.useDynamicColors,
                blurEnabled = userSettings.blurEffects
            ) {
                CicloNavHost()
            }
        }
    }
}
