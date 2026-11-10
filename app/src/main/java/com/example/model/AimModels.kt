package com.example.model

import androidx.compose.ui.graphics.Color
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector2D(val x: Float, val y: Float) {
    operator fun plus(other: Vector2D) = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D) = Vector2D(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector2D(x * scalar, y * scalar)
    operator fun div(scalar: Float) = if (scalar != 0f) Vector2D(x / scalar, y / scalar) else Vector2D(0f, 0f)

    fun length(): Float = sqrt(x * x + y * y)

    fun normalized(): Vector2D {
        val len = length()
        return if (len > 0.0001f) Vector2D(x / len, y / len) else Vector2D(0f, 0f)
    }

    fun distanceTo(other: Vector2D): Float {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx * dx + dy * dy)
    }

    fun dot(other: Vector2D): Float = x * other.x + y * other.y

    companion object {
        val ZERO = Vector2D(0f, 0f)

        fun fromAngle(angleDegrees: Float): Vector2D {
            val rad = Math.toRadians(angleDegrees.toDouble())
            return Vector2D(cos(rad).toFloat(), sin(rad).toFloat())
        }
    }
}

enum class GuidelineColor(val title: String, val color: Color, val hex: String) {
    NEON_GOLD("Neon Gold", Color(0xFFFFD700), "#FFD700"),
    CYBER_YELLOW("Cyber Yellow", Color(0xFFFFEB3B), "#FFEB3B"),
    ELECTRIC_CYAN("Electric Cyan", Color(0xFF00E5FF), "#00E5FF"),
    EMERALD_GREEN("Emerald Green", Color(0xFF00E676), "#00E676"),
    HOT_PINK("Hot Pink", Color(0xFFFF4081), "#FF4081"),
    PURE_WHITE("Pure White", Color(0xFFFFFFFF), "#FFFFFF")
}

data class AimConfiguration(
    val lineThickness: Float = 4f,
    val lineLength: Float = 1600f,
    val primaryColor: GuidelineColor = GuidelineColor.NEON_GOLD,
    val secondaryColor: GuidelineColor = GuidelineColor.ELECTRIC_CYAN,
    val opacity: Float = 0.95f,
    val showBankShots: Boolean = true,
    val maxBankBounces: Int = 4, // 2-Bank and 4-Bank aim modes
    val showPocketGuides: Boolean = true,
    val showExtendedLines: Boolean = true,
    val ballRadius: Float = 36f,
    val dpadStepPx: Float = 2f,
    val isGuidelineVisible: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true
)

data class BallPosition(
    val position: Vector2D,
    val radius: Float = 36f
)

data class Pocket(
    val id: String,
    val position: Vector2D,
    val radius: Float = 44f
)

data class CushionReflection(
    val hitPoint: Vector2D,
    val bounceVector: Vector2D,
    val endPoint: Vector2D,
    val bounceNumber: Int = 1
)

data class AimCalculationResult(
    val cueBallPosition: Vector2D,
    val objectBallPosition: Vector2D,
    val shotAngleDegrees: Float,
    val aimDirection: Vector2D,
    val ghostCueBallPosition: Vector2D?,
    val directImpactOccurred: Boolean,
    val objectBallTrajectory: Vector2D?,
    val cueDeflectionTrajectory: Vector2D?,
    val cushionReflections: List<CushionReflection>,
    val nearestPocket: Pocket?,
    val pocketAlignmentAngleDiff: Float?,
    val cutAngleDegrees: Float = 0f,
    val shotSuccessProbability: Int = 0
)
