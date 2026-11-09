package com.example

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.data.AimPreferencesManager
import com.example.service.AimOverlayService
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AimCalculatorScreen
import com.example.ui.screens.AimSimulatorScreen
import com.example.ui.screens.BankShotMasterScreen
import com.example.ui.screens.FloatingSettingsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PermissionsSetupScreen
import com.example.ui.screens.Screen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var prefsManager: AimPreferencesManager
    private var isOverlayRunning by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefsManager = AimPreferencesManager.getInstance(this)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    MainAppNavigation(
                        prefsManager = prefsManager,
                        isServiceActive = isOverlayRunning,
                        onToggleService = { enable ->
                            handleToggleOverlay(enable)
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateServiceStatus()
    }

    private fun updateServiceStatus() {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        val isRunning = manager.getRunningServices(Int.MAX_VALUE).any {
            it.service.className == AimOverlayService::class.java.name
        }
        isOverlayRunning = isRunning
    }

    private fun handleToggleOverlay(enable: Boolean) {
        val intent = Intent(this, AimOverlayService::class.java)
        if (enable) {
            if (Settings.canDrawOverlays(this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(this, intent)
                } else {
                    startService(intent)
                }
                isOverlayRunning = true
            }
        } else {
            stopService(intent)
            isOverlayRunning = false
        }
    }
}

@Composable
fun MainAppNavigation(
    prefsManager: AimPreferencesManager,
    isServiceActive: Boolean,
    onToggleService: (Boolean) -> Unit
) {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }

    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
        when (screen) {
            Screen.SPLASH -> {
                SplashScreen(onNavigateHome = { currentScreen = Screen.HOME })
            }
            Screen.HOME -> {
                HomeScreen(
                    isServiceActive = isServiceActive,
                    onToggleService = onToggleService,
                    onNavigate = { target -> currentScreen = target }
                )
            }
            Screen.AIM_SIMULATOR -> {
                AimSimulatorScreen(
                    prefsManager = prefsManager,
                    onBack = { currentScreen = Screen.HOME }
                )
            }
            Screen.AIM_CALCULATOR -> {
                AimCalculatorScreen(
                    prefsManager = prefsManager,
                    onBack = { currentScreen = Screen.HOME }
                )
            }
            Screen.BANK_SHOT_MASTER -> {
                BankShotMasterScreen(
                    prefsManager = prefsManager,
                    onBack = { currentScreen = Screen.HOME }
                )
            }
            Screen.FLOATING_SETTINGS -> {
                FloatingSettingsScreen(
                    prefsManager = prefsManager,
                    onBack = { currentScreen = Screen.HOME }
                )
            }
            Screen.PERMISSIONS_SETUP -> {
                PermissionsSetupScreen(
                    onBack = { currentScreen = Screen.HOME }
                )
            }
            Screen.SETTINGS -> {
                SettingsScreen(
                    prefsManager = prefsManager,
                    onBack = { currentScreen = Screen.HOME }
                )
            }
            Screen.ABOUT -> {
                AboutScreen(
                    onBack = { currentScreen = Screen.HOME }
                )
            }
        }
    }
}
