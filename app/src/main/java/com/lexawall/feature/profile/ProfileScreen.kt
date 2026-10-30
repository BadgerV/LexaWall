package com.lexawall.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexawall.core.ui.LexaColors
import com.lexawall.core.ui.LexaType
import com.lexawall.core.ui.PlayfairDisplay
import com.lexawall.data.local.DatabaseModule
import com.lexawall.data.local.PrefsManager
import com.lexawall.domain.engine.AvatarEngine
import com.lexawall.domain.engine.AvatarOutfit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { DatabaseModule.getInstance(context) }

    var currentStreak by remember { mutableIntStateOf(0) }
    var totalXp by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(1) }
    var selectedOutfitId by remember { mutableStateOf(PrefsManager.getSelectedAvatarOutfit(context)) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val streak = db.streakDao().getStreak().currentStreak
            val levelEntity = db.levelDao().getLevel()
            currentStreak = streak
            totalXp = levelEntity.totalXpEarned
            level = levelEntity.currentLevel
        }
    }

    val unlocked = remember(currentStreak, totalXp, level) {
        AvatarEngine.getUnlockedOutfits(currentStreak, totalXp, level)
    }
    val locked = remember(currentStreak, totalXp, level) {
        AvatarEngine.getLockedOutfits(currentStreak, totalXp, level)
    }
    val selectedOutfit = remember(selectedOutfitId) { AvatarEngine.getOutfit(selectedOutfitId) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(LexaColors.BackgroundDark)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF666680),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your Avatar",
                    style = LexaType.headlineLarge.copy(fontFamily = PlayfairDisplay),
                    color = LexaColors.TextPrimary
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color(0xFF161622), CircleShape)
                        .border(1.dp, LexaColors.Primary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(selectedOutfit.iconResourceId),
                        contentDescription = selectedOutfit.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(120.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Lexa the Linguist",
                    style = LexaType.titleLarge.copy(fontFamily = PlayfairDisplay, fontStyle = FontStyle.Italic),
                    color = LexaColors.Primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = selectedOutfit.name,
                    style = LexaType.bodyMedium,
                    color = LexaColors.TextMuted
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ProfileStat("${currentStreak} Days 🔥", "Current streak")
                }
                Box(modifier = Modifier.weight(1f)) {
                    ProfileStat("${totalXp} XP 💎", "Total XP")
                }
                Box(modifier = Modifier.weight(1f)) {
                    ProfileStat("Level $level ⭐", "Level")
                }
            }
        }

        item {
            Text(
                text = "Unlocked Outfits",
                style = LexaType.titleMedium,
                color = LexaColors.TextPrimary
            )
        }

        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(unlocked, key = { it.id }) { outfit ->
                    OutfitCard(
                        outfit = outfit,
                        locked = false,
                        isSelected = outfit.id == selectedOutfitId,
                        requirement = AvatarEngine.requirementLabel(outfit.unlockedAt),
                        onClick = {
                            selectedOutfitId = outfit.id
                            PrefsManager.saveSelectedAvatarOutfit(context, outfit.id)
                        }
                    )
                }
            }
        }

        if (locked.isNotEmpty()) {
            item {
                Text(
                    text = "Coming Soon",
                    style = LexaType.titleMedium,
                    color = LexaColors.TextPrimary
                )
            }
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(locked, key = { it.id }) { outfit ->
                        OutfitCard(
                            outfit = outfit,
                            locked = true,
                            isSelected = false,
                            requirement = "Reach ${AvatarEngine.requirementLabel(outfit.unlockedAt)}",
                            onClick = {}
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(100.dp)) }
    }
}

@Composable
private fun ProfileStat(value: String, label: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .background(LexaColors.SurfaceDark, RoundedCornerShape(14.dp))
            .padding(vertical = 14.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = LexaType.titleMedium,
            color = LexaColors.Primary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = LexaType.bodySmall,
            color = LexaColors.TextMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OutfitCard(
    outfit: AvatarOutfit,
    locked: Boolean,
    isSelected: Boolean,
    requirement: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = LexaColors.SurfaceDark,
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                width = if (isSelected) 1.dp else 0.5.dp,
                color = if (isSelected) LexaColors.Primary else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(enabled = !locked) { onClick() }
            .padding(12.dp)
            .alpha(if (locked) 0.45f else 1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color.Black.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(outfit.iconResourceId),
                contentDescription = outfit.name,
                modifier = Modifier.size(56.dp)
            )
        }
        Text(
            text = outfit.name,
            style = LexaType.bodyMedium,
            color = LexaColors.TextPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (locked) requirement else AvatarEngine.requirementLabel(outfit.unlockedAt),
            style = LexaType.bodySmall,
            color = LexaColors.TextMuted,
            textAlign = TextAlign.Center
        )
        if (isSelected) {
            Text(
                text = "Selected",
                style = LexaType.bodySmall,
                color = LexaColors.Primary
            )
        }
    }
}
