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
    val Display = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold, // 700
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    )
    val Headline = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold, // 700
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.2).sp
    )
    val TitleLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    )
    val TitleMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    )
    val BodyLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal, // 400
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.25.sp
    )
    val BodyMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal, // 400
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp
    )
    val LabelLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium, // 500
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
    val LabelSmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium, // 500
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
}

val Typography = Typography(
    // Display — big headlines, Now Playing track title
    displayLarge = SonzaTypography.Display,
    displayMedium = SonzaTypography.Display.copy(fontSize = 30.sp, lineHeight = 36.sp),
    displaySmall = SonzaTypography.Headline,

    // Headlines — section titles ("Speed dial", "Listen again")
    headlineLarge = SonzaTypography.Headline,
    headlineMedium = SonzaTypography.Headline.copy(fontSize = 24.sp, lineHeight = 30.sp),
    headlineSmall = SonzaTypography.TitleLarge,

    // Titles — cards, dialogs, section headers
    titleLarge = SonzaTypography.TitleLarge,
    titleMedium = SonzaTypography.TitleMedium,
    titleSmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body — primary readable text, artist names
    bodyLarge = SonzaTypography.BodyLarge,
    bodyMedium = SonzaTypography.BodyMedium,
    bodySmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Labels — buttons, chip labels, timestamps, metadata, format badges
    labelLarge = SonzaTypography.LabelLarge,
    labelMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    ),
    labelSmall = SonzaTypography.LabelSmall
)
