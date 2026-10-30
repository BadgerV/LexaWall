# LexaWall Wallpaper Feature Implementation

## Overview
The wallpaper feature enables users to set LexaWall as their Android live wallpaper, displaying a daily vocabulary word with custom themes and ambient mode support for Always-On Displays.

---

## Critical Bug Fixed

### Issue: Wallpaper Not Being Set Despite System Picker Confirmation
**Root Cause:** On Android 13+ (API 33, Tiramisu), `ACTION_WALLPAPER_CHANGED` is a **system broadcast**. Registering the receiver with `Context.RECEIVER_NOT_EXPORTED` blocks system broadcasts — only broadcasts from the app's own process are delivered.

**Symptom:** User opens system wallpaper picker, applies LexaWall, but the app stays on the setup/settings screen instead of confirming the wallpaper was set.

**Fix:** Changed both OnboardingSetupScreen and SettingsScreen to register `ACTION_WALLPAPER_CHANGED` receiver with `Context.RECEIVER_EXPORTED` on API 33+.

---

## Files Involved

### 1. **LexaWallWallpaperService.kt** (`com.lexawall.wallpaper`)
**Purpose:** Live wallpaper service that renders a custom Canvas-based wallpaper with the daily word.

**Key Components:**
- `LexaWallEngine extends Engine` — Main rendering engine
  - `onCreate()` — Initializes paints, reads current wallpaper data, registers screen state receiver
  - `onSurfaceChanged()` — Sets surface dimensions and triggers wallpaper draw
  - `onVisibilityChanged()` — Starts/stops rotation timer when wallpaper visibility changes
  - `drawWallpaper()` — Main drawing method; checks `isScreenOff` to decide between full or ambient rendering
  - `drawAmbientWallpaper()` — Renders black background + simplified word text for Always-On Display
  - `ScreenStateReceiver` — BroadcastReceiver listening for `ACTION_SCREEN_OFF`/`ACTION_SCREEN_ON` to toggle `isScreenOff` for ambient mode detection
  - `startRotationTimer()` — Rotates wallpaper word on configurable interval (1/5/10 min); reads rotation speed each cycle for dynamic updates

**Features:**
- Gradient backgrounds based on theme
- Radial glow effect
- Word with POS (part of speech) and horizontal line
- Definition text with multi-line layout
- Example text in serif italic
- Tap-to-open word detail
- Ambient mode for AOD (Always-On Display) with low-brightness rendering
- Screen off detection via broadcasts

**Ambient Mode (AOD Support):**
- Declared in `lexawall_wallpaper.xml` with `android:supportsAmbientMode="true"`
- When screen off: draws black background + word + metadata at reduced opacity
- Preserves readability on low-brightness OLED panels during AOD

---

### 2. **WallpaperTheme.kt** (`com.lexawall.wallpaper`)
**Purpose:** Defines 10 color themes for the wallpaper with unlock levels tied to user progression.

**Themes:**
1. Midnight (Level 1) — Dark blue gradient
2. Arctic (Level 2) — Cool icy tones
3. Crimson (Level 3) — Deep red
4. Forest (Level 4) — Green shades
5. Jade (Level 5) — Emerald tones
6. Savanna (Level 6) — Warm earth tones
7. Obsidian (Level 6) — Very dark grey
8. Dusk (Level 7) — **Fixed from Level 5** (was duplicate with Jade)
9. Storm (Level 7) — Dark stormy blues
10. Rust (Level 8) — Orange-red
11. Ember (Level 9) — Bright orange
12. Void (Level 10) — Pure black

**Data:**
- `accentColor` — Primary accent color for UI elements
- `accentColorDim` — Muted accent for secondary elements
- `topColor` / `bottomColor` — Gradient endpoints for wallpaper background

---

### 3. **WallpaperWordManager.kt** (`com.lexawall.wallpaper`)
**Purpose:** Manages the current wallpaper word, theme, and rotation logic.

