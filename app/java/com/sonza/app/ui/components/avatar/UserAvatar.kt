package com.sonza.app.ui.components.avatar

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.remember
import com.sonza.app.ui.components.glass.LiquidGlassTokens
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.sonza.app.R
import com.sonza.app.ui.components.sonza.SonzaDesignTokens
import com.sonza.app.ui.components.sonza.SonzaIconButton
import com.sonza.app.ui.theme.SonzaBrandAccent
import com.sonza.app.ui.theme.SonzaTypography

/**
 * Avatar entity model.
 */
data class UserAvatar(
    val id: String,
    val name: String,
    @DrawableRes val drawableRes: Int
)

/**
 * Single shared source of truth for all available 3D Memoji avatars.
 */
object AvatarRepository {
    val ALL_AVATARS: List<UserAvatar> = listOf(
        UserAvatar("avatar_1", "Double Buns", R.drawable.avatar_1),
        UserAvatar("avatar_2", "Blue Fedora", R.drawable.avatar_2),
        UserAvatar("avatar_3", "Purple Hijab", R.drawable.avatar_3),
        UserAvatar("avatar_4", "Dreadlocks", R.drawable.avatar_4),
        UserAvatar("avatar_5", "Blonde Shades", R.drawable.avatar_5),
        UserAvatar("avatar_6", "Freckles", R.drawable.avatar_6),
        UserAvatar("avatar_7", "Green Shades", R.drawable.avatar_7),
        UserAvatar("avatar_8", "Fedora Hat", R.drawable.avatar_8),
        UserAvatar("avatar_9", "Red Beanie", R.drawable.avatar_9),
        UserAvatar("avatar_10", "Pink Topknot", R.drawable.avatar_10),
        UserAvatar("avatar_11", "Afro Curls", R.drawable.avatar_11),
        UserAvatar("avatar_12", "Yellow Turban", R.drawable.avatar_12)
    )

    val DEFAULT_AVATAR: UserAvatar = ALL_AVATARS.first()

    /**
     * Resolves avatar by ID with safe automatic fallback if missing or corrupted.
     */
    fun getAvatarById(id: String?): UserAvatar {
        if (id.isNullOrBlank()) return DEFAULT_AVATAR
        return ALL_AVATARS.find { it.id.equals(id, ignoreCase = true) } ?: DEFAULT_AVATAR
    }
}

/**
 * Reusable Avatar component for rendering user avatar in consistent sizes across all surfaces
 * with physical Liquid Glass framing, specular highlight, and spring press micro-interaction.
 */
@Composable
fun AvatarView(
    avatarId: String?,
    modifier: Modifier = Modifier,
    size: Dp = 84.dp,
    showBorder: Boolean = true,
    borderColor: Color = SonzaBrandAccent,
    backgroundColor: Color = Color(0xFF1E202B),
    onClick: (() -> Unit)? = null
) {
    val avatar = AvatarRepository.getAvatarById(avatarId)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.92f else 1f,
        animationSpec = LiquidGlassTokens.Springs.Interactive,
        label = "avatarPressScale"
    )

    val rimBrush = Brush.sweepGradient(
        listOf(
            Color.White.copy(alpha = 0.50f),
            borderColor.copy(alpha = 0.70f),
            Color.White.copy(alpha = 0.15f),
            borderColor.copy(alpha = 0.70f),
            Color.White.copy(alpha = 0.50f)
        )
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(pressScale)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (showBorder) {
                    Modifier.border(2.dp, rimBrush, CircleShape)
                } else Modifier
            )
            .then(
                if (onClick != null) {
                    Modifier
                        .semantics {
                            this.contentDescription = "User profile: ${avatar.name}"
                            this.role = Role.Button
                        }
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            role = Role.Button,
                            onClickLabel = "Change profile avatar",
                            onClick = onClick
                        )
                } else {
                    Modifier.semantics {
                        this.contentDescription = "User avatar: ${avatar.name}"
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = avatar.drawableRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        )

        // Subtle specular highlight on upper quadrant of glass lens
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.White.copy(alpha = 0.14f),
                        0.45f to Color.Transparent
                    )
                )
        )
    }
}

/**
 * Modern modal sheet for selecting an avatar from the reference grid.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarSelectionSheet(
    isOpen: Boolean,
    selectedAvatarId: String,
    onAvatarSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF13141C),
        contentColor = Color.White,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Title + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Choose Your Avatar",
                        style = SonzaTypography.TitleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Select an avatar for your Sonza profile and widgets",
                        style = SonzaTypography.BodyMedium.copy(
                            fontSize = 13.sp,
                            color = SonzaDesignTokens.Colors.TextSecondary
                        )
                    )
                }

                SonzaIconButton(
                    icon = Icons.Rounded.Close,
                    contentDescription = "Close",
                    onClick = onDismiss,
                    size = 32.dp,
                    iconSize = 16.dp,
                    backgroundColor = Color.White.copy(alpha = 0.08f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4-column avatar selection grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AvatarRepository.ALL_AVATARS, key = { it.id }) { avatar ->
                    val isSelected = avatar.id.equals(selectedAvatarId, ignoreCase = true)
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.05f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.7f),
                        label = "avatarScale"
                    )

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .scale(scale)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) {
                                    SonzaBrandAccent.copy(alpha = 0.16f)
                                } else {
                                    Color.White.copy(alpha = 0.05f)
                                }
                            )
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) SonzaBrandAccent else Color.White.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .semantics {
                                this.role = Role.RadioButton
                                this.selected = isSelected
                                this.contentDescription = "${avatar.name}, ${if (isSelected) "selected" else "available"}"
                            }
                            .clickable(
                                role = Role.RadioButton,
                                onClickLabel = "Select ${avatar.name}"
                            ) {
                                onAvatarSelected(avatar.id)
                                onDismiss()
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = avatar.drawableRes),
                            contentDescription = avatar.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(SonzaBrandAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
