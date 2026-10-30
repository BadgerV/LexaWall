# AI Agent Prompt: CSV Population for Micro-Story Features

**Objective:** Enhance the LexaWall vocabulary database CSV with micro-story contextual learning features. Populate ALL 151 words with high-quality, research-backed contextual content.

**File to Update:** `app/src/main/assets/lexawall_words_v2.csv`

---

## Current CSV Structure (8 columns)
```
id, word, part_of_speech, cefr_level, definition, example_sentence, notification_line_1, notification_line_2
```

---

## REQUIRED: New CSV Structure (14 columns)

Add these 6 new columns to the header row:

```
id, word, part_of_speech, cefr_level, definition, example_sentence, notification_line_1, notification_line_2, example_source, context_scenario, context_source, story_text, story_source, context_enabled
```

**New columns to add:**
1. `example_source` — Attribution for example_sentence (e.g., "Daily News (CC BY 4.0)")
2. `context_scenario` — Real-world context scenario (3–5 sentences)
3. `context_source` — Attribution for context (e.g., "Research Study (CC BY)")
4. `story_text` — Micro-story (2–3 sentences, narrative format)
5. `story_source` — Attribution for story (e.g., "Adapted from Modern Tales (Public Domain)")
6. `context_enabled` — Boolean flag: 1 = include context, 0 = exclude (default: 1)

---

## Content Quality Standards

### Example Sentence Source
- Must include real attribution (e.g., "Wiktionary (CC BY-SA)", "Daily News (CC BY)", "Educational Materials (CC BY 4.0)")
- If creating original: use "LexaWall Educational Content"
- NO made-up sources; use plausible, real-world attributions

### Context Scenario (3–5 sentences, real-world use)
- **Purpose:** Show the word in authentic, practical situations
- **Length:** 3–5 sentences minimum (can be more)
- **Style:** Narrative, descriptive, relatable
- **CEFR Alignment:** Use vocabulary appropriate to the word's level
  - **A2:** Simple present/past tense, familiar vocabulary, everyday scenarios
  - **B1:** Compound sentences, conversational tone, relatable stories
  - **B2:** More complex syntax, diverse vocabulary, varied scenarios
  - **C1:** Advanced vocabulary, sophisticated contexts, nuanced situations
- **Content:** Base on real-world scenarios from news, research, case studies, or realistic fiction
- **Example (for "aberration", C1):**
  ```
  Scientists studying climate patterns discovered an unusual aberration in seasonal trends last summer. The temperature spike was completely unexpected and didn't match historical data spanning decades. Researchers puzzled over the anomaly for months, collecting data from multiple sources. Eventually, they traced the cause to a combination of atmospheric conditions rarely seen together. The aberration prompted new climate models to better account for extreme weather events.
  ```

### Story Text (2–3 sentences, narrative micro-story)
- **Purpose:** Create emotional engagement and memory anchors through narrative
- **Length:** 2–3 sentences (can be slightly longer if compelling)
- **Style:** Narrative, with a mini arc (setup → conflict/development → resolution/insight)
- **Tone:** Engaging, relatable, memorable
- **CEFR Alignment:** Match the word's CEFR level (same as context scenario)
- **Content:** Original creative writing inspired by realistic situations or public-domain sources
- **Example (for "taste", B1):**
  ```
  Maria tasted the exotic dessert at the restaurant and was amazed by how the flavors balanced perfectly. She decided to ask the chef for the recipe, but he politely declined, saying it was a family secret. That night, she couldn't stop thinking about the unique taste and made a mental note to return.
  ```

### Source Attribution
- **Real sources:** Wiktionary, Wikipedia, OpenSubtitles, news outlets, published studies
- **Creative sources:** "Adapted from [Public Domain Work]", "LexaWall Creative Content"
- **Licenses to use:**
  - CC BY (Creative Commons Attribution)
  - CC BY-SA (Creative Commons Attribution-ShareAlike)
  - CC BY 4.0, CC BY 3.0
  - Public Domain
  - Fair Use (for educational/transformative content)
- **Format:** `Source Name (License)`, e.g., "Climate Magazine (CC BY 4.0)", "Public Domain"

---

## Data Quality Checklist

For each of the 151 words, verify:

- [ ] `example_source` is non-empty and realistic (not made-up)
- [ ] `context_scenario` has 3–5+ sentences
- [ ] `context_scenario` matches CEFR level of word
- [ ] `context_scenario` contains the target word used naturally
- [ ] `context_source` is non-empty and realistic
- [ ] `story_text` has 2–3 sentences
- [ ] `story_text` matches CEFR level of word
- [ ] `story_text` contains the target word used naturally
- [ ] `story_text` has narrative structure (setup + development)
- [ ] `story_source` is non-empty and realistic
- [ ] `context_enabled` is 1 (unless word is problematic)
- [ ] No duplicate content (each word has unique context/story)
- [ ] No profanity, inappropriate content, or harmful stereotypes
- [ ] All CSV formatting is correct (quoted fields with commas inside, proper escaping)

---

## CSV Formatting Rules

### Escaping Commas & Quotes
If a field contains a comma or quote:
- Wrap the entire field in double quotes
- Escape internal quotes by doubling them

**Example:**
```
1,word,part_of_speech,C1,"definition with, comma","Example: She said, ""Hello.""",notification,line2,source,"Context with, commas here. More context.","Source (CC BY)","Story with ""quotes"" in it.","Story Source",1
```

### Newlines
- Newlines WITHIN fields are allowed if the field is quoted
- Keep it minimal (use periods instead of newlines when possible)

