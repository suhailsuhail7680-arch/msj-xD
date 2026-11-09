package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AimConfiguration
import com.example.model.GuidelineColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AimPreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("msj_xd_aim_prefs", Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<AimConfiguration> = _configFlow.asStateFlow()

    fun getConfig(): AimConfiguration = _configFlow.value

    fun updateConfig(update: (AimConfiguration) -> AimConfiguration) {
        val newConfig = update(_configFlow.value)
        _configFlow.value = newConfig
        saveConfig(newConfig)
    }

    private fun loadConfig(): AimConfiguration {
        val thickness = prefs.getFloat(KEY_THICKNESS, 4f)
        val length = prefs.getFloat(KEY_LENGTH, 1400f)
        val primaryColorName = prefs.getString(KEY_PRIMARY_COLOR, GuidelineColor.NEON_GOLD.name)
        val secondaryColorName = prefs.getString(KEY_SECONDARY_COLOR, GuidelineColor.ELECTRIC_CYAN.name)
        val opacity = prefs.getFloat(KEY_OPACITY, 0.95f)
        val bankShots = prefs.getBoolean(KEY_BANK_SHOTS, true)
        val pocketGuides = prefs.getBoolean(KEY_POCKET_GUIDES, true)
        val extendedLines = prefs.getBoolean(KEY_EXTENDED_LINES, true)
        val ballRadius = prefs.getFloat(KEY_BALL_RADIUS, 36f)
        val dpadStep = prefs.getFloat(KEY_DPAD_STEP, 2f)
        val isVisible = prefs.getBoolean(KEY_IS_VISIBLE, true)
        val haptic = prefs.getBoolean(KEY_HAPTIC, true)

        val primary = try {
            GuidelineColor.valueOf(primaryColorName ?: GuidelineColor.NEON_GOLD.name)
        } catch (e: Exception) {
            GuidelineColor.NEON_GOLD
        }

        val secondary = try {
            GuidelineColor.valueOf(secondaryColorName ?: GuidelineColor.ELECTRIC_CYAN.name)
        } catch (e: Exception) {
            GuidelineColor.ELECTRIC_CYAN
        }

        return AimConfiguration(
            lineThickness = thickness,
            lineLength = length,
            primaryColor = primary,
            secondaryColor = secondary,
            opacity = opacity,
            showBankShots = bankShots,
            showPocketGuides = pocketGuides,
            showExtendedLines = extendedLines,
            ballRadius = ballRadius,
            dpadStepPx = dpadStep,
            isGuidelineVisible = isVisible,
            hapticFeedbackEnabled = haptic
        )
    }

    private fun saveConfig(config: AimConfiguration) {
        prefs.edit()
            .putFloat(KEY_THICKNESS, config.lineThickness)
            .putFloat(KEY_LENGTH, config.lineLength)
            .putString(KEY_PRIMARY_COLOR, config.primaryColor.name)
            .putString(KEY_SECONDARY_COLOR, config.secondaryColor.name)
            .putFloat(KEY_OPACITY, config.opacity)
            .putBoolean(KEY_BANK_SHOTS, config.showBankShots)
            .putBoolean(KEY_POCKET_GUIDES, config.showPocketGuides)
            .putBoolean(KEY_EXTENDED_LINES, config.showExtendedLines)
            .putFloat(KEY_BALL_RADIUS, config.ballRadius)
            .putFloat(KEY_DPAD_STEP, config.dpadStepPx)
            .putBoolean(KEY_IS_VISIBLE, config.isGuidelineVisible)
            .putBoolean(KEY_HAPTIC, config.hapticFeedbackEnabled)
            .apply()
    }

    companion object {
        private const val KEY_THICKNESS = "guideline_thickness"
        private const val KEY_LENGTH = "guideline_length"
        private const val KEY_PRIMARY_COLOR = "primary_guideline_color"
        private const val KEY_SECONDARY_COLOR = "secondary_guideline_color"
        private const val KEY_OPACITY = "guideline_opacity"
        private const val KEY_BANK_SHOTS = "show_bank_shots"
        private const val KEY_POCKET_GUIDES = "show_pocket_guides"
        private const val KEY_EXTENDED_LINES = "show_extended_lines"
        private const val KEY_BALL_RADIUS = "ball_radius"
        private const val KEY_DPAD_STEP = "dpad_step_px"
        private const val KEY_IS_VISIBLE = "guidelines_visible"
        private const val KEY_HAPTIC = "haptic_enabled"

        @Volatile
        private var INSTANCE: AimPreferencesManager? = null

        fun getInstance(context: Context): AimPreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AimPreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
