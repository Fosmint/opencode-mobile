package dev.opencode.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Strict grayscale palette per design spec: BLACK / DARK GRAY / GRAY / LIGHT GRAY / WHITE.
// No accent hue anywhere — accents are expressed via weight/opacity, not color.
object OpenCodePalette {
    val Black = Color(0xFF000000)
    val NearBlack = Color(0xFF0D0D0D)
    val DarkGray = Color(0xFF1A1A1A)
    val SurfaceGray = Color(0xFF242424)
    val MidGray = Color(0xFF3A3A3A)
    val Gray = Color(0xFF6E6E6E)
    val LightGray = Color(0xFFA6A6A6)
    val PaleGray = Color(0xFFD4D4D4)
    val OffWhite = Color(0xFFF2F2F2)
    val White = Color(0xFFFFFFFF)
    val Error = Color(0xFFE0E0E0) // errors are still conveyed via icon/label, not a red accent
    val ErrorText = Color(0xFFFF6B6B) // the one deliberate exception: destructive/error text needs to read as urgent
}

private val DarkScheme = darkColorScheme(
    primary = OpenCodePalette.White,
    onPrimary = OpenCodePalette.Black,
    secondary = OpenCodePalette.LightGray,
    onSecondary = OpenCodePalette.Black,
    background = OpenCodePalette.Black,
    onBackground = OpenCodePalette.OffWhite,
    surface = OpenCodePalette.NearBlack,
    onSurface = OpenCodePalette.OffWhite,
    surfaceVariant = OpenCodePalette.DarkGray,
    onSurfaceVariant = OpenCodePalette.LightGray,
    outline = OpenCodePalette.MidGray,
    error = OpenCodePalette.ErrorText,
    onError = OpenCodePalette.Black,
)

private val LightScheme = lightColorScheme(
    primary = OpenCodePalette.Black,
    onPrimary = OpenCodePalette.White,
    secondary = OpenCodePalette.Gray,
    onSecondary = OpenCodePalette.White,
    background = OpenCodePalette.White,
    onBackground = OpenCodePalette.Black,
    surface = OpenCodePalette.OffWhite,
    onSurface = OpenCodePalette.Black,
    surfaceVariant = OpenCodePalette.PaleGray,
    onSurfaceVariant = OpenCodePalette.Gray,
    outline = OpenCodePalette.LightGray,
    error = Color(0xFFB3261E),
    onError = OpenCodePalette.White,
)

private val OpenCodeTypography = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)

@Composable
fun OpenCodeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkScheme else LightScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = OpenCodeTypography,
        content = content,
    )
}
