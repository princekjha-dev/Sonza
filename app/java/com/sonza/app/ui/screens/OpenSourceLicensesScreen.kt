package com.sonza.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonza.app.ui.components.ExpressiveBottomNavTokens
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.components.SettingsCard
import com.sonza.app.ui.theme.RadiusTokens
import com.sonza.app.ui.theme.SonzaBackground
import com.sonza.app.ui.theme.SonzaBrandAccent
import com.sonza.app.ui.theme.SonzaOnBackground
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens
import com.sonza.app.ui.theme.SquircleShape

data class OpenSourceLibrary(
    val name: String,
    val artifact: String,
    val license: String,
    val description: String,
    val url: String
)

private val OPEN_SOURCE_LIBRARIES = listOf(
    OpenSourceLibrary(
        name = "Jetpack Compose",
        artifact = "androidx.compose.*",
        license = "Apache-2.0",
        description = "Modern declarative UI toolkit for building native Android applications.",
        url = "https://developer.android.com/jetpack/compose"
    ),
    OpenSourceLibrary(
        name = "Jetpack Media3 & ExoPlayer",
        artifact = "androidx.media3:media3-exoplayer",
        license = "Apache-2.0",
        description = "High-performance audio and video playback engine for streaming and local caching.",
        url = "https://github.com/androidx/media"
    ),
    OpenSourceLibrary(
        name = "Kotlin Coroutines & Flow",
        artifact = "org.jetbrains.kotlinx:kotlinx-coroutines-core",
        license = "Apache-2.0",
        description = "Rich asynchronous primitives and reactive streams for concurrent background operations.",
        url = "https://github.com/Kotlin/kotlinx.coroutines"
    ),
    OpenSourceLibrary(
        name = "KotlinX Serialization",
        artifact = "org.jetbrains.kotlinx:kotlinx-serialization-json",
        license = "Apache-2.0",
        description = "Cross-platform JSON serialization and deserialization library.",
        url = "https://github.com/Kotlin/kotlinx.serialization"
    ),
    OpenSourceLibrary(
        name = "Koin Dependency Injection",
        artifact = "io.insert-koin:koin-compose-viewmodel",
        license = "Apache-2.0",
        description = "Pragmatic and lightweight dependency injection framework for Kotlin & Compose.",
        url = "https://github.com/InsertKoinIO/koin"
    ),
    OpenSourceLibrary(
        name = "Coil 3",
        artifact = "io.coil-kt.coil3:coil-compose",
        license = "Apache-2.0",
        description = "Fast, lightweight image loading library backed by Kotlin Coroutines.",
        url = "https://github.com/coil-kt/coil"
    ),
    OpenSourceLibrary(
        name = "OkHttp & Retrofit",
        artifact = "com.squareup.okhttp3:okhttp",
        license = "Apache-2.0",
        description = "HTTP client and type-safe REST framework for robust networking.",
        url = "https://github.com/square/okhttp"
    ),
    OpenSourceLibrary(
        name = "Ktor Client",
        artifact = "io.ktor:ktor-client-core",
        license = "Apache-2.0",
        description = "Multiplatform asynchronous HTTP client framework.",
        url = "https://github.com/ktorio/ktor"
    ),
    OpenSourceLibrary(
        name = "Room Database",
        artifact = "androidx.room:room-runtime",
        license = "Apache-2.0",
        description = "SQLite object-mapping abstraction layer for resilient local data persistence.",
        url = "https://developer.android.com/training/data-storage/room"
    ),
    OpenSourceLibrary(
        name = "Lottie Compose",
        artifact = "com.airbnb.android:lottie-compose",
        license = "Apache-2.0",
        description = "Vector animation rendering engine for interactive UI components.",
        url = "https://github.com/airbnb/lottie-android"
    ),
    OpenSourceLibrary(
        name = "AndroidX Palette",
        artifact = "androidx.palette:palette-ktx",
        license = "Apache-2.0",
        description = "Color extraction utility for dynamic album-art driven theming.",
        url = "https://developer.android.com/reference/androidx/palette/graphics/Palette"
    ),
    OpenSourceLibrary(
        name = "Gson",
        artifact = "com.google.code.gson:gson",
        license = "Apache-2.0",
        description = "Java serialization and deserialization library to convert Java Objects into JSON.",
        url = "https://github.com/google/gson"
    )
)

@Composable
fun OpenSourceLicensesScreen(
    onBackClick: () -> Unit
) {
    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent.takeIf { it != Color.Unspecified } ?: SonzaBrandAccent
    val uriHandler = LocalUriHandler.current

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomSystemHeight = ExpressiveBottomNavTokens.getBottomSafePadding(false)
    val bottomInset = navBarPadding + bottomSystemHeight + SpacingTokens.Space2Xl

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SonzaBackground)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(
                start = SpacingTokens.SpaceLg,
                end = SpacingTokens.SpaceLg,
                top = SpacingTokens.SpaceSm,
                bottom = bottomInset
            ),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SpacingTokens.SpaceXs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SonzaOnBackground
                        )
                    }

                    Spacer(modifier = Modifier.width(SpacingTokens.SpaceSm))

                    Text(
                        text = "Open Source Licenses",
                        style = SonzaTypography.Headline,
                        fontWeight = FontWeight.Bold,
                        color = SonzaOnBackground
                    )
                }
            }

            // Summary Card
            item {
                SettingsCard(flat = true, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpacingTokens.SpaceLg)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = SquircleShape,
                                color = accentColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))
                            Column {
                                Text(
                                    text = "Third-Party Software",
                                    style = SonzaTypography.TitleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    color = SonzaOnBackground
                                )
                                Text(
                                    text = "Libraries powering Sonza",
                                    style = SonzaTypography.BodySmall,
                                    color = SonzaOnSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(SpacingTokens.SpaceMd))

                        Text(
                            text = "Sonza is made possible thanks to these exceptional open-source libraries and frameworks. We are deeply grateful to their creators and contributors.",
                            style = SonzaTypography.BodyMedium.copy(lineHeight = 22.sp),
                            color = SonzaOnSurfaceVariant
                        )
                    }
                }
            }

            // Libraries List
            items(OPEN_SOURCE_LIBRARIES, key = { it.name }) { lib ->
                LibraryItemCard(
                    library = lib,
                    accentColor = accentColor,
                    onClick = { uriHandler.openUri(lib.url) }
                )
            }
        }
    }
}

@Composable
private fun LibraryItemCard(
    library: OpenSourceLibrary,
    accentColor: Color,
    onClick: () -> Unit
) {
    SettingsCard(flat = true, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(SpacingTokens.SpaceLg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = library.name,
                    style = SonzaTypography.BodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = SonzaOnBackground,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    shape = RoundedCornerShape(RadiusTokens.Sm),
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = library.license,
                        style = SonzaTypography.BodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = library.artifact,
                style = SonzaTypography.BodySmall.copy(fontSize = 12.sp),
                color = accentColor.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = library.description,
                style = SonzaTypography.BodyMedium.copy(lineHeight = 20.sp),
                color = SonzaOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "View repository",
                    style = SonzaTypography.BodySmall.copy(fontWeight = FontWeight.Medium),
                    color = accentColor
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