**Key Methods:**
- `getCurrentWord()` — Fetches current wallpaper word text, definition, POS from database/cache
- `getCurrentWordExample()` — Gets example sentence for current word
- `getActiveTheme()` — Retrieves user's selected wallpaper theme
- `updateCurrentWord()` — Rotates to next available word from daily pool
- `getRotationSpeed()` — Reads user's wallpaper rotation preference (1/5/10 min)

**Data Source:**
- Daily word pool (50 words) from `DailyWordPool.getDailyWords()`
- Current word ID stored in `PrefsManager.getCurrentWordId()`
- Active theme stored in `PrefsManager.getActiveTheme()`
- Rotation speed stored in `PrefsManager.getRotationSpeed()`

---

### 4. **PrefsManager.kt** (`com.lexawall.data.local`)
**Purpose:** SharedPreferences wrapper for app settings including wallpaper state.

**Wallpaper-Related Keys:**
- `wallpaper_set` — Boolean indicating if LexaWall is currently set as live wallpaper
- `wallpaper_opted_out` — Boolean indicating if user skipped wallpaper setup (only learns without wallpaper)
- `active_theme` — String key of selected theme
- `rotation_speed` — User's wallpaper word rotation interval ("1min", "5min", "10min")
- `current_word_id` — ID of word currently displayed on wallpaper

**Methods:**
- `isWallpaperSet(context)` / `saveWallpaperSet(context, value)`
- `isWallpaperOptedOut(context)` / `saveWallpaperOptedOut(context, value)`
- `getActiveTheme(context)` / `saveActiveTheme(context, key)`
- `getRotationSpeed(context)` / `saveRotationSpeed(context, speed)`
- `getCurrentWordId(context)` / `saveCurrentWordId(context, id)`

---

### 5. **OnboardingWallpaperScreen.kt** (`com.lexawall.feature.onboarding`)
**Purpose:** First onboarding screen that prompts user to set wallpaper or skip.

**UI:**
- Mascot animation
- Headline: "I'll live on your wallpaper"
- Description: "Every day, one word. You'll see it every time you unlock your phone."
- Button: "Let's do this →" (green, prominent)
- Link: "I'll learn without the wallpaper" (subtle, low opacity 0.35f, unenticing)

**Behavior:**
- `onSetWallpaper` → navigates to OnboardingSetupScreen
- `onSkip` → saves `wallpaper_opted_out = true`, navigates to OnboardingNotificationsScreen

**Design Note:**
- Skip link intentionally designed to be unnoticeable (low alpha) to discourage skipping

---

### 6. **OnboardingSetupScreen.kt** (`com.lexawall.feature.onboarding`)
**Purpose:** Second onboarding screen where user actually sets the wallpaper via system picker.

**UI:**
- Headline: "Two steps and you're in."
- Step 1: "Set Lexa as your wallpaper"
- Step 2: "Let Lexa nudge you."
- Button: "Open picker"
- Link: "Not now" (ghost button, lighter style)

**Wallpaper Detection (Multi-Layer):**

1. **Activity Result Launcher** (`rememberLauncherForActivityResult`)
   - Fires immediately when system picker closes (user applied or canceled)
   - Calls `completeSetupIfWallpaperActive()`
   - If not set, polls for 5 seconds (20 x 250ms checks) in case system is slow

2. **ACTION_WALLPAPER_CHANGED Broadcast Receiver**
   - **CRITICAL FIX:** Uses `Context.RECEIVER_EXPORTED` on API 33+ to receive system broadcasts
   - Listens for system's official wallpaper change notification
   - If received, treats setup as complete even if component check is inconsistent

3. **Lifecycle ON_RESUME**
   - Fallback: checks wallpaper on screen resume in case detection was missed
   - Allows recovery if user navigates back to this screen

**Intent Launch:**
```kotlin
val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
    putExtra(
        WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
        ComponentName(context, LexaWallWallpaperService::class.java)
    )
}
wallpaperLauncher.launch(intent)  // or fallback to Intent(Intent.ACTION_SET_WALLPAPER)
```

**Completion Flow:**
```
User taps "Open picker"
  ↓
System wallpaper picker opens
  ↓
User applies LexaWall (or cancels)
  ↓
Activity result fires → completeSetupIfWallpaperActive()
    AND
ACTION_WALLPAPER_CHANGED broadcast fires → complete setup immediately
    AND
Polling for 5 seconds if needed
  ↓
If LexaWall is active, save pref & navigate to Home
```

