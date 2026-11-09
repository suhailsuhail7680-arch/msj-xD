# msj xD — Advanced 8 Ball Pool Aim Assistant

Build a high-performance, production-quality native Android 16 application named **msj xD**, inspired by "Aim Tool - Bloro 8 Pool" by WIND STUDIO. The app delivers a draggable floating aim HUD and full-screen guideline canvas over 8 Ball Pool, featuring precision cue/object ball positioning, bank shot reflections, pocket line guides, customization, and full Android 16 permissions & foreground service compliance.

## User Review & Critical Decisions

> [!IMPORTANT]
> **Key Architecture Decisions for Android 16 & Gaming Overlay:**
> - **Dual Overlay System**: We implement a floating draggable HUD panel (`WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`) alongside a full-screen transparent vector canvas overlay that renders the live guideline trajectories, cue ball, target ball, cushion bounce angles, and pocket indicators.
> - **Input Model**: Includes both precision touch drag-handles on the screen and fine-tuning D-pad step buttons (1px / 5px) on the floating HUD for pixel-perfect alignment on the Infinix Note 50s.
> - **Aim Calculation Depth**: Calculates extended direct shot lines, ball-to-pocket alignment, cue ball ghost impact position, and 1-to-2 cushion bank-shot reflections.
> - **Android 16 Compatibility**: Targets SDK 36. Declares and manages `SYSTEM_ALERT_WINDOW`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE` / `MEDIA_PROJECTION` with required foreground service types, notification channels, and battery optimization guidelines.
> - **100% Free & Local**: No accounts, no paywalls, zero ads, no cloud dependencies. Fast in-memory state with DataStore/SharedPreferences persistence.

- **Confirmed Decision 1**: Dual floating tool architecture: Movable floating HUD controller + high-performance transparent touch/draw canvas overlay.
- **Confirmed Decision 2**: Extended aiming guidance with direct trajectory, cue deflection/tangent line, pocket direction indicator, and bank cushion reflection.
- **Confirmed Decision 3**: Dual positioning controls: Direct screen drag on ball anchors plus precision D-pad micro-adjusters.

---

## 1. Overview & Core Concept

- **What It Does**: `msj xD` provides real-time aiming calculations and visual guides over pool games such as 8 Ball Pool. Users launch the overlay from the app, open their game, position the cue ball and target object ball, adjust the angle, and observe calculated shot lines, cushion rebounds, and pocket paths.
- **Target Audience**: 8 Ball Pool players seeking to improve aiming precision, visualize bank shot physics, master angles, and practice cushion bank shots on modern Android devices (specifically Infinix Note 50s and Android 16).
- **Key Value**: Delivers zero-latency mathematical trajectory visualization without intrusive ads or subscriptions, respecting modern Android security and system overlay guidelines.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Splash & Welcome**: Sleek dark gold branding with "msj xD" logo, quick version badge, and smooth fade-in.
2. **Setup & Permission Gate**: Interactive checklist checking Overlay Permission (`SYSTEM_ALERT_WINDOW`), Foreground Service Notification (`POST_NOTIFICATIONS`), and Battery Optimization exclusion with direct system intent launchers and live status indicators.
3. **Home Dashboard**: Quick Launch toggle for the Floating Aim Tool, active tool state indicator, quick presets (Direct Shot, Bank Shot, Practice Table), and navigation to all modules.
4. **Interactive Aim Simulation & Practice Screen**: An in-app full interactive pool table canvas where users can test and master aim trajectories directly inside the app without switching to the game.
5. **Floating Tool Customization Screen**: Controls for guideline length, line thickness, opacity, primary/secondary colors (Neon Gold, Cyber Yellow, Emerald Green, Electric Cyan, Hot Pink), cushion bounce toggle, and pocket target rings.
6. **Overlay Experience**:
   - Floating HUD: Minimized compact floating bubble -> expands to control panel with ball selector (Cue vs Object), D-pad directional nudges, shot angle slider, line toggle, reset, and close buttons.
   - Vector Guideline Canvas: Smooth lines depicting the cue stick trajectory, ghost cue ball contact point, object ball path toward pocket, and cushion reflection angle.
7. **Settings & About**: Preferences for vibration feedback, table aspect ratio presets (16:9, 20:9 for Infinix Note 50s), and app info.

### Visual Identity & Theme
- **Aesthetic**: Luxury Dark Gaming Tool — matte carbon black background, brushed metallic surfaces, high-contrast neon gold and vibrant cyber yellow accents.
- **Color Palette**:
  - Background: `#0B0C10` (Deep Carbon Obsidian)
  - Surface: `#16181E` (Dark Slate Card)
  - Surface Variant: `#222530` (Elevated Panel)
  - Primary Accent: `#FFD700` (Electric Gold)
  - Secondary Accent: `#FFC107` (Cyber Yellow)
  - Neon Indicator: `#00E5FF` (Laser Cyan)
  - Error/Close: `#FF4B4B`
  - Text Primary: `#FFFFFF`, Text Secondary: `#A0A5B5`
