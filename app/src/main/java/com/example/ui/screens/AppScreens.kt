package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AimPreferencesManager
import com.example.engine.PoolPhysicsEngine
import com.example.model.AimConfiguration
import com.example.model.GuidelineColor
import com.example.model.Vector2D
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.OnGold
import com.example.ui.theme.RedDanger
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

enum class Screen {
    SPLASH,
    HOME,
    AIM_SIMULATOR,
    AIM_CALCULATOR,
    BANK_SHOT_MASTER,
    FLOATING_SETTINGS,
    PERMISSIONS_SETUP,
    SETTINGS,
    ABOUT
}

// -----------------------------------------------------------------------------------------
// 1. SPLASH SCREEN
// -----------------------------------------------------------------------------------------
@Composable
fun SplashScreen(onNavigateHome: () -> Unit) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1500)
        onNavigateHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(GoldPrimary, DarkSurfaceVariant, DarkBackground)
                        )
                    )
                    .border(2.dp, GoldPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "8",
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "msj xD",
                color = GoldPrimary,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            Text(
                text = "AIM TOOL - BLORO 8 POOL EDITION",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .width(140.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = GoldPrimary,
                trackColor = DarkBorder
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// 2. PROFESSIONAL HOME SCREEN (With START PLAYING Hero Button)
// -----------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isServiceActive: Boolean,
    onStartPlaying: () -> Unit,
    onToggleService: (Boolean) -> Unit,
    onNavigate: (Screen) -> Unit
) {
    val context = LocalContext.current
    val hasOverlayPermission = remember { Settings.canDrawOverlays(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (isServiceActive) GreenSuccess else RedDanger)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "msj xD AIM TOOL",
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigate(Screen.SETTINGS) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                // Prominent "START PLAYING" Hero Card (Bloro 8 Pool Style)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(2.dp, GoldPrimary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isServiceActive) GreenSuccess else GoldPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isServiceActive) "ACTIVE IN 8 BALL POOL" else "READY TO LAUNCH",
                                    color = if (isServiceActive) GreenSuccess else GoldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GoldPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "2-BANK & 4-BANK",
                                    color = GoldPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Play 8 Ball Pool with Pro Aim",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Text(
                            text = "Screen capture MediaProjection & floating guidelines over 8 Ball Pool. Supports 2-Bank, 4-Bank, and Extended Lines.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
                        )

                        // Big START PLAYING / STOP Button
                        Button(
                            onClick = {
                                if (isServiceActive) {
                                    onToggleService(false)
                                } else {
                                    onStartPlaying()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isServiceActive) RedDanger else GoldPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isServiceActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isServiceActive) Color.White else OnGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isServiceActive) "STOP AIM ASSISTANT" else "START PLAYING",
                                color = if (isServiceActive) Color.White else OnGold,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // Quick Permission Status Alert if denied
            if (!hasOverlayPermission) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(Screen.PERMISSIONS_SETUP) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF261D10)),
                        border = BorderStroke(1.dp, GoldSecondary)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = GoldSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Android 16 Permission Setup",
                                    color = GoldSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Tap to grant 'Display over other apps' on your Infinix Note 50s.",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // AIM SECTION
            item {
                Text(
                    text = "AIM SECTION",
                    color = GoldPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                FeatureMenuCard(
                    icon = Icons.Default.Adjust,
                    title = "Realistic Table Simulator",
                    subtitle = "Interactive 8 Ball Pool table with drag handles, pocket prediction, and ghost ball contact.",
                    badge = "SMART AIM",
                    onClick = { onNavigate(Screen.AIM_SIMULATOR) }
                )
            }

            item {
                FeatureMenuCard(
                    icon = Icons.Default.Layers,
                    title = "Extended Line & Visuals",
                    subtitle = "Toggle extended guidelines, line thickness, neon colors, and pocket markers.",
                    badge = "EXTENDED LINE",
                    onClick = { onNavigate(Screen.FLOATING_SETTINGS) }
                )
            }

            // CALCULATE SECTION
            item {
                Text(
                    text = "CALCULATE SECTION",
                    color = CyanAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                FeatureMenuCard(
                    icon = Icons.Default.Calculate,
                    title = "Precision Angle Calculator",
                    subtitle = "Measure cut angles, ball impact deflection, shot direction, and pot probability.",
                    badge = "CALCULATOR",
                    onClick = { onNavigate(Screen.AIM_CALCULATOR) }
                )
            }

            // MASTER SECTION
            item {
                Text(
                    text = "MASTER SECTION",
                    color = GoldSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                FeatureMenuCard(
                    icon = Icons.Default.Speed,
                    title = "2-Bank & 4-Bank Shot Master",
                    subtitle = "Calculate table cushion rebounds with geometric angles for advanced trick shots.",
                    badge = "4-BANK SHOT",
                    onClick = { onNavigate(Screen.BANK_SHOT_MASTER) }
                )
            }

            item {
                FeatureMenuCard(
                    icon = Icons.Default.Security,
                    title = "Infinix Note 50s Setup",
                    subtitle = "Android 16 permissions, background battery whitelist, and notification settings.",
                    badge = "ANDROID 16",
                    onClick = { onNavigate(Screen.PERMISSIONS_SETUP) }
                )
            }

            item {
                FeatureMenuCard(
                    icon = Icons.Default.Info,
                    title = "About msj xD",
                    subtitle = "100% Free, unlimited, offline-capable 8 Ball Pool assistant. No paywalls.",
                    badge = "FREE",
                    onClick = { onNavigate(Screen.ABOUT) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun FeatureMenuCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    badge: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GoldPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 3. SMART AIM SYSTEM (REALISTIC 8 BALL POOL TABLE SIMULATOR)
// -----------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AimSimulatorScreen(
    prefsManager: AimPreferencesManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val config by prefsManager.configFlow.collectAsState()
    val physicsEngine = remember { PoolPhysicsEngine() }

    var cuePos by remember { mutableStateOf(Vector2D(250f, 400f)) }
    var objPos by remember { mutableStateOf(Vector2D(500f, 400f)) }
    var shotAngle by remember { mutableFloatStateOf(0f) }
    var selectedBall by remember { mutableStateOf(0) } // 0: Cue, 1: Object

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Smart Aim Table",
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        cuePos = Vector2D(250f, 400f)
                        objPos = Vector2D(500f, 400f)
                        shotAngle = 0f
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset table",
                            tint = GoldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive Pool Table Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(14.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0F201B)) // Realistic felt
                    .border(5.dp, Color(0xFF382315), RoundedCornerShape(18.dp))
            ) {
                androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val width = constraints.maxWidth.toFloat()
                    val height = constraints.maxHeight.toFloat()

                    val pockets = remember(width, height) {
                        physicsEngine.createTablePockets(width, height, margin = 20f)
                    }

                    val aimResult = remember(cuePos, objPos, shotAngle, config, width, height) {
                        physicsEngine.calculateAim(
                            cuePos = cuePos,
                            objectPos = objPos,
                            shotAngleDeg = shotAngle,
                            config = config,
                            boundsWidth = width,
                            boundsHeight = height,
                            pockets = pockets
                        )
                    }

                    // Drawing guidelines, balls, and pockets
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(selectedBall) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    if (selectedBall == 0) {
                                        cuePos = Vector2D(
                                            (cuePos.x + dragAmount.x).coerceIn(30f, width - 30f),
                                            (cuePos.y + dragAmount.y).coerceIn(30f, height - 30f)
                                        )
                                    } else {
                                        objPos = Vector2D(
                                            (objPos.x + dragAmount.x).coerceIn(30f, width - 30f),
                                            (objPos.y + dragAmount.y).coerceIn(30f, height - 30f)
                                        )
                                    }
                                }
                            }
                    ) {
                        // 1. Draw 6 pockets
                        pockets.forEach { pocket ->
                            val isTarget = aimResult.nearestPocket?.id == pocket.id
                            drawCircle(
                                color = Color.Black,
                                radius = 22.dp.toPx(),
                                center = Offset(pocket.position.x, pocket.position.y)
                            )
                            drawCircle(
                                color = if (isTarget) GoldPrimary else DarkBorder,
                                radius = 22.dp.toPx(),
                                center = Offset(pocket.position.x, pocket.position.y),
                                style = Stroke(width = if (isTarget) 3.5.dp.toPx() else 1.dp.toPx())
                            )
                        }

                        val ballRadius = config.ballRadius.dp.toPx()
                        val primaryColor = config.primaryColor.color
                        val secondaryColor = config.secondaryColor.color
                        val effectiveLength = if (config.showExtendedLines) config.lineLength * 1.6f else config.lineLength

                        // 2. Direct Cue Trajectory Line
                        val cueDir = aimResult.aimDirection
                        val cueEnd = if (aimResult.directImpactOccurred && aimResult.ghostCueBallPosition != null) {
                            Offset(aimResult.ghostCueBallPosition.x, aimResult.ghostCueBallPosition.y)
                        } else {
                            Offset(cuePos.x + cueDir.x * effectiveLength, cuePos.y + cueDir.y * effectiveLength)
                        }
                        drawLine(
                            color = primaryColor,
                            start = Offset(cuePos.x, cuePos.y),
                            end = cueEnd,
                            strokeWidth = config.lineThickness.dp.toPx()
                        )

                        // 3. Ghost Ball and Object Trajectory
                        if (aimResult.directImpactOccurred && aimResult.ghostCueBallPosition != null) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.45f),
                                radius = ballRadius,
                                center = Offset(aimResult.ghostCueBallPosition.x, aimResult.ghostCueBallPosition.y),
                                style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
                            )

                            aimResult.objectBallTrajectory?.let { objDir ->
                                val objEnd = Offset(
                                    objPos.x + objDir.x * effectiveLength,
                                    objPos.y + objDir.y * effectiveLength
                                )
                                drawLine(
                                    color = secondaryColor,
                                    start = Offset(objPos.x, objPos.y),
                                    end = objEnd,
                                    strokeWidth = config.lineThickness.dp.toPx()
                                )
                            }
                        }

                        // 4. Cushion Bank Reflections (supports up to 4-bank)
                        if (config.showBankShots) {
                            aimResult.cushionReflections.forEach { bounce ->
                                val bounceColor = when (bounce.bounceNumber) {
                                    1 -> GoldPrimary
                                    2 -> GoldSecondary
                                    3 -> CyanAccent
                                    else -> GreenSuccess
                                }
                                drawLine(
                                    color = bounceColor,
                                    start = Offset(bounce.hitPoint.x, bounce.hitPoint.y),
                                    end = Offset(bounce.endPoint.x, bounce.endPoint.y),
                                    strokeWidth = (config.lineThickness * 0.85f).dp.toPx()
                                )
                                drawCircle(
                                    color = bounceColor,
                                    radius = 6.dp.toPx(),
                                    center = Offset(bounce.hitPoint.x, bounce.hitPoint.y)
                                )
                            }
                        }

                        // 5. Draw Cue Ball
                        drawCircle(color = Color.White, radius = ballRadius, center = Offset(cuePos.x, cuePos.y))
                        drawCircle(color = RedDanger, radius = 4.dp.toPx(), center = Offset(cuePos.x, cuePos.y))

                        // 6. Draw Object Ball (8-Ball Style)
                        drawCircle(color = Color(0xFF1E212B), radius = ballRadius, center = Offset(objPos.x, objPos.y))
                        drawCircle(color = Color.White, radius = ballRadius * 0.45f, center = Offset(objPos.x, objPos.y))
                    }

                    // On-screen telemetry banner
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground.copy(alpha = 0.85f))
                            .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Angle: ${shotAngle.toInt()}°",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Cut: ${aimResult.cutAngleDegrees.roundToInt()}°",
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Pot: ${aimResult.shotSuccessProbability}%",
                            color = if (aimResult.shotSuccessProbability > 60) GreenSuccess else GoldSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Bottom Simulator Controls
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Ball Switcher
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
                                .clickable { selectedBall = 0 }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Move Cue Ball (White)",
                                color = if (selectedBall == 0) DarkBackground else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedBall == 1) GoldPrimary else Color.Transparent)
                                .clickable { selectedBall = 1 }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Move Object Ball (Black)",
                                color = if (selectedBall == 1) DarkBackground else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Angle Slider & Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Shot Aim Direction", color = TextSecondary, fontSize = 12.sp)
                        Text(text = "${shotAngle.toInt()}°", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Slider(
                        value = shotAngle,
                        onValueChange = { shotAngle = it },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldPrimary,
                            activeTrackColor = GoldPrimary,
                            inactiveTrackColor = DarkBorder
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(-10f, -1f, 1f, 10f).forEach { step ->
                            Button(
                                onClick = { shotAngle = (shotAngle + step + 360f) % 360f },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).padding(horizontal = 3.dp)
                            ) {
                                Text(
                                    text = if (step > 0) "+${step.toInt()}°" else "${step.toInt()}°",
                                    color = GoldPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 4. AIM CALCULATOR SCREEN
// -----------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AimCalculatorScreen(
    prefsManager: AimPreferencesManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val config by prefsManager.configFlow.collectAsState()
    val physicsEngine = remember { PoolPhysicsEngine() }

    var cueX by remember { mutableFloatStateOf(200f) }
    var cueY by remember { mutableFloatStateOf(400f) }
    var objX by remember { mutableFloatStateOf(450f) }
    var objY by remember { mutableFloatStateOf(350f) }
    var testAngle by remember { mutableFloatStateOf(0f) }

    val pockets = remember { physicsEngine.createTablePockets(800f, 600f) }
    val aimResult = remember(cueX, cueY, objX, objY, testAngle, config) {
        physicsEngine.calculateAim(
            cuePos = Vector2D(cueX, cueY),
            objectPos = Vector2D(objX, objY),
            shotAngleDeg = testAngle,
            config = config,
            boundsWidth = 800f,
            boundsHeight = 600f,
            pockets = pockets
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Aim Angle Calculator", fontWeight = FontWeight.Bold, color = GoldPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        cueX = 200f; cueY = 400f; objX = 450f; objY = 350f; testAngle = 0f
                    }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset values", tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, CyanAccent)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "SHOT METRICS & PREDICTION", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            MetricBox(title = "Cut Angle", value = "${aimResult.cutAngleDegrees.roundToInt()}°", color = GoldPrimary)
                            MetricBox(title = "Aim Direction", value = "${testAngle.roundToInt()}°", color = TextPrimary)
                            MetricBox(title = "Pot Chance", value = "${aimResult.shotSuccessProbability}%", color = GreenSuccess)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (aimResult.directImpactOccurred) {
                                "Collision detected! Ghost ball placed at (${aimResult.ghostCueBallPosition?.x?.roundToInt()}, ${aimResult.ghostCueBallPosition?.y?.roundToInt()}). Object ball deflects toward ${aimResult.nearestPocket?.id ?: "rail"}."
                            } else {
                                "Adjust the aim angle slider to target the object ball."
                            },
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Adjust Shot Direction Angle", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "${testAngle.toInt()}°", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Slider(
                            value = testAngle,
                            onValueChange = { testAngle = it },
                            valueRange = 0f..360f,
                            colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldPrimary, inactiveTrackColor = DarkBorder)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0f, 45f, 90f, 180f, 270f).forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkSurfaceVariant)
                                        .clickable { testAngle = preset }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "${preset.toInt()}°", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Ball Distance & Separation", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        val dist = Vector2D(cueX, cueY).distanceTo(Vector2D(objX, objY)).roundToInt()
                        Text(text = "Current Ball Distance: $dist px", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { objX = (objX + 20f).coerceAtMost(700f) },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Separate Balls", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { objX = (objX - 20f).coerceAtLeast(cueX + 80f) },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Bring Closer", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun MetricBox(title: String, value: String, color: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceVariant)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, color = TextSecondary, fontSize = 11.sp)
        Text(text = value, color = color, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 2.dp))
    }
}

