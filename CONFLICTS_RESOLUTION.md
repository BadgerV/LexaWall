# LexaWall Engagement Features: Conflicts Resolution & Design Decisions

**Date:** March 2, 2026  
**Status:** All conflicts identified and resolved through iterative clarification

---

## Executive Summary

This document resolves all internal contradictions, ambiguities, and design conflicts identified in the proposed engagement features. Every decision prioritizes user retention while protecting core progression mechanics (themes, streaks, XP).

---

## Resolved Conflicts & Decisions

### 1. Notification Frequency & Burnout Prevention

**CONFLICT:** Multiple notification-heavy features (Interactive Quiz, Bonus Challenges, Habit-Stacking Prompts) could overwhelm users.

**DECISION:**
- **Maximum: 2 notifications per day**
- **Slot 1:** Notification Quiz (default) OR Wallpaper Quiz (if user opts in)
- **Slot 2:** Habit-Stacking Prompt (user-defined context reminder)
- **Bonus Challenges:** Randomly triggered within notification slots (intelligent targeting, no extra slots)
- **Wallpaper Quizzes:** Triggered via screen-on detection, no notification required

**Rationale:** Prevents notification fatigue while maintaining engagement diversity.

---

### 2. Interactive Quiz Notification vs. Wallpaper Quiz

**CONFLICT:** Should users interact via notification or wallpaper? Both? Neither?

**DECISION: Both, but user chooses one primary path**

**Path A: Notification Quiz (Default)**
- Daily push notification with quiz question
- User taps answer on lock screen
- Feedback shown immediately on notification
- **XP:** +10 (correct), no XP penalty (wrong → "Try the app for details")
- Does NOT affect streak

**Path B: Wallpaper Quiz (Opt-in)**
- Screen-ON detection triggers random timing
- Max 2 quizzes per day (screen-activity-based, not time-based)
- Quiz replaces wallpaper entirely
- User answers directly on wallpaper
- **XP:** +15 (correct), +1 (wrong with highlight)
- Does NOT affect streak
- Automatically disables Notification Quiz path (notification slot freed)

**Rationale:** Wallpaper quiz is more frictionless (no app launch) and rewards opt-in with higher XP. Both are bonus engagement, not core practice.

---

### 3. Wallpaper Quiz Trigger Timing

**CONFLICT:** How do "random times," "screen on," and "couple of rotations" coexist with variable rotation speeds?

**DECISION: Option B - Screen-Activity-Based Triggering**

**Logic:**
1. System detects screen ON event
2. Sets random delay (varies per trigger, no fixed schedule)
3. Waits for 2 wallpaper rotations to occur while screen remains on
4. If 5 minutes pass without 2 rotations completing → timeout, skip this trigger
5. Trigger next quiz max 5+ hours later (prevents clustering)
6. Maximum 2 quizzes per day

**Adaptive timing:**
- Rotation speed doesn't affect trigger logic
- Fast rotations (1 min) → quiz faster
- Slow rotations (10 min) → respects timeout safety net
- User changing rotation speed mid-wait: system recalculates dynamically

**Rationale:** Mimics the natural "unlocking phone" moment, maximizes context fit without being predictable.

---

### 4. Wallpaper Quiz XP & Streak Mechanics

**CONFLICT:** If wallpaper quiz awards XP and keeps streaks alive, does Streak Freeze become pointless?

**DECISION: Wallpaper quizzes are BONUS XP only**

**Streaks:**
- Maintained by completing **main daily practice quiz** (existing system)
- Wallpaper quizzes do NOT count toward streak maintenance
- Streak Freeze remains valuable insurance for missed main quiz days

**XP Distribution:**
- **Notification Quiz:** +10 XP (correct), +0 XP (wrong)
- **Wallpaper Quiz:** +15 XP (correct), +1 XP (wrong)
- **Main Quiz Session:** Existing XP calculation unchanged

**Why separate:**
- Preserves streak motivation (forces daily app engagement)
- Wallpaper quizzes feel like bonus rewards, not requirements
- Prevents casual users from gaming the system with just taps
- Main quiz remains the core learning activity