- **Typography**: Clean, bold athletic sans-serif headers with crisp monospace telemetry for coordinates and angles.
- **Interactive Feedback**: Haptic feedback on D-pad clicks and snap points, smooth spring transitions on modal expansion.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Native WindowManager Overlay vs Full-Screen View Only**
  - *Chosen Approach*: Use Android's `WindowManager.addView` with `TYPE_APPLICATION_OVERLAY` inside an active `ForegroundService`.
  - *Why*: Allows the aim tool to remain visible and interactive while 8 Ball Pool or any pool game is running in the foreground.
- **Decision 2: Math-Based Trajectory Engine + In-App Pool Table Simulator**
  - *Chosen Approach*: Implement a pure geometric vector physics model (cue ball center, object ball center, tangent/normal separation lines, cushion boundary reflections) combined with an on-screen practice table.
  - *Why*: Reliable across all devices and screen resolutions regardless of game updates or anti-screenshot flags, providing instantaneous feedback.
- **Decision 3: Modern Android 16 Foreground Service Lifecycle**
  - *Chosen Approach*: Declare `foregroundServiceType="specialUse"` / proper types with an ongoing system notification that allows users to pause, show, or dismiss the overlay directly from the notification shade.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────────┐
│                           msj xD App                            │
├───────────────────────────────┬─────────────────────────────────┤
│          Compose UI           │        Background Engine        │
│ ┌───────────────────────────┐ │ ┌─────────────────────────────┐ │
│ │ MainActivity (Single Act) │ │ │ AimOverlayService (FGS)     │ │
│ │ - SplashScreen            │ │ │ - WindowManager Manager     │ │
│ │ - HomeScreen              │ │ │ - Floating HUD Window       │ │
│ │ - AimToolScreen (Sim)     │ │ │ - Fullscreen Canvas Window  │ │
│ │ - FloatingSettingsScreen  │ │ │ - Touch & Drag Dispatcher   │ │
│ │ - PermissionsScreen       │ │ └──────────────┬──────────────┘ │
│ │ - SettingsScreen          │                  │                │
│ │ - AboutScreen             │                  │                │
│ └─────────────┬─────────────┘                  │                │
│               │                                │                │
│               ▼                                ▼                │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │                       AimEngineCore                         │ │
│ │ - Vector2D & PoolGeometry (Cue, Object, Pocket calculations)│ │
│ │ - Cushion Bank Reflection (1-2 cushion bounces)             │ │
│ │ - Ghost Ball contact position & deflection angles           │ │
│ └─────────────────────────────┬───────────────────────────────┘ │
│                               ▼                                 │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │                 Preferences & Settings Store                │ │
│ │ - Line colors, thickness, length, D-pad step, HUD positions │ │
│ └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### Component Structure
1. **`com.example.msjxd.model`**:
   - `Vector2D`: Vector operations (add, subtract, dot product, normalize, distance).
   - `AimConfiguration`: Line color, thickness, guideline length, showBankShots, showPocketGuides, dpadStep.
   - `BallPosition`: Position coordinates (x, y) and radii for Cue Ball and Object Ball.
   - `AimShotResult`: Calculated impact point, object ball trajectory, cue deflection, and cushion bounces.
2. **`com.example.msjxd.engine`**:
   - `AimCalculator`: Computes trajectory raycasts, collisions between cue ball and object ball, tangent impulse lines, and cushion reflections against screen/table boundaries.
3. **`com.example.msjxd.service`**:
   - `AimOverlayService`: Manages overlay windows (`TYPE_APPLICATION_OVERLAY`), notification channel, orientation listener, and lifecycle.
   - `OverlayHudView` & `OverlayCanvasView`: Draggable HUD composable and custom drawing canvas for lines/balls.
4. **`com.example.msjxd.ui`**:
   - `theme`: Deep dark obsidian + metallic gold theme.
   - `screens`: Splash, Home, AimTool (In-app simulator), FloatingSettings, Permissions, Settings, About.
   - `navigation`: Seamless state-based navigation with BackHandler support.
5. **`com.example.msjxd.data`**:
   - `AimPreferencesManager`: Persistent storage for all guideline and HUD configurations.

---

## 5. Implementation Sequence & Verification

1. **Phase 1: Update Platform Identity & Strings**
   - Update `metadata.json` and `res/values/strings.xml` to `msj xD`.
2. **Phase 2: Dependencies & Manifest Permissions**
   - Configure Android 16 permissions: `SYSTEM_ALERT_WINDOW`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `POST_NOTIFICATIONS`.
   - Register `AimOverlayService` with proper foreground service type.
3. **Phase 3: Core Geometry & Aim Engine**
   - Implement vector math, 2D ray-sphere intersections, cushion boundary reflections, and pocket targeting.
4. **Phase 4: Floating Service & Overlay Architecture**
   - Implement `AimOverlayService` with draggable HUD pill, expandable control panel, and full-screen transparent vector guideline canvas.
5. **Phase 5: Jetpack Compose App Screens & Navigation**
   - Build Splash, Home, Aim Tool Simulator, Floating Tool Settings, Permissions Setup, Settings, and About screens in gold/dark gaming aesthetic.
6. **Phase 6: Verification & Compilation**
   - Run `compile_applet` and verify zero errors, smooth navigation, and robust lifecycle management.
