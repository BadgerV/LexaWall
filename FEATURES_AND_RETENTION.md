# LexaWall Features & Retention Analysis

This document catalogs every notable feature present in the LexaWall codebase and explains how it works. Each feature is also assigned a **retention score** on a scale of 1–10, where higher values indicate features most likely to make users stick around.

---

## 1. Onboarding

| Feature | Description | Retention Score |
|---------|-------------|-----------------|
| Splash screen | Delays entry while checking onboarding state; smooth visual introduction. | 2 |
| Wallpaper prompt | Encourages users to set LexaWall as live wallpaper. Heavily emphasised with vibrant UI and a subtle "skip" link. | 8 |
| Setup screen | Launches system picker, listens for `ACTION_WALLPAPER_CHANGED`, polls state, and saves preference. Corrects Android 13 export bug. | 9 |
| Calibration quiz | Five quick multiple‑choice questions to determine user CEFR level (A2–C1). Shuffles choices, stores result in prefs. | 6 |
| Notification permission | Asks for POST_NOTIFICATIONS (Android T+); records onboarding completion. | 7 |

> **Retention note:** The onboarding sequence is the first impression. The wallpaper and quiz steps are the two biggest hooks.

## 2. Wallpaper Engine

| Feature | Description | Score |
|---------|-------------|-------|
| Live wallpaper service | `LexaWallWallpaperService` with `Engine` subclass for canvas rendering. | 8 |
| Gradient backgrounds | Ten unlockable color themes, gradient rendering on canvas. | 7 |
| Word + metadata layout | Daily word, part‑of‑speech, definition, example sentence, tap→detail deep‑link. | 9 |
| Rotation timer | Configurable 1 / 5 / 10 min intervals; reads prefs each cycle so speed changes take effect instantly. | 7 |
| Ambient/AOD support | `supportsAmbientMode=true` plus screen off receiver; draws simplified black/word view on Always‑On Display. | 5 |
| Theme unlock levels | Themes tied to progression levels; encourages progression to unlock new looks. | 6 |
| Wallpaper state sync | Utilities to verify active wallpaper with OEM‑tolerant checks; used in onboarding/settings. | 8 |

> **Retention note:** A live wallpaper that changes automatically and looks nice keeps the app front‑and‑center every time the user unlocks their phone.

## 3. Word Discovery & Reference

| Feature | Description | Score |
|---------|-------------|-------|
| Wander list | "Wander" screen shows a daily list of words; lazy‑loaded, date header, mascot animation. | 6 |
| Word detail pages | Full definition, IPA, example, CEFR level, speech playback via `LexaSpeaker`. | 8 |
| Search (if present) | [may exist elsewhere; not inspected]. | — |

> **Retention note:** Browsing and exploring words is the core content. Ease of access and rich detail make users return.

## 4. Quiz & Practice

| Feature | Description | Score |
|---------|-------------|-------|
| Quiz engine | Spaced‑repetition algorithm, wrong‑answer tracking, word graduation. | 9 |
| Question/answer UI | Multiple‑choice screens with correct/wrong feedback, final summary. | 8 |
| Redemption quiz | (implied) gives second chance on wrong answers. | 5 |
| Quiz result screens | `QuizCorrectScreen`, `QuizWrongScreen`, `QuizEndScreen` drive feedback loops. | 7 |
| XP/streak updates | XP calculation, streak engine, level‑up events, badges. | 9 |
| Milestones | `LevelCompletionScreen`, `Day30SummaryScreen`, `GraduationScreen` with celebratory animations. | 8 |
| Competitive mode | Faster cards; one session before Word Drop; toggle in settings. | 4 |

> **Retention note:** Practice sessions with visible progress (XP, levels, streaks) are the strongest retention drivers in the app.

## 5. Progress & Rewards

| Feature | Description | Score |
|---------|-------------|-------|
| XP engine | Levels, titles, XP totals persisted in database. | 8 |
| Streaks & badges | Daily streak tracking, badge entities stored locally. | 7 |
| Quest/milestone screens | Visual feedback when milestones are reached. | 6 |
| Word graduation | When spaced‑rep review completes, word leaves rotation and user sees reward. | 7 |

> **Retention note:** Gamification elements (XP, streaks, badges) motivate daily return.

## 6. Notifications & Scheduling

| Feature | Description | Score |
|---------|-------------|-------|
| Daily push notifications | Scheduled via `NotificationScheduler` / `LexaWallNotificationWorker` to deliver word alerts. | 8 |
| Opt‑in during onboarding; toggle in settings. | 7 |

> **Retention note:** Notifications bring the app back into the user's attention on a regular cadence.

## 7. Settings & Customization

| Feature | Description | Score |
|---------|-------------|-------|
| Theme picker | Choose active wallpaper theme; unlocks based on progression. | 6 |
| Rotation speed dialog | Change wallpaper update interval. | 5 |
| Wallpaper setup row | Shortcut to launch system picker if not yet set. | 6 |
| Notification toggle | Turn off/on daily notifications. | 5 |
| Competitive mode toggle | Enable/disable faster cards. | 4 |
| Display current level & version info. | 3 |

> **Retention note:** Settings support personalization and remove friction; important but lower retention weight.

## 8. Data & Persistence

| Feature | Description | Score |
|---------|-------------|-------|
| Local database | Room entities for words, progress, levels, streaks, badges, quiz sessions. | 7 |
| `PrefsManager` | SharedPreferences helper storing small flags (wallpaper, theme, speed, competitive mode, onboarding). | 5 |

> **Retention note:** Robust persistence ensures continuity and encourages habit formation.

## 9. Auxiliary Utilities

- `X pEngine`, `StreakEngine`, `SpacedRepetitionEngine` – core algorithms powering progression.
- `LexaWallLogger` – logging utility for debugging.
- `LexaSpeaker` – TTS for word pronunciation.

These support other features rather than being exposed directly to users; their retention impact is indirect.

---

## Retention Scale Summary

### High‑impact (8–10)
1. Live wallpaper with rotating word and detection logic
2. Quiz & spaced‑repetition engine with XP/streak/leveling
3. Onboarding wallpaper prompt & setup mechanism
4. Daily notifications
5. Word detail pages with audio
6. Wallpaper state utilities to guarantee global consistency

### Medium‑impact (5–7)
- Calibration quiz
- Theme unlock system
- Wander browsing
- Milestone screens (level, 30‑day summary, graduation) 
- Notification opt‑in toggle
- Settings options for theme/speed
- Persistence (database & prefs)

### Low‑impact (1–4)
- Splash screen
- Competitive mode toggle
- App version display
- Miscellaneous UI polish (animations)

---

> **Conclusion:** The codebase is rich in user‑facing features, many of which are geared toward daily engagement and habit formation. The live wallpaper and quiz mechanics form the core retention engines; secondary features like notifications and themes support and enhance that experience.

_Last generated: March 2 2026._