**Rationale:** Secondary engagement loop without undermining primary progression.

---

### 5. Bonus Challenge Quizzes & Notification Conflicts

**CONFLICT:** Time-limited challenges (e.g., "Weekend Double XP") conflict with random screen-based triggering.

**DECISION: Intelligently Random, Smart Targeting**

**Trigger Strategy:**
1. **Eligibility check:** Only trigger if user completed main quiz that day (active behavior signal)
2. **Frequency cap:** Maximum 1 bonus challenge per week per user
3. **Contextual timing windows:** Optional (e.g., "Weekend" challenges appear Sat/Sun; "Storm" challenges appear randomly)
4. **Notification integration:** Bonus challenge appears as **replacement or addition to Slot 2**, not a 3rd notification
5. **User notification:** "You've unlocked a Storm Challenge! +25 XP bonus available today"

**Avoidance rules:**
- Don't trigger if user just answered wallpaper quiz (prevent fatigue)
- Don't trigger if user already failed a challenge that day
- Skip users who disabled notifications

**Rationale:** Maximizes surprise/novelty (retention boost) without spamming; rewards active users.

---

### 6. Leaderboards & Backend Features

**CONFLICT:** Leaderboards require backend; current app is 100% local-first.

**DECISION: Skip leaderboards entirely for now**

**Reasoning:**
- Maintains local-first architecture
- Avoids authentication complexity
- Protects user privacy (no data collection)
- Reduces backend dependency

**Alternative (future):** Personal "milestones" screen showing user's own progress over time (no comparison needed).

---

### 7. Custom Background Images & Theme Value

**CONFLICT:** Allowing custom images could make unlockable themes feel pointless.

**DECISION: Parallel system (not replacement)**

**Implementation:**
- **Themes remain primary progression reward**
- **Custom images are optional secondary feature**
- Users choose: Active theme OR active custom image (not both simultaneously)
- When custom image is active → themes are grayed out but remain unlocked
- User can toggle back to themes anytime without losing progress

**Custom Image Specifications:**
- **Max size:** 2MB (auto-compressed to 1080×1920)
- **Overlay:** 30% black semi-transparent layer (ensures text readability)
- **Text color:** Auto-detect image brightness → white or black text for contrast
- **Fallback:** If image fails to load → seamlessly revert to active theme
- **Cache strategy:** Load once per session, lazy-load on demand
- **Quiz interaction:** Quiz replaces custom image with dark background (perfect legibility)

**Custom Font:**
- Allow users to choose from system fonts
- Default fallback if font unavailable
- Font selection affects word display only (not definitions/metadata)

**Rationale:** Customization feels personal without cannibalizing theme unlock motivation.

---

### 8. Voice Pronunciation Practice

**Status:** Included in scope, no conflicts identified.

**Implementation:**
- Android Speech Recognition API for voice input
- Feedback: "Good!" (correct pronunciation) or "Try again"
- Low-friction practice mode (optional, separate from main quiz)
- No XP penalty for mispronunciation (only positive feedback)

---

### 9. Habit-Stacking Prompts

**Status:** Included in scope, integrated into Slot 2 notifications.

**Implementation:**
- Settings screen: "What routine should trigger your daily word?"
- Options: Morning, Breakfast, Commute, Lunch, Afternoon Break, Evening, Bedtime
- User selects one; scheduled notification at that time
- Example: "Time to learn your word — happy breakfast time!"
- Can be toggled off or changed anytime

---

### 10. Real-World Word Hunt (OCR)

**Status:** Included in scope, moderate complexity.

**Implementation:**
- "Daily Challenge" mode: Find today's wallpaper word in real environment
- Camera UI with ML Kit text recognition
- Scan printed text (menus, signs, books, labels)
- App detects word → bonus +20 XP
- Max 1 completion per word per day (prevent abuse)
- Optional/gamified (not required for streak)

---

### 11. Streak Insurance (Freeze)

**Status:** Existing feature, no changes.