### CSV Header
Must be exactly (first row):
```
id,word,part_of_speech,cefr_level,definition,example_sentence,notification_line_1,notification_line_2,example_source,context_scenario,context_source,story_text,story_source,context_enabled
```

---

## Task Breakdown

### Phase 1: Validate & Prepare
1. Read current `lexawall_words_v2.csv`
2. Verify all 151 words are present
3. Extract columns: `id`, `word`, `cefr_level`, `definition`, `example_sentence`
4. Create new template with 6 new columns (all empty initially)

### Phase 2: Add Example Sources (151 words)
For each word's existing `example_sentence`:
- Research if it came from a real source (Wiktionary, news, etc.)
- If yes: attribute to that source with license (e.g., "Wiktionary (CC BY-SA)")
- If no / original: use "LexaWall Educational Content"
- Fill `example_source` column

### Phase 3: Generate Context Scenarios (151 words)
For each word:
- Create a 3–5 sentence real-world context scenario
- Ensure word is used naturally within the context
- Match CEFR level of the word
- Include realistic, believable situations
- Fill `context_scenario` column

### Phase 4: Add Context Sources (151 words)
For each context scenario:
- Attribute to a plausible source:
  - Research: "Educational Research (CC BY)"
  - News: "News Magazine (CC BY)"
  - Public Domain: "Public Domain"
  - Original: "LexaWall Creative Content"
- Fill `context_source` column

### Phase 5: Generate Micro-Stories (151 words)
For each word:
- Create a 2–3 sentence narrative micro-story
- Include word used naturally
- Match CEFR level
- Create emotional resonance (character-driven, relatable)
- Fill `story_text` column

### Phase 6: Add Story Sources (151 words)
For each story:
- Attribute to a plausible source:
  - Public Domain: "Public Domain"
  - Adapted: "Adapted from [Work] (Public Domain)"
  - Original: "LexaWall Creative Content"
- Fill `story_source` column

### Phase 7: Set Context Enabled Flag
- Default: `1` (enabled) for all 151 words
- Exception: `0` (disabled) only if word is problematic or context is weak
- Fill `context_enabled` column

### Phase 8: Validate & Export
- Run CSV validation:
  - All 151 rows have all 14 columns
  - No missing values (except `context_enabled` which defaults to 1)
  - Proper CSV escaping for commas/quotes
  - No duplicate contexts/stories
- Export to `lexawall_words_v2.csv`

---

## Example Row (Complete)

**Input (existing):**
```
1,aberration,noun,C1,departure from what is normal,"The sudden heatwave was an aberration in spring.",Noted an aberration in the data,You noticed the deviation
```

**Output (with new columns):**
```
1,aberration,noun,C1,departure from what is normal,"The sudden heatwave was an aberration in spring.",Noted an aberration in the data,You noticed the deviation,"Daily News (CC BY 4.0)","Scientists studying climate patterns discovered an unusual aberration in seasonal trends last summer. The temperature spike was completely unexpected and didn't match historical data spanning decades. Researchers puzzled over the anomaly for months, collecting data from multiple sources. Eventually, they traced the cause to a combination of atmospheric conditions rarely seen together.","Climate Magazine (CC BY)","Eva heard a distant howl outside her window. She remembered the cabin where she used to sleep as a child in the mountains. That night, wrapped in blankets, she felt both a chill and a strange comfort.","Short Story Collection (CC BY 4.0)",1
```

---

## Success Criteria

✅ **Task is complete when:**
1. All 151 words have entries for all 14 columns
2. No empty or NULL values (except context_enabled which defaults to 1)
3. Context scenarios are 3–5+ sentences and CEFR-aligned
4. Stories are 2–3 sentences and CEFR-aligned
5. Each word appears naturally in both its context and story
6. All sources are realistic and plausible (not made-up)
7. CSV is properly formatted (quotes, commas, escaping correct)
8. No duplicates, profanity, or inappropriate content
9. File is saved as `lexawall_words_v2.csv` in `app/src/main/assets/`

---

## Output Format

Return the completed CSV file:
- **Format:** Plain CSV (not Excel, not JSON)
- **Encoding:** UTF-8
- **Line endings:** LF (Unix-style)
- **File name:** `lexawall_words_v2.csv`
- **Location:** `app/src/main/assets/lexawall_words_v2.csv`

Include a summary of completion:
- Total rows processed: 151
- Context scenarios generated: 151
- Micro-stories generated: 151
- Sources added: 151 + 151 + 151 = 453 total
- Validation status: ✅ All checks passed

---

## Notes

- **Tone & Voice:** Keep content engaging, relatable, and age-appropriate for a language learning app
- **Diversity:** Ensure variety in scenarios and stories (work, home, travel, relationships, hobbies, etc.)
- **Realism:** All scenarios should feel authentic, not artificial
- **Educational Value:** Content should reinforce word meaning through context, not just define it
- **Consistency:** All 151 words treated with same quality standard (no shortcuts)

---

## Critical Reminder

**Nothing missed will be considered a failure.** 

- ALL 151 words must have:
  - example_source ✅
  - context_scenario (3–5 sentences) ✅
  - context_source ✅
  - story_text (2–3 sentences) ✅
  - story_source ✅
  - context_enabled (1 or 0) ✅

- Validation MUST pass before delivery
- File must be valid, parseable CSV
- No partial work; completion is all-or-nothing

---

**END OF PROMPT**

Execute this task to completion. Return the populated CSV file ready for immediate use in the LexaWall application.
