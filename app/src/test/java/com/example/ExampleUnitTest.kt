package com.example

import com.example.engine.PoolPhysicsEngine
import com.example.model.AimConfiguration
import com.example.model.Vector2D
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun `verify aim physics vector calculations and direct impact trajectory`() {
    val engine = PoolPhysicsEngine()
    val cuePos = Vector2D(100f, 200f)
    val objPos = Vector2D(300f, 200f)
    val shotAngle = 0f // Straight right towards object ball
    val config = AimConfiguration()

    val result = engine.calculateAim(
      cuePos = cuePos,
      objectPos = objPos,
      shotAngleDeg = shotAngle,
      config = config,
      boundsWidth = 1080f,
      boundsHeight = 2400f
    )

    assertTrue("Direct impact should occur", result.directImpactOccurred)
    assertNotNull("Ghost cue ball position should be calculated", result.ghostCueBallPosition)
    assertNotNull("Object ball trajectory vector should exist", result.objectBallTrajectory)
    assertEquals(1f, result.objectBallTrajectory?.x ?: 0f, 0.01f)
  }

  @Test
  fun `verify bank shot cushion reflection calculation`() {
    val engine = PoolPhysicsEngine()
    val cuePos = Vector2D(500f, 500f)
    val objPos = Vector2D(800f, 500f)
    val shotAngle = 45f
    val config = AimConfiguration(showBankShots = true)

    val result = engine.calculateAim(
      cuePos = cuePos,
      objectPos = objPos,
      shotAngleDeg = shotAngle,
      config = config,
      boundsWidth = 1080f,
      boundsHeight = 2400f
    )

    assertTrue("Cushion reflections should be calculated", result.cushionReflections.isNotEmpty())
  }
}