**Interaction with new features:**
- Freeze tokens can be earned via high-level achievements or purchased via ads
- Freeze protects main streak only (not affected by wallpaper quiz misses)
- Users understand: Freeze = safety net for main practice, not for wallpaper bonuses

---

### 12. Personal Profile & Avatar

**Status:** Included in scope, lower priority.

**Implementation:**
- Profile screen shows user avatar/character
- Avatar outfits unlock via streaks (7-day, 30-day, 100-day milestones)
- Avatar appears as small icon in corner of wallpaper (optional, toggleable)
- Customizable: Choose base avatar from 3–5 options
- Cosmetic only (no gameplay impact)

---

### 13. Daily Progress Summary

**Status:** Included in scope, low complexity.

**Implementation:**
- Dashboard on home screen showing:
  - Words learned today
  - Current streak count
  - XP earned today
  - Next level progress bar
- Visible on app launch
- Updates in real-time after quiz completion

---

## Notification Slot Architecture

### Slot 1: Quiz Interaction
- **If user opted into Wallpaper Quiz:** No notification (wallpaper handles it)
- **If user didn't opt in:** Notification Quiz +10 XP reward

### Slot 2: Context Trigger
- **Habit-Stacking Prompt:** User's chosen time (e.g., "Time for breakfast word!")
- **OR Bonus Challenge** (if triggered that day): "Storm Challenge unlocked! +25 XP"
- System prioritizes Habit-Stacking by default; Bonus Challenge replaces it on special days

---

## Data & Persistence Updates

**New fields to add to database:**
- `wallpaper_quiz_opted_in` (boolean)
- `custom_image_uri` (nullable, local file path)
- `custom_image_enabled` (boolean)
- `bonus_challenge_completed_today` (boolean, resets daily)
- `wallpaper_quiz_count_today` (integer, resets daily)
- `last_wallpaper_quiz_time` (timestamp)
- `habit_stack_time` (string, user's chosen time)
- `avatar_outfit_unlocked` (list of outfit IDs)
- `selected_avatar_outfit` (outfit ID)

---

## Summary Table: Feature Inclusion & Priority

| Feature | Include? | Priority | Notes |
|---------|----------|----------|-------|
| Wallpaper Quiz | ✅ Yes | **Critical** | 2 random/day, +15 XP bonus, opt-in |
| Bonus Challenge | ✅ Yes | **High** | Smart triggering, 1x/week, +25 XP |
| Habit-Stacking | ✅ Yes | **High** | Slot 2 notification, user-defined time |
| Custom Images | ✅ Yes | **Medium** | Parallel with themes, not replacement |
| Voice Practice | ✅ Yes | **Medium** | Optional mode, speech recognition |
| Word Hunt OCR | ✅ Yes | **Medium** | Daily challenge, +20 XP bonus |
| Leaderboards | ❌ No | — | Skip (backend complexity) |
| Avatar System | ✅ Yes | **Low** | Cosmetic rewards, outfit unlocks |
| Daily Summary | ✅ Yes | **Low** | Dashboard widget on home screen |
| Streak Freeze | ✅ Yes | **Maintain** | No changes to existing feature |

---

## Testing & Validation Checklist

- [ ] Wallpaper quiz triggers correctly after screen ON + 2 rotations
- [ ] Wallpaper quiz respects 5-minute timeout
- [ ] XP correctly distributed (+15 correct, +1 wrong)
- [ ] Quiz doesn't affect streak
- [ ] Custom image loads and displays with overlay
- [ ] Text contrast is sufficient on all custom images
- [ ] Quiz overlay replaces custom image (dark background only)
- [ ] Notifications never exceed 2 per day
- [ ] Bonus challenges trigger only for active users
- [ ] Habit-Stacking prompt appears at user's set time
- [ ] Theme unlock progression unaffected by custom images
- [ ] Voice recognition works with speech API
- [ ] OCR correctly detects word in real images
- [ ] Avatar outfits unlock and display correctly
- [ ] Daily summary updates in real-time

---

**Document Status:** Ready for agent implementation  
**All ambiguities resolved:** Yes  
**Conflicts remaining:** None identified

