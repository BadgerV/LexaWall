# AI Agent Implementation Brief: LexaWall Engagement Features

**Project:** LexaWall Android Vocabulary Learning App  
**Scope:** Complete implementation of engagement features (wallpaper quizzes, bonus challenges, habit stacking, custom backgrounds, voice practice, OCR word hunt, avatar system, daily summary)  
**Success Criteria:** All features fully functional, tested, and integrated into existing codebase with zero gaps

---

## CRITICAL INSTRUCTION

**Nothing missed will be considered a failure.** This brief specifies EVERY feature, UI element, button, interaction, edge case, and design detail. You must implement:
- All 9 features listed below
- All database changes
- All UI/UX flows
- All error handling
- All animations and transitions
- All color/styling decisions
- All notification logic
- All edge cases

Do not assume or improvise. Every element listed here is required.

---

## Feature 1: Wallpaper Quiz System

### 1A. Wallpaper Quiz Trigger Logic

**File to create/modify:** `wallpaper/WallpaperQuizEngine.kt`

**What it does:**
- Monitors screen-on events via existing `ScreenStateReceiver`
- On screen ON: waits random delay (30-90 seconds)
- Counts wallpaper rotations while screen is on
- After 2 rotations complete: triggers quiz overlay on current word
- If 5 minutes pass without 2 rotations: skips, reschedules next trigger 5+ hours later
- Enforces max 2 quizzes per day (reset at midnight)
- Never triggers if user already answered a quiz in last 5 hours

**Logic pseudocode:**
```
On screen ON event:
  if quizzes_today >= 2:
    return
  
  random_delay = Random(30_000 to 90_000) ms
  wait(random_delay)
  
  rotation_count = 0
  timeout_start = now()
  
  while rotation_count < 2:
    if now() - timeout_start > 5 minutes:
      next_trigger_time = now() + Random(5 to 8 hours)
      save(next_trigger_time)
      return
    
    on rotation_event():
      rotation_count++
  
  current_word = WallpaperWordManager.getCurrentWord()
  trigger_quiz_overlay(current_word)
  last_quiz_time = now()
  quizzes_today++
```

**Database:**
- New entity: `WallpaperQuizSession` (timestamp, word_id, correct_answer, xp_earned)
- Update `PrefsManager` with: `wallpaper_quiz_opted_in`, `wallpaper_quiz_count_today`, `last_wallpaper_quiz_time`, `next_wallpaper_quiz_trigger_time`

**Edge cases:**
- Screen turns off during quiz wait → cancel, reschedule
- User answers quiz, screen off, turns back on within 5 min → don't retrigger same word
- Rotation speed changes mid-wait → recalculate rotation count dynamically
- 11:59 PM trigger reaches 5-min timeout at 12:01 AM → still counts toward yesterday's quota

---

### 1B. Wallpaper Quiz Overlay UI

**File to create:** `wallpaper/WallpaperQuizOverlay.kt`

