package com.example.engine

import com.example.model.AimCalculationResult
import com.example.model.AimConfiguration
import com.example.model.CushionReflection
import com.example.model.Pocket
import com.example.model.Vector2D
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

class PoolPhysicsEngine {

    /**
     * Calculates the aiming trajectory for 8 Ball Pool.
     * Computes the line from Cue Ball along the shot angle, checks for collision with the Object Ball,
     * calculates the ghost cue ball center, the resulting object ball direction, cue deflection tangent line,
     * and cushion bank rebounds.
     */
    fun calculateAim(
        cuePos: Vector2D,
        objectPos: Vector2D,
        shotAngleDeg: Float,
        config: AimConfiguration,
        boundsWidth: Float,
        boundsHeight: Float,
        pockets: List<Pocket> = emptyList()
    ): AimCalculationResult {
        val dir = Vector2D.fromAngle(shotAngleDeg).normalized()
        val ballRadius = config.ballRadius
        val collisionDistance = ballRadius * 2f // Center-to-center distance at impact

        // Raycast from cuePos along dir to test collision with sphere around objectPos with radius collisionDistance
        val d = cuePos.distanceTo(objectPos)
        val toObj = objectPos - cuePos
        val proj = toObj.dot(dir)

        var ghostCuePos: Vector2D? = null
        var directImpact = false
        var objTrajectory: Vector2D? = null
        var cueDeflection: Vector2D? = null

        if (proj > 0f) {
            // Distance squared from object center to ray
            val perpDistSq = toObj.dot(toObj) - proj * proj
            val rSq = collisionDistance * collisionDistance
            if (perpDistSq <= rSq) {
                // Collision occurs!
                val halfChord = sqrt(rSq - perpDistSq)
                val hitDist = proj - halfChord
                if (hitDist > 0f) {
                    directImpact = true
                    ghostCuePos = cuePos + (dir * hitDist)

                    // Line of centers at impact: from ghost cue to object ball
                    val impactNormal = (objectPos - ghostCuePos).normalized()
                    objTrajectory = impactNormal

                    // Tangent vector perpendicular to normal (cue ball deflection direction)
                    val tangent = Vector2D(-impactNormal.y, impactNormal.x)
                    val cueDirDotTangent = dir.dot(tangent)
                    cueDeflection = (tangent * cueDirDotTangent).normalized()
                }
            }
        }

        // Cushion Reflections calculation
        val reflections = mutableListOf<CushionReflection>()
        if (config.showBankShots) {
            val startPoint = if (directImpact && ghostCuePos != null) objectPos else cuePos
            val trajectoryDir = if (directImpact && objTrajectory != null) objTrajectory else dir

            calculateCushionBounces(
                startPoint = startPoint,
                direction = trajectoryDir,
                maxBounces = 2,
                boundsWidth = boundsWidth,
                boundsHeight = boundsHeight,
                padding = ballRadius,
                reflections = reflections
            )
        }

        // Check nearest pocket to object ball trajectory
        var nearestPocket: Pocket? = null
        var pocketAngleDiff: Float? = null

        if (pockets.isNotEmpty()) {
            val activeDir = objTrajectory ?: dir
            var minDiff = Float.MAX_VALUE

            for (p in pockets) {
                val toPocket = (p.position - objectPos).normalized()
                val dot = activeDir.dot(toPocket).coerceIn(-1f, 1f)
                val angleDiff = Math.toDegrees(acos(dot.toDouble())).toFloat()
                if (angleDiff < minDiff && angleDiff <= 65f) {
                    minDiff = angleDiff
                    nearestPocket = p
                    pocketAngleDiff = angleDiff
                }
            }
        }

        return AimCalculationResult(
            cueBallPosition = cuePos,
            objectBallPosition = objectPos,
            shotAngleDegrees = shotAngleDeg,
            aimDirection = dir,
            ghostCueBallPosition = ghostCuePos,
            directImpactOccurred = directImpact,
            objectBallTrajectory = objTrajectory,
            cueDeflectionTrajectory = cueDeflection,
            cushionReflections = reflections,
            nearestPocket = nearestPocket,
            pocketAlignmentAngleDiff = pocketAngleDiff
        )
    }

