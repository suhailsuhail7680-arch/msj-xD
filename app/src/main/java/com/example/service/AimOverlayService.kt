package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.data.AimPreferencesManager
import com.example.engine.PoolPhysicsEngine
import com.example.model.AimCalculationResult
import com.example.model.AimConfiguration
import com.example.model.Pocket
import com.example.model.Vector2D
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RedDanger
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Custom lifecycle owner to host Jetpack Compose in an Android WindowManager Overlay Service.
 */
class OverlayLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    init {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    fun destroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}

class AimOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var prefsManager: AimPreferencesManager
    private val physicsEngine = PoolPhysicsEngine()

    private var hudOverlayView: View? = null
    private var canvasOverlayView: View? = null

    private lateinit var hudLayoutParams: WindowManager.LayoutParams
    private lateinit var canvasLayoutParams: WindowManager.LayoutParams

    private val lifecycleOwner = OverlayLifecycleOwner()

    // Real-time overlay state
    private val cueBallPosState = MutableStateFlow(Vector2D(300f, 600f))
    private val objectBallPosState = MutableStateFlow(Vector2D(500f, 600f))
    private val shotAngleState = MutableStateFlow(0f)
    private val isMinimizedState = MutableStateFlow(false)
    private val selectedBallState = MutableStateFlow(0) // 0: Cue, 1: Object

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        prefsManager = AimPreferencesManager.getInstance(this)

        startAsForegroundService()
        initOverlayViews()
    }

    private fun startAsForegroundService() {
        val channelId = "msj_xd_aim_channel"
        val channelName = "msj xD Aim Tool Service"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Active floating aim tool running over 8 Ball Pool"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AimOverlayService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("msj xD Aim Tool Active")
            .setContentText("Aim guidance is running over your pool game. Tap to open controls.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Stop Tool", stopPendingIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun initOverlayViews() {
        val metrics = resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels

        // 1. Full-screen Vector Guideline & Ball Interaction Canvas
        canvasLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        val canvasComposeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setContent {
                MyApplicationTheme {
                    OverlayAimCanvas(
                        screenWidth = screenWidth.toFloat(),
                        screenHeight = screenHeight.toFloat(),
                        cuePosFlow = cueBallPosState,
                        objectPosFlow = objectBallPosState,
                        shotAngleFlow = shotAngleState,
                        prefsManager = prefsManager,
                        physicsEngine = physicsEngine,
                        onCueMoved = { cueBallPosState.value = it },
                        onObjectMoved = { objectBallPosState.value = it }
                    )
                }
            }
        }
        canvasOverlayView = canvasComposeView
        try {
            windowManager.addView(canvasOverlayView, canvasLayoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Floating Draggable HUD Window
        hudLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 120
        }

        val hudComposeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setContent {
                MyApplicationTheme {
                    FloatingHudControl(
                        isMinimizedFlow = isMinimizedState,
                        selectedBallFlow = selectedBallState,
                        shotAngleFlow = shotAngleState,
                        prefsManager = prefsManager,
                        onDragHUD = { dx, dy ->
                            hudLayoutParams.x = (hudLayoutParams.x + dx.toInt()).coerceAtLeast(0)
                            hudLayoutParams.y = (hudLayoutParams.y + dy.toInt()).coerceAtLeast(0)
                            windowManager.updateViewLayout(hudOverlayView, hudLayoutParams)
                        },
                        onNudge = { dx, dy ->
                            vibrateStep()
                            val step = prefsManager.getConfig().dpadStepPx
                            if (selectedBallState.value == 0) {
                                cueBallPosState.value = cueBallPosState.value + Vector2D(dx * step, dy * step)
                            } else {
                                objectBallPosState.value = objectBallPosState.value + Vector2D(dx * step, dy * step)
                            }
                        },
                        onAngleChange = { newAngle ->
                            shotAngleState.value = newAngle
                        },
                        onResetBalls = {
                            val w = screenWidth.toFloat()
                            val h = screenHeight.toFloat()
                            cueBallPosState.value = Vector2D(w * 0.35f, h * 0.5f)
                            objectBallPosState.value = Vector2D(w * 0.65f, h * 0.5f)
                            shotAngleState.value = 0f
                        },
                        onCloseTool = {
                            stopSelf()
                        }
                    )
                }
            }
        }
        hudOverlayView = hudComposeView
        try {
            windowManager.addView(hudOverlayView, hudLayoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun vibrateStep() {
        if (!prefsManager.getConfig().hapticFeedbackEnabled) return
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(18)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleOwner.destroy()
        hudOverlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        canvasOverlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_ID = 8801
        const val ACTION_STOP_SERVICE = "com.example.msjxd.STOP_OVERLAY_SERVICE"
    }
}

/**
 * Interactive full-screen canvas rendering the trajectory lines and precision ball handles
 */
@Composable
fun OverlayAimCanvas(
    screenWidth: Float,
    screenHeight: Float,
    cuePosFlow: MutableStateFlow<Vector2D>,
    objectPosFlow: MutableStateFlow<Vector2D>,
    shotAngleFlow: MutableStateFlow<Float>,
    prefsManager: AimPreferencesManager,
    physicsEngine: PoolPhysicsEngine,
    onCueMoved: (Vector2D) -> Unit,
    onObjectMoved: (Vector2D) -> Unit
) {
    val cuePos by cuePosFlow.collectAsState()
    val objectPos by objectPosFlow.collectAsState()
    val shotAngle by shotAngleFlow.collectAsState()
    val config by prefsManager.configFlow.collectAsState()

    val pockets = remember(screenWidth, screenHeight) {
        physicsEngine.createTablePockets(screenWidth, screenHeight)
    }

    val aimResult = remember(cuePos, objectPos, shotAngle, config, screenWidth, screenHeight) {
        physicsEngine.calculateAim(
            cuePos = cuePos,
            objectPos = objectPos,
            shotAngleDeg = shotAngle,
            config = config,
            boundsWidth = screenWidth,
            boundsHeight = screenHeight,
            pockets = pockets
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Draw guidelines & cushion reflections
        if (config.isGuidelineVisible) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val primaryColor = config.primaryColor.color.copy(alpha = config.opacity)
                val secondaryColor = config.secondaryColor.color.copy(alpha = config.opacity)
                val strokeWidth = config.lineThickness.dp.toPx()
                val ballRadius = config.ballRadius.dp.toPx()

                // Draw table pocket indicators
                if (config.showPocketGuides) {
                    pockets.forEach { pocket ->
                        val isTarget = aimResult.nearestPocket?.id == pocket.id
                        drawCircle(
                            color = if (isTarget) GoldPrimary.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.25f),
                            radius = if (isTarget) 26.dp.toPx() else 18.dp.toPx(),
                            center = Offset(pocket.position.x, pocket.position.y),
                            style = Stroke(
                                width = if (isTarget) 3.5.dp.toPx() else 1.5.dp.toPx(),
                                pathEffect = if (isTarget) null else PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                            )
                        )
                    }
                }

                // 1. Direct Cue Aim Line
                val cueDir = aimResult.aimDirection
                val cueLineEnd = if (aimResult.directImpactOccurred && aimResult.ghostCueBallPosition != null) {
                    Offset(aimResult.ghostCueBallPosition.x, aimResult.ghostCueBallPosition.y)
                } else {
                    Offset(cuePos.x + cueDir.x * config.lineLength, cuePos.y + cueDir.y * config.lineLength)
                }

                drawLine(
                    color = primaryColor,
                    start = Offset(cuePos.x, cuePos.y),
                    end = cueLineEnd,
                    strokeWidth = strokeWidth
                )

                // 2. Ghost Ball at impact point
                if (aimResult.directImpactOccurred && aimResult.ghostCueBallPosition != null) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.45f),
                        radius = ballRadius,
                        center = Offset(aimResult.ghostCueBallPosition.x, aimResult.ghostCueBallPosition.y),
                        style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
                    )

                    // 3. Object ball trajectory line toward pocket
                    aimResult.objectBallTrajectory?.let { objDir ->
                        val objLineEnd = Offset(
                            objectPos.x + objDir.x * config.lineLength,
                            objectPos.y + objDir.y * config.lineLength
                        )
                        drawLine(
                            color = secondaryColor,
                            start = Offset(objectPos.x, objectPos.y),
                            end = objLineEnd,
                            strokeWidth = strokeWidth
                        )
                    }

                    // 4. Cue deflection tangent line
                    aimResult.cueDeflectionTrajectory?.let { cueDef ->
                        val cueDefEnd = Offset(
                            aimResult.ghostCueBallPosition.x + cueDef.x * 240f,
                            aimResult.ghostCueBallPosition.y + cueDef.y * 240f
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.7f),
                            start = Offset(aimResult.ghostCueBallPosition.x, aimResult.ghostCueBallPosition.y),
                            end = cueDefEnd,
                            strokeWidth = (strokeWidth * 0.75f).coerceAtLeast(1.5f),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )
                    }
                }

                // 5. Cushion bank reflections
                if (config.showBankShots && aimResult.cushionReflections.isNotEmpty()) {
                    aimResult.cushionReflections.forEach { bounce ->
                        drawLine(
                            color = GoldSecondary.copy(alpha = 0.8f),
                            start = Offset(bounce.hitPoint.x, bounce.hitPoint.y),
                            end = Offset(bounce.endPoint.x, bounce.endPoint.y),
                            strokeWidth = (strokeWidth * 0.85f).coerceAtLeast(2f)
                        )
                        drawCircle(
                            color = GoldPrimary,
                            radius = 6.dp.toPx(),
                            center = Offset(bounce.hitPoint.x, bounce.hitPoint.y)
                        )
                    }
                }
            }
        }

        // Draggable Cue Ball Handle
        Box(
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.TopStart)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onCueMoved(cuePos + Vector2D(dragAmount.x, dragAmount.y))
                    }
                }
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ball graphic positioned at cuePos
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerOffset = Offset(cuePos.x, cuePos.y)
                drawCircle(
                    color = Color.White,
                    radius = config.ballRadius.dp.toPx(),
                    center = centerOffset
                )
                drawCircle(
                    color = RedDanger,
                    radius = 5.dp.toPx(),
                    center = centerOffset
                )
                drawCircle(
                    color = DarkBorder,
                    radius = config.ballRadius.dp.toPx(),
                    center = centerOffset,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Draggable Object Ball Handle
        Box(
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.TopStart)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onObjectMoved(objectPos + Vector2D(dragAmount.x, dragAmount.y))
                    }
                }
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerOffset = Offset(objectPos.x, objectPos.y)
                drawCircle(
                    color = GoldPrimary,
                    radius = config.ballRadius.dp.toPx(),
                    center = centerOffset
                )
                drawCircle(
                    color = Color.Black,
                    radius = 9.dp.toPx(),
                    center = centerOffset
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = centerOffset
                )
            }
        }
    }
}