**Visual design:**
- Replaces entire wallpaper canvas temporarily
- Dark overlay background (match theme background or pure black)
- Centered question text: Large, white, Playfair Display font, 32sp
- 4 answer buttons arranged vertically:
  - Each button: full width minus 32dp padding, height 56dp, rounded corners (12dp)
  - Unselected button: LexaColors.SurfaceDark background, LexaColors.TextPrimary text
  - Hover/press state: slightly lighter background, subtle scale animation (98%)
  - Selected button (before result): Primary color background, white text
  - Correct answer button: Green (#4CAF50) background, white text, ✓ icon left of text
  - Wrong answer button: Red (#F44336) background, white text, ✗ icon left of text
- Result feedback: Below buttons, large text
  - Correct: "✓ Correct! +15 XP" in green, 28sp, Playfair Display
  - Wrong: "✗ Wrong. +1 XP" in red, 28sp, Playfair Display
  - Also show the correct answer highlighted: "The correct answer was: [answer]" in secondary text color

**Interaction flow:**
1. Quiz overlay renders (fade-in animation, 300ms)
2. User taps answer button
3. Button animates (scale to 95%, then back to 100%)
4. XP updated in background (via `QuizViewModel.recordAnswer()`)
5. Correct/wrong feedback displays (100ms delay)
6. Highlight correct answer if user was wrong
7. After 10 seconds (or user taps screen): overlay fades out (300ms)
8. Returns to wallpaper with rotation animation

**Button styling:**
```
Answer Button:
  - cornerRadius: 12dp
  - padding: 16dp
  - borderWidth: 0
  - font: LexaType.bodyLarge (18sp)
  - height: 56dp
  - ripple effect on tap

Correct state:
  - backgroundColor: Color(0xFF4CAF50)
  - textColor: Color.White
  - icon: Icons.Default.Check (20dp)

Wrong state:
  - backgroundColor: Color(0xFFF44336)
  - textColor: Color.White
  - icon: Icons.Default.Close (20dp)

Disabled (after answer):
  - all buttons alpha = 0.5f
```

**Animations:**
- Fade in wallpaper quiz: `animateFloatAsState(0f to 1f, 300ms)`
- Button tap: scale animation (1f to 0.95f to 1f, 150ms total)
- Feedback text slide-in from bottom (200ms, easing = EaseOutCubic)
- Fade out: reverse of fade in (300ms)

---

### 1C. Wallpaper Quiz User Opt-In

**File to modify:** `feature/settings/SettingsScreen.kt`

**UI element location:** After "Rotation Speed" row

**New Settings Row:**
- Icon: Dice icon (Icons.Default.Casino or similar), color = Primary
- Title: "Wallpaper Quizzes"
- Subtitle: "Get random quizzes on your wallpaper for bonus XP"
- Control: Toggle switch (SwitchDefaults with Primary colors)
- Row styling: Same as existing settings rows (rounded corners, surface background)

**Toggle behavior:**
- Default state: OFF (opt-in required)
- When ON: `PrefsManager.saveWallpaperQuizOptedIn(context, true)`
  - Notification Quiz disabled automatically
  - Notification Slot 1 becomes available for other notifications
  - User sees confirmation toast: "Wallpaper quizzes enabled! (Max 2 per day)"
- When OFF: Reverts to Notification Quiz
  - Toast: "Wallpaper quizzes disabled. Notification quiz restored."

**Database update:** `PrefsManager.kt` add methods:
```kotlin
fun isWallpaperQuizOptedIn(context: Context): Boolean
fun saveWallpaperQuizOptedIn(context: Context, value: Boolean)
```

---

### 1D. XP Integration for Wallpaper Quiz

**Modify:** `feature/quiz/QuizViewModel.kt`

**New method:** `recordWallpaperQuizAnswer(wordId: String, wasCorrect: Boolean)`

**Logic:**
```kotlin
fun recordWallpaperQuizAnswer(wordId: String, wasCorrect: Boolean) {
  val xpEarned = if (wasCorrect) 15 else 1
  val currentLevel = database.levelDao().getLevel()
  val newXp = currentLevel.currentXp + xpEarned
  
  database.levelDao().updateLevel(
    currentLevel.copy(
      currentXp = newXp,
      totalXpEarned = currentLevel.totalXpEarned + xpEarned
    )
  )
  
  database.wallpaperQuizSessionDao().insertSession(
    WallpaperQuizSession(
      timestamp = LocalDate.now().toString(),
      wordId = wordId,
      correct = wasCorrect,
      xpEarned = xpEarned
    )
  )
  
  // Check for level up
  val oldLevel = XpEngine.getLevelForXp(currentLevel.currentXp)
  val newLevel = XpEngine.getLevelForXp(newXp)
  if (newLevel.level > oldLevel.level) {
    _levelUpEvent.value = LevelUpEvent(newLevel)
  }
}
```

**Important:** Does NOT trigger `StreakEngine.recordCompletion()`. Wallpaper quiz XP is bonus only.

---

## Feature 2: Bonus Challenge Quiz System

### 2A. Bonus Challenge Trigger & Management

**File to create:** `domain/engine/BonusChallengeEngine.kt`

**What it does:**
- Determines if user is eligible for bonus challenge (completed main quiz today)
- Limits to max 1 per week per user
- Randomly selects challenge type (Double XP, Storm, etc.)
- Schedules challenge notification as Slot 2 replacement
- Tracks completion to prevent duplicates

**Challenge types (expand as needed):**
1. **Weekend Double XP** (appears Sat/Sun only)
   - Reward: +25 XP instead of usual
   - Quiz: 1 question from daily pool
   
2. **Storm Challenge** (random day, ~1x per month)
   - Reward: +30 XP + rare badge
   - Quiz: 1 harder question (C1 level)

3. **Speed Run** (random, ~2x per month)
   - Reward: Answer within 5 seconds for +25 XP (auto-fail after 5s)
   - Quiz: 1 question, visible countdown timer

**Eligibility logic:**
```kotlin
fun isEligibleForBonus(userId: String): Boolean {
  val completedMainQuizToday = 
    database.quizSessionDao()
      .getSessionsForDate(LocalDate.now())
      .isNotEmpty()
  
  val lastBonusDate = database.bonusChallengeDao()
    .getLastCompletedDate(userId)
  val daysSinceLastBonus = 
    ChronoUnit.DAYS.between(lastBonusDate, LocalDate.now())
  
  return completedMainQuizToday && daysSinceLastBonus >= 7
}

fun selectRandomChallenge(): ChallengeType {
  val dayOfWeek = LocalDate.now().dayOfWeek
  return when {
    dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY 
      -> ChallengeType.WEEKEND_DOUBLE_XP
    Random.nextDouble() < 0.05 -> ChallengeType.STORM
    Random.nextDouble() < 0.10 -> ChallengeType.SPEED_RUN
    else -> null
  }
}
```

**Database:**
- New entity: `BonusChallengeSession` (id, challenge_type, timestamp, completed, xp_earned)
- Add DAO: `BonusChallengeDao` with methods: `getLastCompletedDate()`, `insertSession()`, `getById()`

---

### 2B. Bonus Challenge Notification

**File to modify:** `notifications/NotificationScheduler.kt`

**Notification content:**
- Title: Challenge name ("🌩️ Storm Challenge Unlocked!" or "⚡ Weekend Double XP!")
- Body: "Complete a quiz for +25 XP bonus today"
- Action button: "Try Challenge" (opens app to challenge UI)
- Large icon: Challenge emoji or badge icon

**Notification ID:** Unique per challenge (prevent duplicates)

**Timing:**
- Triggered after user completes main quiz if eligible
- Send immediately (don't wait for Slot 2 time)
- If Slot 2 notification already scheduled, delay challenge notification by 30 minutes

**Code:**
```kotlin
fun scheduleBonus ChallengeNotification(challenge: ChallengeType) {
  val isEligible = BonusChallengeEngine.isEligibleForBonus(context)
  if (!isEligible) return
  
  val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
    .setSmallIcon(R.drawable.ic_notification)
    .setLargeIcon(BitmapFactory.decodeResource(context.resources, 
      getChallengeIconFor(challenge)))
    .setContentTitle(getChallengeTitle(challenge))
    .setContentText("Complete a quiz for ${getChallengeReward(challenge)} XP bonus today")
    .setPriority(NotificationCompat.PRIORITY_HIGH)
    .setAutoCancel(true)
    .addAction(
      R.drawable.ic_play,
      "Try Challenge",
      PendingIntent.getActivity(context, CHALLENGE_REQUEST_CODE,
        Intent(context, MainActivity::class.java).apply {
          action = "CHALLENGE_QUIZ"
          putExtra("challenge_type", challenge.name)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
    )
  
  NotificationManagerCompat.from(context).notify(BONUS_CHALLENGE_ID, notificationBuilder.build())
}
```

---

### 2C. Bonus Challenge Quiz UI

**File to create:** `feature/quiz/BonusChallengeScreen.kt`

**Layout:**
- Header: Challenge banner with icon, name, reward prominently displayed
  - Background gradient (theme-based)
  - Text: "🌩️ STORM CHALLENGE" in white, 24sp, Playfair Display
  - Subtitle: "+30 XP + Storm Badge" in gold (#FFD700), 18sp
  
- Quiz section: Standard multiple-choice (reuse existing `QuizQuestionScreen` styling but with challenge colors)
  
- Answer buttons: Same as Wallpaper Quiz but with challenge-specific colors
  - Correct: Gold (#FFD700) instead of green
  - Wrong: Orange (#FF9800) instead of red

- Result screen: Challenge-specific celebration
  - Correct: "⚡ Electrifying! +30 XP!" with particle animation
  - Wrong: "⚡ Keep learning! +1 XP" with subtle animation

**Challenge-specific mechanics:**

Speed Run version:
- Add visible countdown timer (5 seconds, large red numbers, 48sp)
- Timer starts immediately (no wait for tap)
- Auto-fail and show feedback if timer reaches 0
- Visual feedback: timer turns red at 2 seconds, pulses

**Navigation:**
- Launch via action from notification OR from home screen "Challenges" tab
- Close button (X) at top-left, returns to home
- Show "1/1 Challenge Completed Today" message at bottom

---

## Feature 3: Habit-Stacking Prompts

### 3A. Habit-Stacking Settings UI

**File to modify:** `feature/settings/SettingsScreen.kt`

**New Settings Row (after Rotation Speed):**
- Icon: Alarm icon (Icons.Default.Schedule), color = Purple (#9C27B0)
- Title: "Learning Routine"
- Subtitle: User's selected time (e.g., "Daily at 8:30 AM") or "Not set"
- Tap action: Opens `HabitStackDialog`

**Dialog UI (`feature/settings/HabitStackDialog.kt`):**
- Title: "When do you learn best?"
- Subtitle: "We'll remind you at this time daily"
- Options (vertical list, selectable):
  1. 🌅 Morning (7:00 AM)
  2. 🍽️ Breakfast (8:00 AM)
  3. 🚗 Commute (9:00 AM)
  4. 🍴 Lunch (12:00 PM)
  5. ☕ Afternoon Break (3:00 PM)
  6. 🌆 Evening (6:00 PM)
  7. 🌙 Bedtime (9:00 PM)

- Selection styling:
  - Unselected: Gray text, subtle background
  - Selected: Primary color text, Primary color background with transparency, checkmark icon
  - Each option has emoji (left) + time text (right)

- Bottom buttons:
  - "Cancel" (ghost button, left)
  - "Save" (primary button, right, disabled until selection made)

**Database:**
- `PrefsManager` add methods:
  ```kotlin
  fun getHabitStackTime(context: Context): Int? // hour of day (0-23)
  fun saveHabitStackTime(context: Context, hour: Int)
  fun clearHabitStackTime(context: Context)
  ```

---

### 3B. Habit-Stacking Notification Scheduling

**File to modify:** `notifications/NotificationScheduler.kt`

**New method:** `scheduleHabitStackNotification(context, hour: Int)`

**Logic:**
- Calculates time until next scheduled hour
- If already past that hour today: schedule for tomorrow
- Reschedules daily using WorkManager (PeriodicWorkRequest)
- Persists time in PrefsManager

**Notification content:**
- Title: "Time for your word!" or "Learning time!"
- Body: Context-aware message based on time:
  - 7 AM: "🌅 Good morning! Let's learn a new word."
  - 8 AM: "🍽️ Time to learn over breakfast"
  - 9 AM: "🚗 Learn something new on your commute"
  - 12 PM: "🍴 Lunch break? Let's expand your vocabulary"
  - 3 PM: "☕ Afternoon refresh — time for a word"
  - 6 PM: "🌆 Evening learning — your word awaits"
  - 9 PM: "🌙 Before bed, master today's word"
- Action: Opens app to daily quiz screen
- Importance: IMPORTANCE_HIGH

**Code:**
```kotlin
fun scheduleHabitStackNotification(context: Context) {
  val habitHour = PrefsManager.getHabitStackTime(context) ?: return
  val now = Calendar.getInstance()
  val scheduledTime = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, habitHour)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
  }
  
  if (scheduledTime.before(now)) {
    scheduledTime.add(Calendar.DAY_OF_MONTH, 1)
  }
  
  val delayMillis = scheduledTime.timeInMillis - now.timeInMillis
  
  val notificationWorker = PeriodicWorkRequestBuilder<HabitStackWorker>(
    1, TimeUnit.DAYS
  ).build()
  
  WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "habit_stack_notification",
    ExistingPeriodicWorkPolicy.KEEP,
    notificationWorker
  )
}
```

---

## Feature 4: Custom Background Images

### 4A. Image Upload & Storage

**File to create:** `feature/settings/CustomImageManager.kt`

**What it does:**
- Handles image selection from gallery
- Compresses to max 2MB, 1080×1920
- Stores in app cache directory
- Updates wallpaper to render custom image
- Provides fallback if image is corrupted

**Method:** `selectAndSaveCustomImage(context, uri: Uri): Boolean`

```kotlin
fun selectAndSaveCustomImage(context: Context, uri: Uri): Boolean {
  return try {
    // Load from URI
    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    
    // Resize to 1080×1920
    val resized = Bitmap.createScaledBitmap(bitmap, 1080, 1920, true)
    
    // Compress to JPEG, max 2MB
    val file = File(context.cacheDir, "custom_wallpaper.jpg")
    val fos = FileOutputStream(file)
    var quality = 95
    var success = false
    
    while (quality > 0 && !success) {
      resized.compress(Bitmap.CompressFormat.JPEG, quality, fos)
      success = file.length() <= 2_000_000 // 2MB
      quality -= 5
    }
    
    fos.close()
    
    if (success) {
      PrefsManager.saveCustomImagePath(context, file.absolutePath)
      PrefsManager.saveCustomImageEnabled(context, true)
      return true
    }
    false
  } catch (e: Exception) {
    LexaWallLogger.e("CustomImage", "Failed to save image", e)
    false
  }
}
```

**Database:**
- `PrefsManager` add:
  ```kotlin
  fun getCustomImagePath(context: Context): String?
  fun saveCustomImagePath(context: Context, path: String)
  fun isCustomImageEnabled(context: Context): Boolean
  fun saveCustomImageEnabled(context: Context, enabled: Boolean)
  ```

---

### 4B. Settings UI for Custom Image

**File to modify:** `feature/settings/SettingsScreen.kt`

**New Settings Row (after Theme picker):**
- Icon: Image icon (Icons.Default.Image), color = Teal (#00BCD4)
- Title: "Custom Background"
- Subtitle: "Add a personal photo" or "Custom photo active"
- Tap action: Opens image picker (ACTION_PICK, image/* mimetype)
  - On image selected: calls `CustomImageManager.selectAndSaveCustomImage()`
  - Shows toast: "Photo saved! Your wallpaper now uses your custom image."
  - If selection fails: "Unable to load image. Try a different photo."

**After image is uploaded:**
- Add remove button below settings row
- Button text: "Remove Custom Background"
- Styling: Ghost button with warning color (Orange)
- On tap: Shows confirmation dialog ("Remove custom photo and return to themes?")
  - On confirm: Clears image, reverts to active theme

**Theme picker interaction:**
- When custom image is active: Theme picker shows as grayed out
- Subtitle changes: "Currently using custom photo"
- Explanation text: "Switch back to themes by removing your custom photo above"
- If user taps theme row while custom image active: Toast "Disable custom photo first"

---

### 4C. Custom Image Rendering in Wallpaper

**File to modify:** `wallpaper/LexaWallWallpaperService.kt`

**In `drawWallpaper()` method, before drawing text:**

```kotlin
override fun drawWallpaper() {
  val canvas = surfaceHolder.lockCanvas() ?: return
  
  try {
    // Load custom image if enabled
    val customImagePath = PrefsManager.getCustomImagePath(context)
    val imageEnabled = PrefsManager.isCustomImageEnabled(context)
    
    if (imageEnabled && customImagePath != null) {
      val bitmap = BitmapFactory.decodeFile(customImagePath)
      if (bitmap != null) {
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        
        // Draw 30% black overlay for text readability
        val overlayPaint = Paint().apply {
          color = Color.BLACK
          alpha = (0.3f * 255).toInt()
        }
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), overlayPaint)
      } else {
        // Fallback to theme
        drawThemeGradient(canvas)
      }
    } else {
      // Draw normal theme gradient
      drawThemeGradient(canvas)
    }
    
    // Draw text (word, definition, etc.) with auto-detected color
    val textColor = detectOptimalTextColor(canvas)
    drawWord(canvas, textColor)
    drawMetadata(canvas, textColor)
    
  } catch (e: Exception) {
    LexaWallLogger.e("Wallpaper", "Error drawing wallpaper", e)
    // Fallback to theme gradient
    drawThemeGradient(canvas)
  } finally {
    if (canvas != null) {
      surfaceHolder.unlockCanvasAndPost(canvas)
    }
  }
}

private fun detectOptimalTextColor(canvas: Canvas): Int {
  // Sample center 5x5 pixels, calculate average brightness
  val colors = IntArray(25)
  val bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888)
  canvas.drawBitmap(bitmap, 0f, 0f, null)
  
  bitmap.getPixels(colors, 0, 5, canvas.getWidth() / 2 - 2, canvas.getHeight() / 2 - 2, 5, 5)
  
  val avgBrightness = colors.map { color ->
    val r = Color.red(color)
    val g = Color.green(color)
    val b = Color.blue(color)
    (r + g + b) / 3
  }.average()
  
  // If dark: use white text. If light: use black text.
  return if (avgBrightness < 128) Color.WHITE else Color.BLACK
}
```

---

### 4D. Quiz Overlay on Custom Image

**File to modify:** `wallpaper/WallpaperQuizOverlay.kt`

**When quiz is triggered with custom image active:**
- Don't show custom image behind quiz
- Instead: render dark background (LexaColors.BackgroundDark, opacity 100%)
- Keep all quiz styling (centered buttons, white text)
- Ensures maximum readability regardless of image

```kotlin
fun drawQuizOverlay(canvas: Canvas, quiz: WallpaperQuiz) {
  // Dark background (ignore custom image)
  canvas.drawColor(Color.parseColor("#1a1a1a")) // or LexaColors.BackgroundDark
  
  // Question and buttons rendered as before
  drawQuizContent(canvas, quiz)
}
```

---

## Feature 5: Voice Pronunciation Practice

### 5A. Voice Practice Screen

**File to create:** `feature/voicepractice/VoicePracticeScreen.kt`

**Layout:**
- Top: Word to practice (large, 48sp, Playfair Display)
- Middle: Large circular record button
  - Background: Primary color circle (120dp diameter)
  - Icon: Microphone (Icons.Default.Mic) in white
  - On tap: starts recording audio
  - During recording: pulse animation (scale 1f → 1.1f → 1f, repeat)
  - Color changes to red during recording
  - Tap again to stop recording
  
- Below button: Status text
  - Default: "Tap microphone to practice speaking"
  - Recording: "Recording... (elapsed time in MM:SS format)"
  - Processing: "Analyzing your pronunciation..."
  - Result: "Good!" (green) or "Try again" (orange)

- Result feedback section (appears after recording stops):
  - Correctness score: "98% Match" or "75% Match" (green/orange)
  - Feedback text: "Excellent pronunciation!" or "Listen closely to this part: [phonetic breakdown]"
  - Buttons:
    - "Try Again" (secondary button, left)
    - "Next Word" (primary button, right)

- Navigation:
  - Top-left: Back arrow button
  - Top-right: Skip button (ghost, gray)

**Styling:**
- Background: LexaColors.BackgroundDark
- Buttons: LexaColors.Primary (record), LexaColors.Success (next word)
- Text: LexaColors.TextPrimary / TextSecondary

---

### 5B. Speech Recognition Integration

**File to create:** `feature/voicepractice/SpeechRecognizer.kt`

**Using Android Speech Recognition API:**

```kotlin
class VoicePracticeEngine(private val context: Context) {
  private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
  private var isListening = false
  
  fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
    isListening = true
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
      putExtra(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT_BUNDLE, true)
    }
    
    speechRecognizer.setRecognitionListener(object : RecognitionListener {
      override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val topMatch = matches?.firstOrNull() ?: ""
        onResult(topMatch)
        isListening = false
      }
      
      override fun onError(errorCode: Int) {
        val errorMsg = when (errorCode) {
          SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
          SpeechRecognizer.ERROR_NETWORK -> "Network error"
          SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
          else -> "Recognition error: $errorCode"
        }
        onError(errorMsg)
        isListening = false
      }
      
      override fun onReadyForSpeech(params: Bundle?) {}
      override fun onBeginningOfSpeech() {}
      override fun onRmsChanged(rmsdB: Float) {}
      override fun onBufferReceived(buffer: ByteArray?) {}
      override fun onEndOfSpeech() {}
      override fun onPartialResults(partialResults: Bundle?) {}
      override fun onEvent(eventType: Int, params: Bundle?) {}
    })
    
    speechRecognizer.startListening(intent)
  }
  
  fun stopListening() {
    speechRecognizer.stopListening()
    isListening = false
  }
  
  fun comparePronunciation(targetWord: String, spokenText: String): Float {
    // Simple algorithm: normalize both, compare similarity
    val targetNorm = targetWord.lowercase().trim()
    val spokenNorm = spokenText.lowercase().trim()
    
    return if (spokenNorm.contains(targetNorm) || targetNorm.contains(spokenNorm)) {
      1.0f // Perfect match
    } else {
      // Calculate Levenshtein distance for partial match
      val distance = levenshteinDistance(targetNorm, spokenNorm)
      val maxLen = maxOf(targetNorm.length, spokenNorm.length)
      1.0f - (distance.toFloat() / maxLen)
    }
  }
  
  private fun levenshteinDistance(s1: String, s2: String): Int {
    val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
    for (i in 0..s1.length) dp[i][0] = i
    for (j in 0..s2.length) dp[0][j] = j
    for (i in 1..s1.length) {
      for (j in 1..s2.length) {
        dp[i][j] = when {
          s1[i-1] == s2[j-1] -> dp[i-1][j-1]
          else -> 1 + minOf(dp[i-1][j], dp[i][j-1], dp[i-1][j-1])
        }
      }
    }
    return dp[s1.length][s2.length]
  }
}
```

**AndroidManifest.xml additions:**
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

---

### 5C. Navigation to Voice Practice

**File to modify:** `core/navigation/NavGraph.kt`

**New route:** `voice_practice/{wordId}`

**Accessible from:**
- Word Detail Screen (in Wander): Add "Practice Voice" button below definition
- Settings: Add "Voice Practice Mode" option

**Button styling (in word detail):**
- Icon: Microphone (Icons.Default.VolumeUp or Mic)
- Text: "Practice Pronunciation"
- Color: Primary
- Position: Below audio playback button, if present

---

## Feature 6: Real-World Word Hunt (OCR)

### 6A. OCR Word Hunt Challenge Screen

**File to create:** `feature/wordhunt/WordHuntScreen.kt`

**Layout:**
- Header: "Find Today's Word"
  - Subtitle: Display today's wallpaper word in large text (32sp)
  - Small hint: "Found in photos? Spot it in real life!"

- Camera preview: Large central area showing camera feed
  - Overlay reticle in center (cross-hair or circle, Primary color, semi-transparent)
  - Instruction text at top: "Point camera at text"
  - Instruction text at bottom: "Hold steady for 2 seconds"

- Buttons below camera:
  - Left button: Close (X icon, ghost style)
  - Right button: Snap/Scan (Camera icon, primary color, large 60dp diameter)
  - During analysis: "Scanning text..." progress indicator (circular progress, 24dp, center aligned)

- Result screen (after scan):
  - If word detected:
    - ✓ Success message: "Found it! 🎉 +20 XP"
    - Show highlighted text: "[Word] highlighted in green in the photo
    - Button: "Great! Continue" (primary)
    - Limitation message: "You can find [word] once per day"
    
  - If word NOT detected:
    - ✗ Message: "Hmm, [word] not found in this image"
    - Help text: "Try a clearer photo or text"
    - Buttons: "Try Again" (left), "Skip" (right)

- Navigation:
  - On close: Returns to home screen
  - On success: Returns to home, shows toast "+20 XP earned from Word Hunt"
  - Accessible from: Settings tab "Daily Challenges" OR Home screen widget

---

### 6B. Camera & OCR Integration

**File to create:** `feature/wordhunt/OCREngine.kt`

**Using Google ML Kit Text Recognition:**

```kotlin
class WordHuntOCREngine(private val context: Context) {
  private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
  
  fun scanImageForWord(imageBitmap: Bitmap, targetWord: String, 
    onSuccess: (Boolean, List<String>) -> Unit, onError: (String) -> Unit) {
    
    val image = InputImage.fromBitmap(imageBitmap, 0)
    
    recognizer.process(image)
      .addOnSuccessListener { visionText ->
        val detectedTexts = mutableListOf<String>()
        val foundWord = visionText.textBlocks.any { block ->
          block.text.lowercase().contains(targetWord.lowercase()).also {
            if (it) detectedTexts.add(block.text)
          }
        }
        onSuccess(foundWord, detectedTexts)
      }
      .addOnFailureListener { e ->
        onError("OCR failed: ${e.message}")
      }
  }
}
```

**Dependencies (build.gradle.kts):**
```gradle
implementation("com.google.mlkit:text-recognition:16.0.0")
```

**AndroidManifest.xml additions:**
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="true" />
```

**Camera integration:**
- Use `androidx.camera:camera-core:1.3.0` for camera feed
- Permissions: Request CAMERA at runtime (API 23+)
- Preview: CameraX PreviewView widget

---

### 6C. Word Hunt Rewards & Limits

**Database:**
- New entity: `WordHuntSession` (date, word_id, found, xp_earned)
- New method in DAO: `getLastHuntDate(wordId): LocalDate?`

**Logic in `WordHuntScreen`:**
```kotlin
fun isWordAlreadyFoundToday(wordId: String): Boolean {
  return database.wordHuntDao().getLastHuntDate(wordId) == LocalDate.now()
}

fun recordWordHunt(wordId: String, found: Boolean) {
  if (found) {
    val xpEarned = 20
    database.wordHuntDao().insertSession(
      WordHuntSession(
        date = LocalDate.now(),
        wordId = wordId,
        found = true,
        xpEarned = xpEarned
      )
    )
    // Add XP (same as wallpaper quiz logic)
    recordXpEarned(xpEarned)
  }
}
```

**Safeguard:** If user tries to scan same word twice in one day:
- Don't award XP
- Show message: "You already found this word today! Come back tomorrow."

---

## Feature 7: Avatar System & Customization

### 7A. Avatar Profile Screen

**File to create:** `feature/profile/ProfileScreen.kt`

**Layout:**
- Header: "Your Avatar"
  - Large avatar display (200×200 dp, centered)
  - Avatar name below (e.g., "Lexa the Linguist")
  
- Stats section (3 columns):
  - Column 1: Current streak (e.g., "7 Days 🔥")
  - Column 2: Total XP (e.g., "2,450 XP 💎")
  - Column 3: Level (e.g., "Level 12 ⭐")

- Outfits section:
  - Title: "Unlocked Outfits"
  - Grid of outfit cards (2 columns)
  - Each outfit card:
    - Avatar preview with outfit (80×80 dp)
    - Outfit name (e.g., "Scholar Hat", "Gold Robe")
    - Unlock condition (e.g., "7-day streak" or "Level 15")
    - Selection indicator (checkmark if currently active)
    - Tap to select outfit
  
  - Locked outfits section:
    - Title: "Coming Soon"
    - Grayed-out cards showing next outfit to unlock
    - Requirement text: "Reach 14-day streak"

- Navigation: Back button (top-left)

---

### 7B. Avatar Data & Unlocks

**File to create:** `domain/engine/AvatarEngine.kt`

**Outfit definitions:**
```kotlin
data class AvatarOutfit(
  val id: String,
  val name: String,
  val description: String,
  val iconResourceId: Int,
  val unlockedAt: AvatarUnlockCondition
)

sealed class AvatarUnlockCondition {
  object StartingOutfit : AvatarUnlockCondition()
  data class StreakDays(val days: Int) : AvatarUnlockCondition()
  data class XpThreshold(val xp: Int) : AvatarUnlockCondition()
  data class LevelReached(val level: Int) : AvatarUnlockCondition()
}

object AvatarDefinitions {
  val outfits = listOf(
    AvatarOutfit("base", "Lexa (Default)", "The classic Lexa", R.drawable.avatar_lexa_base, 
      AvatarUnlockCondition.StartingOutfit),
    AvatarOutfit("scholar", "Scholar", "Lexa with scholar's cap", R.drawable.avatar_lexa_scholar,
      AvatarUnlockCondition.StreakDays(7)),
    AvatarOutfit("gold_robe", "Gold Robe", "Luxurious golden attire", R.drawable.avatar_lexa_gold,
      AvatarUnlockCondition.StreakDays(14)),
    AvatarOutfit("crown", "The Master", "Crown for vocabulary masters", R.drawable.avatar_lexa_crown,
      AvatarUnlockCondition.LevelReached(20)),
    AvatarOutfit("astronaut", "Explorer", "Space explorer outfit", R.drawable.avatar_lexa_explorer,
      AvatarUnlockCondition.XpThreshold(5000)),
    // Add 5+ more
  )
  
  fun getUnlockedOutfits(streakDays: Int, xp: Int, level: Int): List<AvatarOutfit> {
    return outfits.filter { outfit ->
      when (outfit.unlockedAt) {
        AvatarUnlockCondition.StartingOutfit -> true
        is AvatarUnlockCondition.StreakDays -> streakDays >= outfit.unlockedAt.days
        is AvatarUnlockCondition.XpThreshold -> xp >= outfit.unlockedAt.xp
        is AvatarUnlockCondition.LevelReached -> level >= outfit.unlockedAt.level
      }
    }
  }
}
```

**Database:**
- `PrefsManager` add:
  ```kotlin
  fun getSelectedAvatarOutfit(context: Context): String // outfit ID
  fun saveSelectedAvatarOutfit(context: Context, outfitId: String)
  ```

---

### 7C. Avatar Display on Wallpaper

**File to modify:** `wallpaper/LexaWallWallpaperService.kt`

**Optional feature:** Small avatar icon in corner of wallpaper

**In `drawWallpaper()` method, after drawing word text:**

```kotlin
// Optional: Draw avatar in corner (user can toggle this in settings)
if (PrefsManager.showAvatarOnWallpaper(context)) {
  val selectedOutfit = PrefsManager.getSelectedAvatarOutfit(context)
  val avatarBitmap = loadAvatarBitmap(selectedOutfit)
  
  if (avatarBitmap != null) {
    val size = 60.dp.toPx() // 60dp avatar size
    val padding = 16.dp.toPx()
    
    val x = canvas.width - size - padding
    val y = canvas.height - size - padding
    
    // Draw avatar with slight shadow
    val shadowPaint = Paint().apply {
      color = Color.BLACK
      alpha = 50
    }
    canvas.drawCircle(x + size/2, y + size/2 + 2, size/2 + 2, shadowPaint)
    
    // Draw avatar
    val rect = RectF(x, y, x + size, y + size)
    canvas.drawBitmap(avatarBitmap, null, rect, null)
  }
}
```

**Settings toggle (SettingsScreen):**
- New toggle: "Show Avatar on Wallpaper" (bool)
- Icon: Person icon
- Subtitle: "Display your avatar in corner"
- Default: ON

---

## Feature 8: Daily Progress Summary

### 8A. Dashboard Widget

**File to create/modify:** `feature/home/HomeScreen.kt`

**Add dashboard section at top (above main content):**

**Layout:**
- Title: "Today's Progress" (gray, secondary color, 14sp)
- 4-column grid:
  - Column 1: "Words Learned"
    - Icon: Book (Icons.Default.Book)
    - Count: "5" (large, primary color, 28sp)
    - Label: "Today" (small, secondary)
  
  - Column 2: "XP Earned"
    - Icon: Zap (Lightning, custom icon)
    - Count: "145 XP" (large, primary color, 28sp)
    - Label: "Today" (small, secondary)
  
  - Column 3: "Streak"
    - Icon: Flame (🔥 emoji or custom)
    - Count: "12" (large, warning color #FF9800, 28sp)
    - Label: "Days" (small, secondary)
  
  - Column 4: "Level"
    - Icon: Star (Icons.Default.Star), color = Gold
    - Count: "15" (large, gold, 28sp)
    - Label: "Title: 'The Eloquent'" (small, secondary)

- Container styling:
  - Each column: Rounded background (LexaColors.SurfaceDark), 12dp corners, 16dp padding
  - Tap feedback: Slight color change on press
  - Grid gap: 12dp

- Updated real-time:
  - After quiz completion: Update "Words Learned" and "XP Earned"
  - After streak update: Update "Streak"
  - After level-up: Update "Level"

**Data source:**
```kotlin
data class DailyProgressSnapshot(
  val wordsLearnedToday: Int,
  val xpEarnedToday: Int,
  val currentStreak: Int,
  val currentLevel: Int,
  val currentTitle: String
)

// Call this on home screen load and after quiz completion
fun getDailyProgress(context: Context): DailyProgressSnapshot {
  val db = DatabaseModule.getInstance(context)
  val today = LocalDate.now().toString()
  
  val wordsLearned = db.userProgressDao()
    .getProgressByLastAnsweredDate(today)
    .size
  
  val xpEarned = db.quizSessionDao()
    .getSessionsForDate(LocalDate.now())
    .sumOf { it.xpEarned }
  
  val streak = db.streakDao().getStreak().currentStreak
  val level = db.levelDao().getLevel()
  
  return DailyProgressSnapshot(
    wordsLearnedToday = wordsLearned,
    xpEarnedToday = xpEarned,
    currentStreak = streak,
    currentLevel = level.currentLevel,
    currentTitle = level.title
  )
}
```

---

### 8B. Progress Summary in Notifications

**Optional:** Include mini progress update in daily habit notification

**Notification body expansion (Android 10+):**
```
"📚 Today's Progress:
5 words learned | 145 XP earned | 12-day streak 🔥"
```

---

## Feature 9: Streak Freeze (Existing Feature - No Changes)

**Status:** Maintain existing implementation as-is.

**Note:** Wallpaper quiz answers do NOT affect streak. Streak Freeze remains insurance for missed main practice days.

---

## Database Schema Updates

### New Entities

**WallpaperQuizSession.kt:**
```kotlin
@Entity(tableName = "wallpaper_quiz_sessions")
data class WallpaperQuizSession(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val timestamp: String, // YYYY-MM-DD HH:mm:ss
  val wordId: String,
  val correct: Boolean,
  val xpEarned: Int
)
```

**BonusChallengeSession.kt:**
```kotlin
@Entity(tableName = "bonus_challenge_sessions")
data class BonusChallengeSession(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val timestamp: String,
  val challengeType: String,
  val completed: Boolean,
  val xpEarned: Int
)
```

**WordHuntSession.kt:**
```kotlin
@Entity(tableName = "word_hunt_sessions")
data class WordHuntSession(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val date: String, // YYYY-MM-DD
  val wordId: String,
  val found: Boolean,
  val xpEarned: Int
)
```

### New DAOs

**WallpaperQuizDao.kt:**
```kotlin
@Dao
interface WallpaperQuizDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: WallpaperQuizSession)
  
  @Query("SELECT COUNT(*) FROM wallpaper_quiz_sessions WHERE DATE(timestamp) = DATE('now')")
  suspend fun getTodayCount(): Int
  
  @Query("SELECT * FROM wallpaper_quiz_sessions WHERE DATE(timestamp) = :date ORDER BY timestamp DESC")
  suspend fun getSessionsForDate(date: String): List<WallpaperQuizSession>
}
```

**BonusChallengeDao.kt:**
```kotlin
@Dao
interface BonusChallengeDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: BonusChallengeSession)
  
  @Query("SELECT * FROM bonus_challenge_sessions WHERE DATE(timestamp) >= DATE('now', '-7 days') ORDER BY timestamp DESC LIMIT 1")
  suspend fun getLastCompletedInWeek(): BonusChallengeSession?
  
  @Query("SELECT COUNT(*) FROM bonus_challenge_sessions WHERE DATE(timestamp) = DATE('now') AND completed = 1")
  suspend fun getTodayCompletedCount(): Int
}
```

**WordHuntDao.kt:**
```kotlin
@Dao
interface WordHuntDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: WordHuntSession)
  
  @Query("SELECT * FROM word_hunt_sessions WHERE date = :date AND wordId = :wordId LIMIT 1")
  suspend fun getSessionForWordAndDate(wordId: String, date: String): WordHuntSession?
  
  @Query("SELECT * FROM word_hunt_sessions WHERE DATE(date) = DATE('now') AND found = 1")
  suspend fun getTodayFoundWords(): List<WordHuntSession>
}
```

### Database Migration

**Update LexaWallDatabase.kt:**
```kotlin
@Database(
  entities = [
    WordEntity::class,
    UserProgressEntity::class,
    LevelEntity::class,
    StreakEntity::class,
    BadgeEntity::class,
    QuizSessionEntity::class,
    WallpaperQuizSession::class,
    BonusChallengeSession::class,
    WordHuntSession::class
  ],
  version = 3
)
abstract class LexaWallDatabase : RoomDatabase() {
  abstract fun wallpaperQuizDao(): WallpaperQuizDao
  abstract fun bonusChallengeDao(): BonusChallengeDao
  abstract fun wordHuntDao(): WordHuntDao
  // ... existing DAOs ...
}
```

---

## PrefsManager Extensions

**Add methods to `PrefsManager.kt`:**

```kotlin
// Wallpaper Quiz
fun isWallpaperQuizOptedIn(context: Context): Boolean = 
  context.getSharedPreferences().getBoolean("wallpaper_quiz_opted_in", false)
fun saveWallpaperQuizOptedIn(context: Context, value: Boolean) =
  context.getSharedPreferences().edit().putBoolean("wallpaper_quiz_opted_in", value).apply()

fun getWallpaperQuizCountToday(context: Context): Int =
  context.getSharedPreferences().getInt("wallpaper_quiz_count_today", 0)
fun saveWallpaperQuizCountToday(context: Context, count: Int) =
  context.getSharedPreferences().edit().putInt("wallpaper_quiz_count_today", count).apply()

fun getLastWallpaperQuizTime(context: Context): Long =
  context.getSharedPreferences().getLong("last_wallpaper_quiz_time", 0)
fun saveLastWallpaperQuizTime(context: Context, time: Long) =
  context.getSharedPreferences().edit().putLong("last_wallpaper_quiz_time", time).apply()

// Habit Stacking
fun getHabitStackHour(context: Context): Int? {
  val value = context.getSharedPreferences().getInt("habit_stack_hour", -1)
  return if (value == -1) null else value
}
fun saveHabitStackHour(context: Context, hour: Int) =
  context.getSharedPreferences().edit().putInt("habit_stack_hour", hour).apply()

// Custom Image
fun getCustomImagePath(context: Context): String? =
  context.getSharedPreferences().getString("custom_image_path", null)
fun saveCustomImagePath(context: Context, path: String) =
  context.getSharedPreferences().edit().putString("custom_image_path", path).apply()

fun isCustomImageEnabled(context: Context): Boolean =
  context.getSharedPreferences().getBoolean("custom_image_enabled", false)
fun saveCustomImageEnabled(context: Context, enabled: Boolean) =
  context.getSharedPreferences().edit().putBoolean("custom_image_enabled", enabled).apply()

// Avatar
fun getSelectedAvatarOutfit(context: Context): String =
  context.getSharedPreferences().getString("selected_avatar_outfit", "base") ?: "base"
fun saveSelectedAvatarOutfit(context: Context, outfitId: String) =
  context.getSharedPreferences().edit().putString("selected_avatar_outfit", outfitId).apply()

fun showAvatarOnWallpaper(context: Context): Boolean =
  context.getSharedPreferences().getBoolean("show_avatar_on_wallpaper", true)
fun saveShowAvatarOnWallpaper(context: Context, show: Boolean) =
  context.getSharedPreferences().edit().putBoolean("show_avatar_on_wallpaper", show).apply()

// Bonus Challenge
fun getLastBonusChallengeCompletedDate(context: Context): String? =
  context.getSharedPreferences().getString("last_bonus_challenge_date", null)
fun saveLastBonusChallengeCompletedDate(context: Context, date: String) =
  context.getSharedPreferences().edit().putString("last_bonus_challenge_date", date).apply()
```

---

## Testing Requirements

### Unit Tests

**Test files to create:**
- `WallpaperQuizEngineTest.kt` — Trigger logic, timeout, daily count
- `BonusChallengeEngineTest.kt` — Eligibility, selection, frequency
- `CustomImageManagerTest.kt` — Compression, fallback, readability detection
- `AvatarEngineTest.kt` — Outfit unlocking, level progression
- `WordHuntOCRTest.kt` — Text detection, word matching

### Integration Tests

**Test scenarios:**
- [ ] Wallpaper quiz triggers at correct times
- [ ] XP awarded correctly for quiz answers
- [ ] Streak NOT affected by wallpaper quiz
- [ ] Bonus challenge appears only for active users
- [ ] Custom image loads and displays
- [ ] Avatar outfits unlock on schedule
- [ ] Daily summary updates in real-time
- [ ] Notifications respect 2-per-day limit
- [ ] OCR detects words in images
- [ ] Voice recognition produces feedback

### UI Tests (Screenshots)

- [ ] Wallpaper quiz overlay (correct/wrong states)
- [ ] Custom image with 30% overlay
- [ ] Avatar profile screen
- [ ] Habit stack dialog
- [ ] Word hunt camera view
- [ ] Daily progress dashboard
- [ ] Settings screens (wallpaper quiz, custom image, habit stack, avatar)

---

## Rollout & Monitoring

### Phase 1: Backend Setup (Week 1)
- [ ] Create all database entities & migrations
- [ ] Create all DAOs
- [ ] Update `LexaWallDatabase`
- [ ] Extend `PrefsManager`

### Phase 2: Wallpaper Quiz (Weeks 2-3)
- [ ] Implement `WallpaperQuizEngine`
- [ ] Create `WallpaperQuizOverlay`
- [ ] Add settings toggle
- [ ] Integrate XP recording
- [ ] Test trigger logic

### Phase 3: Notifications & Habits (Weeks 3-4)
- [ ] Implement `BonusChallengeEngine`
- [ ] Implement `HabitStackDialog`
- [ ] Set up `NotificationScheduler` extensions
- [ ] Add notification scheduling

### Phase 4: Customization (Weeks 4-5)
- [ ] Implement `CustomImageManager`
- [ ] Add image upload UI
- [ ] Render custom images in wallpaper
- [ ] Implement `AvatarEngine` & profile screen

### Phase 5: Advanced Features (Weeks 5-6)
- [ ] Implement speech recognition
- [ ] Implement OCR word hunt
- [ ] Create daily progress dashboard
- [ ] Add remaining avatar outfits

### Phase 6: Testing & Polish (Weeks 6-7)
- [ ] All unit tests
- [ ] All integration tests
- [ ] UI/UX polish
- [ ] Bug fixes
- [ ] Performance optimization

### Phase 7: Rollout (Week 7+)
- [ ] Beta testing with small user group
- [ ] Monitor crash reports
- [ ] Gather feedback
- [ ] Full release

---

## Success Metrics

After implementation, track:
- **Daily Active Users (DAU):** Should increase 15-25% within 2 weeks
- **Session Length:** Should increase 20-30% (more engagement)
- **Retention (Day 7, Day 30):** Should improve 10-15% points
- **Streak Maintenance:** Should improve by 5-10% (Freeze + Wallpaper quiz safety net)
- **Feature Adoption:** Track % of users enabling wallpaper quizzes, custom images, voice practice
- **XP Growth:** Users earning more XP via bonus challenges & wallpaper quizzes

---

**END OF BRIEF**

This document specifies EVERY detail. Implementation is not complete until ALL items above are delivered, tested, and integrated. Any missing feature or button will be considered a failure.

