package com.sonza.app.ui.components.sonza

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonza.app.ui.theme.SonzaTypography

/**
 * Premium Loading State.
 */
@Composable
fun SonzaLoadingState(
    modifier: Modifier = Modifier,
    message: String? = null,
    color: Color = SonzaDesignTokens.Colors.BrandAccent
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = color,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
            if (!message.isNullOrBlank()) {
                Text(
                    text = message,
                    style = SonzaTypography.BodyMedium,
                    color = SonzaDesignTokens.Colors.TextSecondary
                )
            }
        }
    }
}

/**
 * Premium Empty State with icon, title, description, and optional action button.
 */
@Composable
fun SonzaEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector = Icons.Rounded.MusicOff,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SonzaDesignTokens.Colors.TextTertiary,
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = SonzaTypography.SectionTitle.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = SonzaDesignTokens.Colors.TextPrimary,
                textAlign = TextAlign.Center
            )
            if (!description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = SonzaTypography.BodyMedium,
                    color = SonzaDesignTokens.Colors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            if (!actionText.isNullOrBlank() && onActionClick != null) {
                Spacer(modifier = Modifier.height(20.dp))
                SonzaButton(
                    text = actionText,
                    onClick = onActionClick,
                    style = SonzaButtonStyle.Filled
                )
            }
        }
    }
}

/**
 * Premium Error State with retry option.
 */
@Composable
fun SonzaErrorState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    onRetry: (() -> Unit)? = null,
    retryText: String = "Try Again"
) {
    SonzaEmptyState(
        title = title,
        description = message,
        icon = Icons.Rounded.ErrorOutline,
        actionText = if (onRetry != null) retryText else null,
        onActionClick = onRetry,
        modifier = modifier
    )
}