---

### 7. **SettingsScreen.kt** (`com.lexawall.feature.settings`)
**Purpose:** App settings with option to set wallpaper if not already set.

**Wallpaper Row:**
- Icon: Image (green, 0xFF4CAF50)
- Title: "Set Wallpaper"
- Subtitle: "Tap to set LexaWall as your wallpaper"
- Only shown if `!wallpaperSet`
- Uses same `rememberLauncherForActivityResult` pattern as OnboardingSetupScreen

**Wallpaper State Sync:**

1. **Activity Result Launcher**
   - Fires when picker closes
   - Calls `syncWallpaperState()` immediately
   - Then polls every 250ms for up to 3 seconds (12 checks) as fallback

2. **ACTION_WALLPAPER_CHANGED Broadcast**
   - **CRITICAL FIX:** Uses `Context.RECEIVER_EXPORTED` on API 33+ to receive system broadcasts
   - Sets `wallpaperSet = true` and saves preference

3. **Lifecycle ON_RESUME**
   - Checks wallpaper state whenever user returns to Settings
   - Ensures `wallpaperSet` boolean reflects reality

**syncWallpaperState() Logic:**
```kotlin
fun syncWallpaperState() {
    val isLexaWallpaper = WallpaperSetupUtils.isLexaWallpaperActive(context)
    if (isLexaWallpaper && !wallpaperSet) {
        PrefsManager.saveWallpaperSet(context, true)
        wallpaperSet = true
    } else if (!isLexaWallpaper && wallpaperSet) {
        PrefsManager.saveWallpaperSet(context, false)
        wallpaperSet = false
    }
}
```

---

### 8. **WallpaperSetupUtils.kt** (`com.lexawall.wallpaper`)
**Purpose:** Utility to check if LexaWall is currently the active live wallpaper.

**Method: `isLexaWallpaperActive(context)`**
- Checks `WallpaperManager.getInstance(context).wallpaperInfo`
- Compares service name against expected `com.lexawall.wallpaper.LexaWallWallpaperService`
- Handles OEM inconsistencies with multiple fallback checks:
  1. Exact match: `serviceName == expected`
  2. Suffix match: `serviceName.endsWith(".LexaWallWallpaperService")`
  3. Contains match: `serviceName.contains("LexaWallWallpaperService")`
  4. Package + non-empty check: `packageName == context.packageName && serviceName.isNotBlank()`

**Why Multiple Checks:**
- Different OEMs report wallpaper info inconsistently
- System wallpaper pickers may not immediately update component metadata
- Need robust detection across Samsung, Google, Xiaomi, etc.

---

### 9. **NavGraph.kt** (`com.lexawall.core.navigation`)
**Navigation Paths:**

**Onboarding Path:**
```
Splash
  ↓
OnboardingWallpaper
  ├─ "Set wallpaper" → OnboardingSetup
  └─ "Skip" → OnboardingNotifications
     ↓
OnboardingSetup (wallpaper picker)
  ├─ Wallpaper set → Home
  └─ "Not now" → OnboardingNotifications
     ↓
OnboardingNotifications → CalibrationWordDrop → Home
```

**Settings Path:**
```
Settings
  ├─ "Set Wallpaper" (if not set) → System Wallpaper Picker → Auto-sync
  ├─ "Theme" → ThemePickerScreen
  ├─ "Rotation Speed" → Dialog
  └─ "Competitive Mode" / "Notifications" → Toggles
```

---

### 10. **AndroidManifest.xml**
**Service Declaration:**
```xml
<service
    android:name=".wallpaper.LexaWallWallpaperService"
    android:enabled="true"
    android:exported="true"
    android:permission="android.permission.BIND_WALLPAPER">
    <intent-filter>
        <action android:name="android.service.wallpaper.WallpaperService" />
    </intent-filter>
    <meta-data
        android:name="android.service.wallpaper"
        android:resource="@xml/lexawall_wallpaper" />
</service>
```

