package com.sonza.app.ui.screens.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.sonza.app.core.model.LogoVariant
import com.sonza.app.data.SessionManager
import com.sonza.app.ui.components.drawableRes
import com.sonza.app.ui.theme.SquircleShape
import com.sonza.app.ui.utils.SocialIcons
import org.koin.compose.koinInject

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AboutDeveloperSection(onOpenUri: (String) -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val onSurfaceVariant = colorScheme.onSurfaceVariant
    val sessionManager: SessionManager = koinInject()
    val logoVariant by sessionManager.logoVariantFlow.collectAsState(initial = LogoVariant.DEFAULT)

    AboutSectionTitle("Developer")
    AboutCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = "https://avatars.githubusercontent.com/u/201319388?v=4",
                contentDescription = "Prince Kumar Jha",
                modifier = Modifier
                    .size(100.dp)
                    .clip(SquircleShape),
                contentScale = ContentScale.Crop,
                error = painterResource(id = logoVariant.drawableRes())
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Prince Kumar Jha",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )

            Text(
                text = "Developer",
                style = MaterialTheme.typography.titleMedium,
                color = primaryColor,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "A self-taught Kotlin developer focused on clean architecture and thoughtful app design.",
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                color = onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SocialIconBadge(
                    icon = SocialIcons.GitHub,
                    onClick = { onOpenUri("https://github.com/princekjha-dev") }
                )
                SocialIconBadge(
                    icon = Icons.Default.Email,
                    onClick = { onOpenUri("mailto:pkjha2028@gmail.com") }
                )
                SocialIconBadge(
                    icon = Icons.Default.Language,
                    onClick = { onOpenUri("https://princekjha-dev.github.io/Sonza-Website/") }
                )
                SocialIconBadge(
                    icon = SocialIcons.Instagram,
                    onClick = { onOpenUri("https://www.instagram.com/naruto__sengupta?igsh=MWhyMXE4YzhxaDVvNg==") }
                )
                SocialIconBadge(
                    icon = SocialIcons.Telegram,
                    onClick = { onOpenUri("https://t.me/princekjha-dev") }
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
}
