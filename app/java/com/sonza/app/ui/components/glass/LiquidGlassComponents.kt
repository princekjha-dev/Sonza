package com.sonza.app.ui.components.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sonza.app.core.model.GlassStyle
import com.sonza.app.ui.components.avatar.AvatarRepository
import com.sonza.app.ui.theme.SonzaTypography

/**
 * 1. LiquidGlassCard — Interactive or static translucent card with dynamic depth.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LiquidGlassTokens.RadiusCard),
    style: GlassStyle? = null,
    tint: Color = Color.Unspecified,
    ambientTint: Color = Color.Unspecified,
    drawShadow: Boolean = true,
    drawRim: Boolean = true,
    elevation: Dp? = null,
    blurRadius: Dp? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val glassConfig = rememberLiquidGlassConfig()
    val effectiveStyle = style ?: glassConfig.style
    val effectiveTint = if (ambientTint != Color.Unspecified) ambientTint else tint
    val effectiveBlur = blurRadius?.value ?: glassConfig.blur
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) LiquidGlassTokens.CardHoverScale else 1f,
        animationSpec = LiquidGlassTokens.springInteractive(),
        label = "glassCardScale"
    )

    val clickModifier = if (onClick != null) {
        Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    } else Modifier

    LiquidGlassSurface(
        modifier = modifier.then(clickModifier),
        shape = shape,
        style = effectiveStyle,
        blurAmount = effectiveBlur,
        intensity = glassConfig.intensity,
        tint = effectiveTint,
        isDarkTheme = glassConfig.isDarkTheme,
        drawShadow = drawShadow,
        drawRim = drawRim,
        elevationOverride = elevation,
        content = content
    )
}

/**
 * 2. LiquidGlassButton — Spring-animated interactive glass pill.
 */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    text: String? = null,
    shape: Shape = RoundedCornerShape(LiquidGlassTokens.RadiusPill),
    tint: Color = Color.Unspecified,
    accentColor: Color = LiquidGlassTokens.AmbientCyan,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
) {
    val glassConfig = rememberLiquidGlassConfig()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) LiquidGlassTokens.PressScale else 1f,
        animationSpec = LiquidGlassTokens.springInteractive(),
        label = "glassBtnScale"
    )

    val activeTint = if (isPressed) {
        accentColor.copy(alpha = 0.25f)
    } else {
        tint
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
    ) {
        LiquidGlassSurface(
            shape = shape,
            style = glassConfig.style,
            tint = activeTint,
            isDarkTheme = glassConfig.isDarkTheme,
            drawShadow = true,
            drawRim = true,
            elevationOverride = if (isPressed) 4.dp else 12.dp
        ) {
            Row(
                modifier = Modifier.padding(contentPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (icon != null && text != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
                if (text != null) {
                    Text(
                        text = text,
                        style = SonzaTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}

/**
 * 3. LiquidGlassNavigation — Floating translucent navigation pill.
 */
@Composable
fun LiquidGlassNavigation(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LiquidGlassTokens.RadiusPill),
    tint: Color = Color.Unspecified,
    ambientTint: Color = Color.Unspecified,
    elevation: Dp? = null,
    blurRadius: Dp? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val glassConfig = rememberLiquidGlassConfig()
    val effectiveTint = if (ambientTint != Color.Unspecified) ambientTint else tint
    val effectiveBlur = blurRadius?.value ?: glassConfig.blur
    LiquidGlassSurface(
        modifier = modifier,
        shape = shape,
        style = glassConfig.style,
        blurAmount = effectiveBlur,
        tint = effectiveTint,
        isDarkTheme = glassConfig.isDarkTheme,
        drawShadow = true,
        drawRim = true,
        elevationOverride = elevation ?: 24.dp,
        content = content
    )
}

/**
 * 4. LiquidGlassSearch — Floating search input with dynamic blur and focus glow.
 */
@Composable
fun LiquidGlassSearch(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search songs, albums, artists...",
    accentColor: Color = LiquidGlassTokens.AmbientCyan,
    onFocusChanged: ((Boolean) -> Unit)? = null,
    onClear: (() -> Unit)? = null
) {
    val glassConfig = rememberLiquidGlassConfig()
    var isFocused by remember { mutableStateOf(false) }

    val focusScale by animateFloatAsState(
        targetValue = if (isFocused) 1.01f else 1f,
        animationSpec = LiquidGlassTokens.springInteractive(),
        label = "searchFocusScale"
    )

    val focusTint by animateColorAsState(
        targetValue = if (isFocused) accentColor.copy(alpha = 0.15f) else Color.Unspecified,
        animationSpec = tween(250),
        label = "searchFocusTint"
    )

    LiquidGlassSurface(
        modifier = modifier
            .scale(focusScale)
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        style = glassConfig.style,
        blurAmount = if (isFocused) glassConfig.blur * 1.25f else glassConfig.blur,
        tint = focusTint,
        isDarkTheme = glassConfig.isDarkTheme,
        drawShadow = true,
        drawRim = true,
        elevationOverride = if (isFocused) 18.dp else 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = if (isFocused) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = SonzaTypography.BodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focus ->
                            isFocused = focus.isFocused
                            onFocusChanged?.invoke(focus.isFocused)
                        },
                    textStyle = SonzaTypography.BodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(accentColor),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() })
                )
            }

            if (query.isNotEmpty() && onClear != null) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * 5. LiquidGlassPlayer — Floating glass container for player surfaces.
 */
@Composable
fun LiquidGlassPlayer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LiquidGlassTokens.RadiusCardLarge),
    tint: Color = Color.Unspecified,
    content: @Composable BoxScope.() -> Unit
) {
    val glassConfig = rememberLiquidGlassConfig()
    LiquidGlassSurface(
        modifier = modifier,
        shape = shape,
        style = glassConfig.style,
        blurAmount = glassConfig.blur * 1.2f,
        tint = tint,
        isDarkTheme = glassConfig.isDarkTheme,
        drawShadow = true,
        drawRim = true,
        elevationOverride = 30.dp,
        content = content
    )
}

