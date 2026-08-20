package com.sonza.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.theme.MotionTokens
import com.sonza.app.ui.theme.RadiusTokens
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant
import com.sonza.app.ui.theme.SonzaOutline
import com.sonza.app.ui.theme.SonzaSurfaceVariant
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens

/**
 * Mood / Genre Pills per DESIGN_SYSTEM.md Part 6.2 & Part 7:
 * - Horizontal scroll, radius-pill (999dp), space-sm internal vertical padding.
 * - Material Symbols Rounded icons at 20dp (zero emojis).
 * - Selected: filled dynamic accent background, on-accent text & icon.
 * - Unselected: surface-variant background, on-surface-variant text & icon, outline border.
 * - Part 8 tap feedback: scale 0.97, 100ms.
 */
private data class Mood(val name: String, val icon: ImageVector)

private val MOODS = listOf(
    Mood("All",       Icons.Rounded.Whatshot),
    Mood("Relax",     Icons.Rounded.Spa),
    Mood("Feel Good", Icons.Rounded.AutoAwesome),
    Mood("Energize",  Icons.Rounded.Bolt),
    Mood("Focus",     Icons.Rounded.Psychology),
    Mood("Romance",   Icons.Rounded.Favorite),
    Mood("Sleep",     Icons.Rounded.Bedtime),
    Mood("Sad",       Icons.Rounded.WaterDrop),
    Mood("Party",     Icons.Rounded.Celebration)
)

@Composable
fun MoodChipsSection(
    selectedMood: String?,
    onMoodSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceSm)
    ) {
        itemsIndexed(MOODS, key = { _, m -> m.name }) { _, mood ->
            MoodChip(
                mood = mood,
                isSelected = (selectedMood == null && mood.name == "All") || mood.name == selectedMood,
                onClick = { onMoodSelected(if (mood.name == "All") "" else mood.name) }
            )
        }
    }
}

@Composable
private fun MoodChip(
    mood: Mood,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val dynamicColors = LocalSonzaDynamicColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) MotionTokens.CardTapScale else 1f,
        animationSpec = tween(
            durationMillis = MotionTokens.CardTapDuration,
            easing = FastOutSlowInEasing
        ),
        label = "moodChipPress"
    )

    val pillShape = RoundedCornerShape(RadiusTokens.Pill)

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) dynamicColors.accent else SonzaSurfaceVariant,
        animationSpec = tween(MotionTokens.NavSelectionDuration, easing = FastOutSlowInEasing),
        label = "chipBg"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) dynamicColors.onAccent else SonzaOnSurfaceVariant,
        animationSpec = tween(MotionTokens.NavSelectionDuration, easing = FastOutSlowInEasing),
        label = "chipContentColor"
    )

    Box(
        modifier = Modifier
            .scale(pressScale)
            .clip(pillShape)
            .background(backgroundColor)
            .then(
                if (!isSelected) {
                    Modifier.border(width = 1.dp, color = SonzaOutline, shape = pillShape)
                } else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceSm),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = mood.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(SpacingTokens.SpaceSm))
            Text(
                text = mood.name,
                style = SonzaTypography.LabelLarge.copy(
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = contentColor
            )
        }
    }
}