**Permissions:**
- `BIND_WALLPAPER` — Allows service to be used as live wallpaper

---

### 11. **lexawall_wallpaper.xml** (`res/xml`)
**Wallpaper Metadata:**
```xml
<wallpaper xmlns:android="http://schemas.android.com/apk/res/android"
    android:description="@string/app_name"
    android:thumbnail="@drawable/lexawall_wallpaper_thumb"
    android:supportsAmbientMode="true" />
```

**Critical Attribute:** `android:supportsAmbientMode="true"`
- Tells Android system wallpaper engine that this live wallpaper supports Always-On Display
- Without this, system won't attempt to render wallpaper during AOD
- Must be present for AOD feature to work

---

## Complete Wallpaper Feature Flow

### Setting Wallpaper (Onboarding)
1. User sees OnboardingWallpaperScreen
2. Taps "Let's do this →"
3. Navigates to OnboardingSetupScreen
4. Taps "Open picker"
5. `wallpaperLauncher.launch(intent)` opens system wallpaper picker
6. System picker shows LexaWall in live wallpaper list
7. User taps LexaWall, system applies it
8. One of three detection methods fires:
   - Activity result callback + immediate check
   - ACTION_WALLPAPER_CHANGED broadcast (with RECEIVER_EXPORTED)
   - Polling during 5-second window
9. `completeSetupIfWallpaperActive()` verifies via `WallpaperSetupUtils.isLexaWallpaperActive()`
10. If verified: saves `wallpaper_set = true` & navigates to Home
11. If user taps "Not now": navigates to OnboardingNotifications

### Setting Wallpaper (Settings)
1. User navigates to Settings
2. If `wallpaperSet` is false, shows "Set Wallpaper" row
3. User taps "Set Wallpaper"
4. `wallpaperLauncher.launch(intent)` opens system wallpaper picker
5. Same detection flow as onboarding
6. `syncWallpaperState()` updates local state and prefs
7. UI updates to hide "Set Wallpaper" row once set

### Displaying Wallpaper
1. `LexaWallWallpaperService` is created and `onCreate()` runs
2. Immediately saves `wallpaper_set = true` (confirms service activated)
3. Reads current word via `WallpaperWordManager.getCurrentWord()`
4. Starts rotation timer via `startRotationTimer()` at user's configured interval
5. On each interval, calls `WallpaperWordManager.updateCurrentWord()` to rotate to next word
6. Calls `LexaWallWallpaperService.refresh()` to trigger redraw
7. `drawWallpaper()` renders full wallpaper with gradient, word, definition, example
8. If screen is off: renders ambient version (black bg + simple text)
9. User taps word → deep link to WordDetailScreen

### Theme Selection
1. User navigates to Settings → Theme
2. `ThemePickerScreen` shows all 10 themes with unlock levels
3. User selects unlocked theme
4. `saveActiveTheme()` stores in prefs
5. `LexaWallWallpaperService.refresh()` triggers redraw with new theme colors
6. Next time wallpaper draws, uses new `activeTheme` for gradients and accents

### Rotation Interval
1. User navigates to Settings → Rotation Speed
2. Dialog shows options: 1 min, 5 min, 10 min
3. User selects interval
4. `saveRotationSpeed()` stores in prefs
5. Next timer cycle reads new speed via `PrefsManager.getRotationSpeed()`
6. Applies new delay to next rotation

---

## Critical Implementation Details

### Screen State Detection (Ambient/AOD)
```kotlin
private inner class ScreenStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SCREEN_OFF -> {
                isScreenOff = true
                drawWallpaper()
            }
            Intent.ACTION_SCREEN_ON -> {
                isScreenOff = false
                drawWallpaper()
            }
        }
    }
}
```
- Toggles `isScreenOff` boolean on screen power events
- When true, `drawWallpaper()` calls `drawAmbientWallpaper()` instead
- Provides low-power rendering for Always-On Display

