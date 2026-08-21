package com.sonza.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sonza.app.R

/**
 * Typography — Material 3 Expressive scale powered by Manrope (variable font).
 * Bold, confident hierarchy per Part 2 of DESIGN_SYSTEM.md.
 */
val Manrope: FontFamily = FontFamily(
    Font(R.font.manrope, FontWeight.Normal),
    Font(R.font.manrope, FontWeight.Medium),
    Font(R.font.manrope, FontWeight.SemiBold),
    Font(R.font.manrope, FontWeight.Bold),
    Font(R.font.manrope, FontWeight.ExtraBold)
)

// Explicit Sonza Design System typography tokens
object SonzaTypography {
    // 1. Page Title — Top clean, regular/medium/bold large display header (e.g. "New", "Search", "Library")
    val Display = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold, // 700
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp
    )
    val PageTitle = Display

    // 2. Section Title — Standardized large header for sections (e.g. "Best New Songs", "New This Week", "Recent Releases", "Latest Songs")
    val Headline = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold, // 700
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.3).sp
    )
    val SectionTitle = Headline

    val TitleLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.3).sp
    )

    // 3. Song Title — Medium-sized, semi-bold title for all song rows and list items
    val SongTitle = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )
    val TitleMedium = SongTitle

    // 4. Card Title — Standard title for album/playlist/mix square & content cards
    val CardTitle = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    )
    val TitleSmall = CardTitle

    // 5. Body Large — Readable body text
    val BodyLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal, // 400
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp
    )

    // 6. Artist / Subtitle — Subordinate artist name / secondary description
    val ArtistSubtitle = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal, // 400
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    )
    val BodyMedium = ArtistSubtitle

    // 7. Card Subtitle — Subordinate artist/creator/count on cards
    val CardSubtitle = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal, // 400
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp
    )
    val BodySmall = CardSubtitle

    // 8. Kicker / Category Super-header (e.g. "UPDATED PLAYLIST", "NEW ALBUM", "FEATURED")
    val Kicker = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold, // 700
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.8.sp
    )

    // 9. Navigation & Chip Labels
    val LabelLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    )

    val LabelMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium, // 500
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    )

    val NavLabel = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium, // 500
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.2.sp
    )
    val LabelSmall = NavLabel

    // 10. Metadata & Badges (e.g. duration, format badge, rank)
    val Metadata = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium, // 500
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.2.sp
    )
}

val Typography = Typography(
    // Display — page titles, Now Playing hero track title
    displayLarge = SonzaTypography.Display,
    displayMedium = SonzaTypography.Display,
    displaySmall = SonzaTypography.Headline,

    // Headlines — section titles ("Best New Songs", "New This Week", "Recent Releases")
    headlineLarge = SonzaTypography.Headline,
    headlineMedium = SonzaTypography.Headline,
    headlineSmall = SonzaTypography.TitleLarge,

    // Titles — song titles, card titles, dialog titles
    titleLarge = SonzaTypography.TitleLarge,
    titleMedium = SonzaTypography.SongTitle,
    titleSmall = SonzaTypography.CardTitle,

    // Body — primary readable text, artist names, card subtitles
    bodyLarge = SonzaTypography.BodyLarge,
    bodyMedium = SonzaTypography.ArtistSubtitle,
    bodySmall = SonzaTypography.CardSubtitle,

    // Labels — buttons, chip labels, timestamps, metadata, format badges, navigation
    labelLarge = SonzaTypography.LabelLarge,
    labelMedium = SonzaTypography.LabelMedium,
    labelSmall = SonzaTypography.NavLabel
)