    private fun calculateCushionBounces(
        startPoint: Vector2D,
        direction: Vector2D,
        maxBounces: Int,
        boundsWidth: Float,
        boundsHeight: Float,
        padding: Float,
        reflections: MutableList<CushionReflection>
    ) {
        val minX = padding
        val maxX = (boundsWidth - padding).coerceAtLeast(minX + 10f)
        val minY = padding
        val maxY = (boundsHeight - padding).coerceAtLeast(minY + 10f)

        var currPoint = startPoint
        var currDir = direction.normalized()

        for (bounce in 0 until maxBounces) {
            if (currDir.length() < 0.01f) break

            // Ray vs 4 boundary segments
            var tHit = Float.MAX_VALUE
            var hitNormal = Vector2D.ZERO

            // Vertical boundaries (Left / Right)
            if (currDir.x > 0f) {
                val t = (maxX - currPoint.x) / currDir.x
                if (t > 1f && t < tHit) {
                    tHit = t
                    hitNormal = Vector2D(-1f, 0f)
                }
            } else if (currDir.x < 0f) {
                val t = (minX - currPoint.x) / currDir.x
                if (t > 1f && t < tHit) {
                    tHit = t
                    hitNormal = Vector2D(1f, 0f)
                }
            }

            // Horizontal boundaries (Top / Bottom)
            if (currDir.y > 0f) {
                val t = (maxY - currPoint.y) / currDir.y
                if (t > 1f && t < tHit) {
                    tHit = t
                    hitNormal = Vector2D(0f, -1f)
                }
            } else if (currDir.y < 0f) {
                val t = (minY - currPoint.y) / currDir.y
                if (t > 1f && t < tHit) {
                    tHit = t
                    hitNormal = Vector2D(0f, 1f)
                }
            }

            if (tHit == Float.MAX_VALUE || tHit <= 0f) break

            val hitPoint = currPoint + (currDir * tHit)
            // Reflect: R = D - 2*(D·N)*N
            val dot = currDir.dot(hitNormal)
            val bounceDir = (currDir - (hitNormal * (2f * dot))).normalized()
            val endPoint = hitPoint + (bounceDir * 300f)

            reflections.add(
                CushionReflection(
                    hitPoint = hitPoint,
                    bounceVector = bounceDir,
                    endPoint = endPoint
                )
            )

            currPoint = hitPoint
            currDir = bounceDir
        }
    }

    /**
     * Standard 8-ball pool 6-pocket layout proportional to table width and height.
     */
    fun createTablePockets(width: Float, height: Float, margin: Float = 30f): List<Pocket> {
        val left = margin
        val right = width - margin
        val top = margin
        val bottom = height - margin
        val midX = width / 2f
        val midY = height / 2f

        // When in landscape or portrait, generate the 6 pockets along the perimeter
        return if (width >= height) {
            // Landscape orientation: 4 corners + 2 center side cushions (top and bottom)
            listOf(
                Pocket("top_left", Vector2D(left, top)),
                Pocket("top_mid", Vector2D(midX, top)),
                Pocket("top_right", Vector2D(right, top)),
                Pocket("bottom_left", Vector2D(left, bottom)),
                Pocket("bottom_mid", Vector2D(midX, bottom)),
                Pocket("bottom_right", Vector2D(right, bottom))
            )
        } else {
            // Portrait orientation: 4 corners + 2 center side cushions (left and right)
            listOf(
                Pocket("top_left", Vector2D(left, top)),
                Pocket("top_right", Vector2D(right, top)),
                Pocket("mid_left", Vector2D(left, midY)),
                Pocket("mid_right", Vector2D(right, midY)),
                Pocket("bottom_left", Vector2D(left, bottom)),
                Pocket("bottom_right", Vector2D(right, bottom))
            )
        }
    }
}
