package com.lexawall.domain.engine

import com.lexawall.R

data class AvatarOutfit(
    val id: String,
    val name: String,
    val description: String,
    val iconResourceId: Int,
    val unlockedAt: AvatarUnlockCondition
)

sealed class AvatarUnlockCondition {
    data object StartingOutfit : AvatarUnlockCondition()
    data class StreakDays(val days: Int) : AvatarUnlockCondition()
    data class XpThreshold(val xp: Int) : AvatarUnlockCondition()
    data class LevelReached(val level: Int) : AvatarUnlockCondition()
}

object AvatarEngine {
    // One unique visual asset per unlockable outfit (no asset reuse across tiers).
    val outfits: List<AvatarOutfit> = listOf(
        AvatarOutfit(
            id = "base",
            name = "Minimal Flight",
            description = "Clean minimal hummingbird mark",
            iconResourceId = R.drawable.avatar_svg_clean_1,
            unlockedAt = AvatarUnlockCondition.StartingOutfit
        ),
        AvatarOutfit(
            id = "scholar",
            name = "Treasure Crest",
            description = "Hummingbird and chest emblem",
            iconResourceId = R.drawable.avatar_svg_clean_2,
            unlockedAt = AvatarUnlockCondition.StreakDays(7)
        ),
        AvatarOutfit(
            id = "astronaut",
            name = "Ascendant Crest",
            description = "Premium hummingbird crest",
            iconResourceId = R.drawable.avatar_svg_clean_3,
            unlockedAt = AvatarUnlockCondition.XpThreshold(5000)
        )
    )

    fun getUnlockedOutfits(streakDays: Int, xp: Int, level: Int): List<AvatarOutfit> {
        return outfits.filter { isUnlocked(it, streakDays, xp, level) }
    }

    fun getLockedOutfits(streakDays: Int, xp: Int, level: Int): List<AvatarOutfit> {
        return outfits.filterNot { isUnlocked(it, streakDays, xp, level) }
    }

    fun getOutfit(id: String): AvatarOutfit {
        return outfits.firstOrNull { it.id == id } ?: outfits.first()
    }

    fun requirementLabel(condition: AvatarUnlockCondition): String {
        return when (condition) {
            AvatarUnlockCondition.StartingOutfit -> "Unlocked"
            is AvatarUnlockCondition.StreakDays -> "${condition.days}-day streak"
            is AvatarUnlockCondition.XpThreshold -> "${condition.xp} XP"
            is AvatarUnlockCondition.LevelReached -> "Level ${condition.level}"
        }
    }

    private fun isUnlocked(
        outfit: AvatarOutfit,
        streakDays: Int,
        xp: Int,
        level: Int
    ): Boolean {
        return when (val unlock = outfit.unlockedAt) {
            AvatarUnlockCondition.StartingOutfit -> true
            is AvatarUnlockCondition.StreakDays -> streakDays >= unlock.days
            is AvatarUnlockCondition.XpThreshold -> xp >= unlock.xp
            is AvatarUnlockCondition.LevelReached -> level >= unlock.level
        }
    }
}