// -----------------------------------------------------------------------------------------
// 5. BANK SHOT MASTER SCREEN (2-BANK & 4-BANK SYSTEM)
// -----------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankShotMasterScreen(
    prefsManager: AimPreferencesManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val config by prefsManager.configFlow.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Bank Shot Master", fontWeight = FontWeight.Bold, color = GoldPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "CUSHION REFLECTION GEOMETRY", color = GoldSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text(
                    text = "Bank shots use reflection physics (Angle of Incidence = Angle of Reflection) to project cushion rebounds across the table rails.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Bank Bounce Selector (Direct, 2-Bank, 4-Bank)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Aim Bank Modes", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Select 2-Bank or 4-Bank cushion reflection depth", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                1 to "Direct",
                                2 to "2-Bank",
                                4 to "4-Bank"
                            ).forEach { (bounces, label) ->
                                val isSelected = config.maxBankBounces == bounces
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) GoldPrimary else DarkSurfaceVariant)
                                        .clickable {
                                            prefsManager.updateConfig { it.copy(maxBankBounces = bounces, showBankShots = true) }
                                        }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) DarkBackground else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                SettingToggleItem(
                    title = "Enable Cushion Bank Lines",
                    subtitle = "Draw bounce lines when hitting cushions in overlay and simulator",
                    checked = config.showBankShots,
                    onCheckedChange = { chk ->
                        prefsManager.updateConfig { it.copy(showBankShots = chk) }
                    }
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Pro Tip for Bank Shots:", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• 2-Bank: Table corner double rail bounce for tight snookers.\n" +
                                    "• 4-Bank: Full diamond-system quad rail escape shots across the table.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 6. FLOATING TOOL & EXTENDED LINE SETTINGS SCREEN
// -----------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingSettingsScreen(
    prefsManager: AimPreferencesManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val config by prefsManager.configFlow.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Guideline & Extended Line", fontWeight = FontWeight.Bold, color = GoldPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "EXTENDED AIMING GUIDELINES", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            item {
                SettingToggleItem(
                    title = "Extended Line Mode",
                    subtitle = "Project full table length aiming trajectory without cutoff",
                    checked = config.showExtendedLines,
                    onCheckedChange = { checked ->
                        prefsManager.updateConfig { it.copy(showExtendedLines = checked) }
                    }
                )
            }

            // Primary Color Picker
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Cue Shot Guideline Color", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Color of the direct line from cue ball", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            GuidelineColor.values().forEach { gc ->
                                val isSelected = config.primaryColor == gc
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(gc.color)
                                        .border(width = if (isSelected) 3.dp else 1.dp, color = if (isSelected) Color.White else Color.Transparent, shape = CircleShape)
                                        .clickable { prefsManager.updateConfig { it.copy(primaryColor = gc) } },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Secondary Color Picker
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Object Ball Trajectory Color", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Color of the object ball path toward pocket", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            GuidelineColor.values().forEach { gc ->
                                val isSelected = config.secondaryColor == gc
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(gc.color)
                                        .border(width = if (isSelected) 3.dp else 1.dp, color = if (isSelected) Color.White else Color.Transparent, shape = CircleShape)
                                        .clickable { prefsManager.updateConfig { it.copy(secondaryColor = gc) } },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Line Thickness Slider
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Line Thickness", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "${config.lineThickness.toInt()} dp", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Slider(
                            value = config.lineThickness,
                            onValueChange = { thick -> prefsManager.updateConfig { it.copy(lineThickness = thick) } },
                            valueRange = 2f..10f,
                            colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldPrimary, inactiveTrackColor = DarkBorder)
                        )
                    }
                }
            }

            // Line Length Slider
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Guideline Length", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "${config.lineLength.toInt()} px", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Slider(
                            value = config.lineLength,
                            onValueChange = { len -> prefsManager.updateConfig { it.copy(lineLength = len) } },
                            valueRange = 600f..3000f,
                            colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldPrimary, inactiveTrackColor = DarkBorder)
                        )
                    }
                }
            }

            item {
                SettingToggleItem(
                    title = "Pocket Direction Guides",
                    subtitle = "Highlight nearest pocket and aim angle divergence",
                    checked = config.showPocketGuides,
                    onCheckedChange = { checked -> prefsManager.updateConfig { it.copy(showPocketGuides = checked) } }
                )
            }

            item {
                SettingToggleItem(
                    title = "Haptic Vibration Feedback",
                    subtitle = "Vibrate softly when tapping D-Pad micro-adjustments",
                    checked = config.hapticFeedbackEnabled,
                    onCheckedChange = { checked -> prefsManager.updateConfig { it.copy(hapticFeedbackEnabled = checked) } }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SettingToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = subtitle, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = OnGold,
                    checkedTrackColor = GoldPrimary,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = DarkSurfaceVariant
                )
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// 7. PERMISSIONS & SETUP SCREEN (ANDROID 16 / INFINIX NOTE 50s COMPLIANT)
// -----------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsSetupScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var overlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    LaunchedEffect(Unit) {
        overlayGranted = Settings.canDrawOverlays(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Android 16 Setup", fontWeight = FontWeight.Bold, color = GoldPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "TARGET DEVICE: INFINIX NOTE 50s (ANDROID 16 / SDK 36)", color = GoldSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text(
                    text = "Android 16 requires system authorizations to draw aim overlays over 8 Ball Pool without interruption:",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                PermissionCard(
                    title = "Display Over Other Apps",
                    description = "Mandatory for rendering the floating aim HUD and 4-bank trajectory lines over the pool game.",
                    isGranted = overlayGranted,
                    actionText = if (overlayGranted) "GRANTED" else "GRANT OVERLAY PERMISSION",
                    onAction = {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                        context.startActivity(intent)
                    }
                )
            }

            item {
                PermissionCard(
                    title = "Foreground Service Notifications",
                    description = "Required on Android 16 to keep the aim tool active with quick-stop actions in the drawer.",
                    isGranted = true,
                    actionText = "SYSTEM CONFIGURED",
                    onAction = {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                )
            }

            item {
                PermissionCard(
                    title = "Background Battery Exemption",
                    description = "Prevents Infinix Note 50s battery management from closing the overlay service mid-game.",
                    isGranted = true,
                    actionText = "OPEN BATTERY SETTINGS",
                    onAction = {
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val appDetails = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(appDetails)
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    actionText: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, if (isGranted) GreenSuccess.copy(alpha = 0.5f) else GoldPrimary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isGranted) GreenSuccess.copy(alpha = 0.15f) else GoldPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = if (isGranted) "READY" else "ACTION REQUIRED", color = if (isGranted) GreenSuccess else GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }

            Text(text = description, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp, bottom = 12.dp))

            Button(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = if (isGranted) DarkSurfaceVariant else GoldPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = actionText, color = if (isGranted) TextSecondary else OnGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 8. SETTINGS SCREEN
// -----------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefsManager: AimPreferencesManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val config by prefsManager.configFlow.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "App Settings", fontWeight = FontWeight.Bold, color = GoldPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "CONTROLLER PRECISION & STEPPING", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            // D-Pad Step Size Selector (1px, 2px, 5px, 10px)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "D-Pad Micro-Step Precision", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Amount of pixels nudged per click on the HUD directional arrows", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1f, 2f, 5f, 10f).forEach { step ->
                                val isSelected = config.dpadStepPx == step
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) GoldPrimary else DarkSurfaceVariant)
                                        .clickable { prefsManager.updateConfig { it.copy(dpadStepPx = step) } }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "${step.toInt()}px", color = if (isSelected) DarkBackground else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Ball Radius Size
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Ball Radius Calibration", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "${config.ballRadius.toInt()} px", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Slider(
                            value = config.ballRadius,
                            onValueChange = { rad -> prefsManager.updateConfig { it.copy(ballRadius = rad) } },
                            valueRange = 24f..52f,
                            colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldPrimary, inactiveTrackColor = DarkBorder)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// 9. ABOUT SCREEN
// -----------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "About msj xD", fontWeight = FontWeight.Bold, color = GoldPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                        .border(2.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "msj", color = GoldPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }

                Text(text = "msj xD PRO", color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 12.dp))
                Text(text = "Version 2.0 • Android 16 Edition", color = GoldSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(text = "100% Free & Unlimited", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "msj xD is completely free with zero ads, no subscriptions, no daily limits, and works offline. Features 4-Bank rebounds, extended guidelines, angle calculator, and floating HUD.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