/**
 * 6. LiquidGlassWidget — Container tailored for widget cards.
 */
@Composable
fun LiquidGlassWidget(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LiquidGlassTokens.RadiusCard),
    tint: Color = Color.Unspecified,
    content: @Composable BoxScope.() -> Unit
) {
    val glassConfig = rememberLiquidGlassConfig()
    LiquidGlassSurface(
        modifier = modifier,
        shape = shape,
        style = glassConfig.style,
        tint = tint,
        isDarkTheme = glassConfig.isDarkTheme,
        drawShadow = true,
        drawRim = true,
        elevationOverride = 16.dp,
        content = content
    )
}

/**
 * 7. LiquidGlassDialog — Frosted modal dialog overlay.
 */
@Composable
fun LiquidGlassDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LiquidGlassTokens.RadiusCardLarge),
    tint: Color = Color.Unspecified,
    content: @Composable BoxScope.() -> Unit
) {
    val glassConfig = rememberLiquidGlassConfig()
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            LiquidGlassSurface(
                modifier = modifier.fillMaxWidth(),
                shape = shape,
                style = glassConfig.style,
                blurAmount = glassConfig.blur,
                tint = tint,
                isDarkTheme = glassConfig.isDarkTheme,
                drawShadow = true,
                drawRim = true,
                elevationOverride = 36.dp,
                content = content
            )
        }
    }
}

/**
 * 8. LiquidGlassSheet — Translucent bottom sheet container.
 */
@Composable
fun LiquidGlassSheet(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(topStart = LiquidGlassTokens.RadiusSheet, topEnd = LiquidGlassTokens.RadiusSheet),
    tint: Color = Color.Unspecified,
    content: @Composable BoxScope.() -> Unit
) {
    val glassConfig = rememberLiquidGlassConfig()
    LiquidGlassSurface(
        modifier = modifier,
        shape = shape,
        style = glassConfig.style,
        blurAmount = glassConfig.blur,
        tint = tint,
        isDarkTheme = glassConfig.isDarkTheme,
        drawShadow = false,
        drawRim = true,
        content = content
    )
}

/**
 * 9. LiquidGlassControl — Circular glass media control button with spring press.
 */
@Composable
fun LiquidGlassControl(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    tint: Color = Color.Unspecified,
    iconTint: Color = Color.White,
    enabled: Boolean = true
) {
    val glassConfig = rememberLiquidGlassConfig()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.93f else 1f,
        animationSpec = LiquidGlassTokens.springInteractive(),
        label = "controlScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassSurface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            style = glassConfig.style,
            tint = tint,
            isDarkTheme = glassConfig.isDarkTheme,
            drawShadow = true,
            drawRim = true,
            elevationOverride = if (isPressed) 2.dp else 8.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = iconTint,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

/**
 * 10. LiquidGlassChip — Glass tag / filter chip with selection glow.
 */
@Composable
fun LiquidGlassChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = LiquidGlassTokens.AmbientCyan
) {
    val glassConfig = rememberLiquidGlassConfig()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = LiquidGlassTokens.springInteractive(),
        label = "chipScale"
    )

    val chipTint = if (selected) {
        accentColor.copy(alpha = 0.28f)
    } else {
        Color.Unspecified
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(LiquidGlassTokens.RadiusChip))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        LiquidGlassSurface(
            shape = RoundedCornerShape(LiquidGlassTokens.RadiusChip),
            style = glassConfig.style,
            tint = chipTint,
            isDarkTheme = glassConfig.isDarkTheme,
            drawShadow = selected,
            drawRim = true,
            elevationOverride = if (selected) 6.dp else 0.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = label,
                    style = SonzaTypography.LabelMedium.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * 11. LiquidGlassAvatar — Profile avatar framed with luminous glass rim.
 */
@Composable
fun LiquidGlassAvatar(
    avatarId: String = "avatar_1",
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    onClick: (() -> Unit)? = null
) {
    val glassConfig = rememberLiquidGlassConfig()
    val avatar = remember(avatarId) { AvatarRepository.getAvatarById(avatarId) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.94f else 1f,
        animationSpec = LiquidGlassTokens.springInteractive(),
        label = "avatarScale"
    )

    val clickMod = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .then(clickMod),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassSurface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            style = glassConfig.style,
            tint = LiquidGlassTokens.AmbientCyan.copy(alpha = 0.15f),
            isDarkTheme = glassConfig.isDarkTheme,
            drawShadow = true,
            drawRim = true,
            elevationOverride = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = avatar.drawableRes),
                    contentDescription = avatar.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * 12. LiquidGlassOrbitPanel — Conversational floating glass panel for Orbit AI Assistant.
 */
@Composable
fun LiquidGlassOrbitPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    tint: Color = LiquidGlassTokens.AmbientCyan.copy(alpha = 0.12f),
    content: @Composable BoxScope.() -> Unit
) {
    val glassConfig = rememberLiquidGlassConfig()
    LiquidGlassSurface(
        modifier = modifier,
        shape = shape,
        style = glassConfig.style,
        blurAmount = glassConfig.blur * 1.3f,
        tint = tint,
        isDarkTheme = glassConfig.isDarkTheme,
        drawShadow = true,
        drawRim = true,
        elevationOverride = 32.dp,
        content = content
    )
}