### Rotation Timer with Dynamic Speed
```kotlin
private fun startRotationTimer() {
    rotationJob?.cancel()
    rotationJob = engineScope.launch {
        while (isActive) {
            val intervalMs = when (PrefsManager.getRotationSpeed(applicationContext)) {
                "1min" -> 60_000L
                "5min" -> 300_000L
                else -> 600_000L
            }
            delay(intervalMs)
            WallpaperWordManager.updateCurrentWord(applicationContext)
            refreshWallpaper()
        }
    }
}
```
- Reads rotation speed **each cycle** (not once at start)
- Allows user to change speed and have it take effect immediately
- Cancels job when wallpaper becomes hidden

### Broadcast Receiver Export Level (CRITICAL FIX)
**Before (Broken):**
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
}
```
- `RECEIVER_NOT_EXPORTED` = receive only broadcasts from app's own process
- System broadcasts (like `ACTION_WALLPAPER_CHANGED`) are blocked
- App never learns wallpaper was set
- User stuck on setup screen forever

**After (Fixed):**
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
}
```
- `RECEIVER_EXPORTED` = receive broadcasts from any process (including system)
- System's `ACTION_WALLPAPER_CHANGED` is now delivered
- App immediately detects wallpaper was set
- Setup completes successfully

---

## Testing Checklist

- [ ] Onboarding: Tap "Let's do this" → System picker opens
- [ ] Onboarding: Select LexaWall in picker → App returns to setup screen
- [ ] Onboarding: Wallpaper actually appears on home screen after setup completes
- [ ] Onboarding: Tap "I'll learn without the wallpaper" → Skips to notifications, `wallpaper_opted_out = true`
- [ ] Settings: If wallpaper not set, "Set Wallpaper" row visible
- [ ] Settings: Tap "Set Wallpaper" → System picker opens, returns to settings
- [ ] Settings: After setting, "Set Wallpaper" row hidden
- [ ] Wallpaper: Word changes every configured interval (1/5/10 min)
- [ ] Wallpaper: Theme colors apply correctly
- [ ] Wallpaper: Tap word on wallpaper → Opens WordDetailScreen
- [ ] Wallpaper: Definition and example text display correctly
- [ ] AOD: Lock screen shows ambient version (black + simple text) on Always-On Display
- [ ] AOD: Full wallpaper returns when screen unlocked
- [ ] Rotation speed change: New interval takes effect within 5 seconds
- [ ] Theme change: New colors apply on next rotation or screen refresh

---

## Known Limitations & Future Improvements

1. **Theme Unlock Levels** — Currently hardcoded; could be tied to other progression (time, XP)
2. **Word Rotation** — Always cycles through daily pool; could implement user favorites
3. **Ambient Text Quality** — Simplified text to avoid burn-in; could optimize for specific devices
4. **Orientation** — Wallpaper assumes portrait; landscape support could be added
5. **Performance** — Paint objects recreated on each draw; could cache more aggressively

---

## Files Summary

| File | Purpose | Key Responsibility |
|------|---------|-------------------|
| LexaWallWallpaperService.kt | Live wallpaper rendering engine | Draw word, handle ambient mode, manage rotation |
| WallpaperTheme.kt | Theme color definitions | Store 10 themes with unlock levels |
| WallpaperWordManager.kt | Word and theme data access | Fetch/rotate words, manage current state |
| WallpaperSetupUtils.kt | Wallpaper detection utility | Verify if LexaWall is active wallpaper |
| PrefsManager.kt | SharedPreferences wrapper | Store wallpaper state, theme, rotation speed |
| OnboardingWallpaperScreen.kt | First onboarding UI | Prompt user to set wallpaper or skip |
| OnboardingSetupScreen.kt | Wallpaper setup UI | Open system picker, detect completion |
| SettingsScreen.kt | Settings UI | Manage wallpaper + theme + rotation settings |
| NavGraph.kt | Navigation definitions | Route between onboarding/settings/home |
| AndroidManifest.xml | Service registration | Declare wallpaper service with permissions |
| lexawall_wallpaper.xml | Wallpaper metadata | Enable AOD support |

---

**Last Updated:** March 1, 2026  
**Critical Fix Applied:** ACTION_WALLPAPER_CHANGED receiver now uses RECEIVER_EXPORTED on API 33+ to receive system broadcasts.
