package com.sonza.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.sonza.app.core.model.BrowseCategory
import com.sonza.app.ui.theme.MotionTokens
import com.sonza.app.ui.theme.RadiusTokens
import com.sonza.app.ui.theme.SonzaColors
import com.sonza.app.ui.theme.SonzaSurfaceVariant
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens
import com.sonza.app.ui.utils.DiscoveryArtRegistry

/**
 * Image-driven music discovery card inspired by modern editorial music apps.
 * Features full-bleed artwork, vibrant duotone color wash, protective scrim gradient,
 * bold typography directly over the image, and 0.97 tap bounce feedback.
 */
@Composable
fun BrowseCategoryCard(
    category: BrowseCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHero: Boolean = false
) {
    val meta = DiscoveryArtRegistry.getDiscoveryMeta(category.title)
    val imageUrl = category.thumbnailUrl?.takeIf { it.isNotBlank() } ?: meta.imageUrl
    val tintLong = category.color ?: meta.tintColor
    val tintColor = Color(tintLong or 0xFF000000)

    val cardShape = RoundedCornerShape(RadiusTokens.Lg)
    val aspectRatio = if (isHero) 2.1f else 1.55f

    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .clip(cardShape)
            .background(SonzaSurfaceVariant)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = "${category.title} music discovery category"
            }
    ) {
        // Base Dynamic Background Gradient (shown immediately while image loads or if offline)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            tintColor.copy(alpha = 0.85f),
                            tintColor.copy(alpha = 0.65f),
                            SonzaColors.Surface
                        )
                    )
                )
        )

        // Full-bleed Artwork Image
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.15f),
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center)
            )
        }

        // Duotone Vibrant Color Tint Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            tintColor.copy(alpha = 0.55f),
                            tintColor.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        // Dark Protective Gradient Scrim for WCAG AA Typography Legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.65f)
                        )
                    )
                )
        )

        // Category / Genre Title
        Text(
            text = category.title,
            style = SonzaTypography.TitleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (isHero) 17.sp else 15.sp,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.55f),
                    blurRadius = 8f
                )
            ),
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = SpacingTokens.SpaceMd,
                    end = SpacingTokens.SpaceMd,
                    bottom = SpacingTokens.SpaceMd,
                    top = SpacingTokens.SpaceSm
                )
        )
    }
}