/**
 * Floating draggable HUD controller window
 */
@Composable
fun FloatingHudControl(
    isMinimizedFlow: MutableStateFlow<Boolean>,
    selectedBallFlow: MutableStateFlow<Int>,
    shotAngleFlow: MutableStateFlow<Float>,
    prefsManager: AimPreferencesManager,
    onDragHUD: (Float, Float) -> Unit,
    onNudge: (Float, Float) -> Unit,
    onAngleChange: (Float) -> Unit,
    onResetBalls: () -> Unit,
    onCloseTool: () -> Unit
) {
    val isMinimized by isMinimizedFlow.collectAsState()
    val selectedBall by selectedBallFlow.collectAsState()
    val shotAngle by shotAngleFlow.collectAsState()
    val config by prefsManager.configFlow.collectAsState()

    Surface(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDragHUD(dragAmount.x, dragAmount.y)
                }
            }
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, GoldPrimary.copy(alpha = 0.7f), RoundedCornerShape(16.dp)),
        color = DarkSurface.copy(alpha = 0.94f),
        shadowElevation = 8.dp
    ) {
        if (isMinimized) {
            // Minimized Floating Pill Bubble
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .clickable { isMinimizedFlow.value = false },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary)
                )
                Text(
                    text = "msj xD",
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        } else {
            // Expanded Aim HUD
            Column(
                modifier = Modifier
                    .width(260.dp)
                    .padding(12.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(GreenSuccess)
                        )
                        Text(
                            text = "msj xD AIM HUD",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                prefsManager.updateConfig { it.copy(isGuidelineVisible = !it.isGuidelineVisible) }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (config.isGuidelineVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle lines",
                                tint = if (config.isGuidelineVisible) GoldPrimary else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { isMinimizedFlow.value = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Minimize HUD",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onCloseTool,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close overlay",
                                tint = RedDanger,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Ball Selector: Cue Ball vs Object Ball
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedBall == 0) GoldPrimary else Color.Transparent)
                            .clickable { selectedBallFlow.value = 0 }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cue Ball",
                            color = if (selectedBall == 0) DarkBackground else TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedBall == 1) GoldPrimary else Color.Transparent)
                            .clickable { selectedBallFlow.value = 1 }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Object Ball",
                            color = if (selectedBall == 1) DarkBackground else TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Precision D-Pad Controls for 1px / 5px micro-adjustments
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // D-Pad Up
                    IconButton(
                        onClick = { onNudge(0f, -1f) },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Up",
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // D-Pad Left
                        IconButton(
                            onClick = { onNudge(-1f, 0f) },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Left",
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Center Reset
                        IconButton(
                            onClick = onResetBalls,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset positions",
                                tint = CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // D-Pad Right
                        IconButton(
                            onClick = { onNudge(1f, 0f) },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Right",
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // D-Pad Down
                    IconButton(
                        onClick = { onNudge(0f, 1f) },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Down",
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Shot Angle Fine-Tuning Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Aim Angle",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${shotAngle.toInt()}°",
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Slider(
                    value = shotAngle,
                    onValueChange = onAngleChange,
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = GoldPrimary,
                        activeTrackColor = GoldPrimary,
                        inactiveTrackColor = DarkBorder
                    ),
                    modifier = Modifier.height(30.dp)
                )

                // Quick Angle Steppers: -1°, +1°, -5°, +5°
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(-5f, -1f, 1f, 5f).forEach { step ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DarkSurfaceVariant)
                                .clickable {
                                    val next = (shotAngle + step + 360f) % 360f
                                    onAngleChange(next)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (step > 0) "+${step.toInt()}°" else "${step.toInt()}°",
                                color = GoldPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
