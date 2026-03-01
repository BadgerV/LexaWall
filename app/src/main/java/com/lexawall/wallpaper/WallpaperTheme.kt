package com.lexawall.wallpaper

data class LexaTheme(
    val key: String,
    val displayName: String,
    val backgroundTop: Long,
    val backgroundBottom: Long,
    val accentColor: Long,
    val accentColorDim: Long,
    val surfaceColor: Long,
    val unlockLevel: Int
)

object LexaThemes {
    val Midnight = LexaTheme(
        key = "midnight",
        displayName = "Midnight",
        backgroundTop = 0xFF080812L,
        backgroundBottom = 0xFF10100AL,
        accentColor = 0xFFC9A74AL,
        accentColorDim = 0xFF8A7040L,
        surfaceColor = 0xFF12121CL,
        unlockLevel = 1
    )

    val Savanna = LexaTheme(
        key = "savanna",
        displayName = "Savanna",
        backgroundTop = 0xFF100C06L,
        backgroundBottom = 0xFF0A0806L,
        accentColor = 0xFFD4A853L,
        accentColorDim = 0xFF8A6E35L,
        surfaceColor = 0xFF1A1208L,
        unlockLevel = 6
    )

    val Arctic = LexaTheme(
        key = "arctic",
        displayName = "Arctic",
        backgroundTop = 0xFF060C12L,
        backgroundBottom = 0xFF08101AL,
        accentColor = 0xFF7BAFD4L,
        accentColorDim = 0xFF4A7A99L,
        surfaceColor = 0xFF0C1420L,
        unlockLevel = 2
    )

    val Forest = LexaTheme(
        key = "forest",
        displayName = "Forest",
        backgroundTop = 0xFF060E08L,
        backgroundBottom = 0xFF0A1208L,
        accentColor = 0xFF7BAF8CL,
        accentColorDim = 0xFF4A7A5CL,
        surfaceColor = 0xFF0C1A10L,
        unlockLevel = 4
    )

    val Crimson = LexaTheme(
        key = "crimson",
        displayName = "Crimson",
        backgroundTop = 0xFF120608L,
        backgroundBottom = 0xFF0A0606L,
        accentColor = 0xFFBF4040L,
        accentColorDim = 0xFF8A2A2AL,
        surfaceColor = 0xFF1A0C0CL,
        unlockLevel = 3
    )

    val Jade = LexaTheme(
        key = "jade",
        displayName = "Jade",
        backgroundTop = 0xFF061008L,
        backgroundBottom = 0xFF040C06L,
        accentColor = 0xFF40BF80L,
        accentColorDim = 0xFF2A8A58L,
        surfaceColor = 0xFF0C1A12L,
        unlockLevel = 5
    )

    val Dusk = LexaTheme(
        key = "dusk",
        displayName = "Dusk",
        backgroundTop = 0xFF0C0A14L,
        backgroundBottom = 0xFF080812L,
        accentColor = 0xFF9B8FBFL,
        accentColorDim = 0xFF6B608AL,
        surfaceColor = 0xFF141220L,
        unlockLevel = 7
    )

    val Obsidian = LexaTheme(
        key = "obsidian",
        displayName = "Obsidian",
        backgroundTop = 0xFF080808L,
        backgroundBottom = 0xFF040404L,
        accentColor = 0xFFE8E8E8L,
        accentColorDim = 0xFF999999L,
        surfaceColor = 0xFF141414L,
        unlockLevel = 6
    )

    val Storm = LexaTheme(
        key = "storm",
        displayName = "Storm",
        backgroundTop = 0xFF060A14L,
        backgroundBottom = 0xFF04080EL,
        accentColor = 0xFF4A7FCCL,
        accentColorDim = 0xFF2A508AL,
        surfaceColor = 0xFF0A1020L,
        unlockLevel = 7
    )

    val Rust = LexaTheme(
        key = "rust",
        displayName = "Rust",
        backgroundTop = 0xFF140A06L,
        backgroundBottom = 0xFF0A0604L,
        accentColor = 0xFFCC6633L,
        accentColorDim = 0xFF8A4422L,
        surfaceColor = 0xFF1E1008L,
        unlockLevel = 8
    )

    val Ember = LexaTheme(
        key = "ember",
        displayName = "Ember",
        backgroundTop = 0xFF140806L,
        backgroundBottom = 0xFF0A0604L,
        accentColor = 0xFFFF6B35L,
        accentColorDim = 0xFFBF4A1AL,
        surfaceColor = 0xFF1E100AL,
        unlockLevel = 9
    )

    val Void = LexaTheme(
        key = "void",
        displayName = "Void",
        backgroundTop = 0xFF060408L,
        backgroundBottom = 0xFF040206L,
        accentColor = 0xFF8B5CF6L,
        accentColorDim = 0xFF5A3AAAL,
        surfaceColor = 0xFF100C18L,
        unlockLevel = 10
    )

    val all: List<LexaTheme> = listOf(
        Midnight,
        Savanna,
        Arctic,
        Forest,
        Crimson,
        Jade,
        Dusk,
        Obsidian,
        Storm,
        Rust,
        Ember,
        Void
    )

    private val byKey: Map<String, LexaTheme> = all.associateBy { it.key }

    fun fromKey(key: String?): LexaTheme {
        val safeKey = key?.trim()?.lowercase().orEmpty()
        return byKey[safeKey] ?: Midnight
    }

    fun gradientForWordId(wordId: String?, activeTheme: LexaTheme): GradientBackground {
        val mixSeed = (wordId?.toIntOrNull() ?: wordId?.hashCode() ?: 0).toUInt().toLong()
        val shift = (mixSeed % 7L).toInt()
        val top = darken(activeTheme.backgroundTop, shift)
        val bottom = darken(activeTheme.backgroundBottom, shift + 2)
        return GradientBackground(topColor = top, bottomColor = bottom)
    }

    private fun darken(color: Long, amount: Int): Long {
        val a = (color shr 24) and 0xFF
        val r = ((color shr 16) and 0xFF).toInt()
        val g = ((color shr 8) and 0xFF).toInt()
        val b = (color and 0xFF).toInt()
        val factor = (1f - (amount.coerceIn(0, 12) * 0.02f)).coerceAtLeast(0.75f)
        val nr = (r * factor).toInt().coerceIn(0, 255)
        val ng = (g * factor).toInt().coerceIn(0, 255)
        val nb = (b * factor).toInt().coerceIn(0, 255)
        return (a shl 24) or (nr.toLong() shl 16) or (ng.toLong() shl 8) or nb.toLong()
    }
}

data class GradientBackground(
    val topColor: Long,
    val bottomColor: Long
)
