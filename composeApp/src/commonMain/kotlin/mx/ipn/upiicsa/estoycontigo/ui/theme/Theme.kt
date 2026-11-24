package mx.ipn.upiicsa.estoycontigo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.ipn.upiicsa.estoycontigo.domain.model.Emotion
import mx.ipn.upiicsa.estoycontigo.domain.model.ThemeMode

/** Paleta neutra y desaturada inspirada en las emociones: tonos que no alarman ni sobreestimulan. */
object Palette {
    val Sage = Color(0xFF5E7F6E)
    val SageLight = Color(0xFFDCE8E0)
    val Cream = Color(0xFFF8F4EE)
    val Sand = Color(0xFFEFE7DC)
    val Ink = Color(0xFF2F3E36)
    val Clay = Color(0xFFB9705E)
    val Pending = Color(0xFFD9893B)
    val Synced = Color(0xFF4F9A6B)
    val CrisisRed = Color(0xFFB44B3F)

    val NightBg = Color(0xFF171D1B)
    val NightSurface = Color(0xFF222A27)
    val NightSage = Color(0xFF9EC3AE)
}

@Immutable
data class EmotionColors(val base: Color, val container: Color, val onContainer: Color)

fun Emotion.colors(dark: Boolean): EmotionColors = when (this) {
    Emotion.ALEGRIA -> if (dark) EmotionColors(Color(0xFFE6C875), Color(0xFF4A4127), Color(0xFFF6E7B8))
    else EmotionColors(Color(0xFFD9B556), Color(0xFFF7EBC7), Color(0xFF5A4813))
    Emotion.CALMA -> if (dark) EmotionColors(Color(0xFF9EC3AE), Color(0xFF2E4238), Color(0xFFD5E8DC))
    else EmotionColors(Color(0xFF7FA892), Color(0xFFDCEBE2), Color(0xFF2C4A3B))
    Emotion.TRISTEZA -> if (dark) EmotionColors(Color(0xFFA2B9D4), Color(0xFF2E3A4A), Color(0xFFD8E3F0))
    else EmotionColors(Color(0xFF7F9BBD), Color(0xFFDDE6F1), Color(0xFF2A3D57))
    Emotion.ENOJO -> if (dark) EmotionColors(Color(0xFFE0AA9A), Color(0xFF4A322C), Color(0xFFF4D9D0))
    else EmotionColors(Color(0xFFC98B78), Color(0xFFF4DED6), Color(0xFF5E2E21))
    Emotion.ANSIEDAD -> if (dark) EmotionColors(Color(0xFFC0B3DC), Color(0xFF3A3449), Color(0xFFE5DEF3))
    else EmotionColors(Color(0xFFA394C8), Color(0xFFE8E2F3), Color(0xFF3D3359))
    Emotion.CANSANCIO -> if (dark) EmotionColors(Color(0xFFC6BBAE), Color(0xFF3D3833), Color(0xFFEAE3DA))
    else EmotionColors(Color(0xFFA89C8D), Color(0xFFECE6DE), Color(0xFF4A4036))
}

@Immutable
data class ExtraColors(val pending: Color, val synced: Color, val crisis: Color, val onCrisis: Color, val isDark: Boolean)

val LocalExtraColors = staticCompositionLocalOf {
    ExtraColors(Palette.Pending, Palette.Synced, Palette.CrisisRed, Color.White, false)
}

private val LightColors = lightColorScheme(
    primary = Palette.Sage,
    onPrimary = Color.White,
    primaryContainer = Palette.SageLight,
    onPrimaryContainer = Color(0xFF1C3328),
    secondary = Color(0xFF8A7A9E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEAE3F1),
    onSecondaryContainer = Color(0xFF3A3049),
    tertiary = Palette.Clay,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF6DDD5),
    onTertiaryContainer = Color(0xFF4E2418),
    background = Palette.Cream,
    onBackground = Palette.Ink,
    surface = Palette.Cream,
    onSurface = Palette.Ink,
    surfaceVariant = Palette.Sand,
    onSurfaceVariant = Color(0xFF55605A),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFCF9F5),
    surfaceContainer = Color(0xFFF4EEE6),
    surfaceContainerHigh = Color(0xFFEFE8DE),
    surfaceContainerHighest = Color(0xFFE9E1D6),
    outline = Color(0xFF85908A),
    outlineVariant = Color(0xFFD6D0C6),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = Palette.NightSage,
    onPrimary = Color(0xFF0F2A1D),
    primaryContainer = Color(0xFF3B5547),
    onPrimaryContainer = Color(0xFFD5E8DC),
    secondary = Color(0xFFC9BCDB),
    onSecondary = Color(0xFF2F2640),
    secondaryContainer = Color(0xFF453B55),
    onSecondaryContainer = Color(0xFFEAE3F1),
    tertiary = Color(0xFFE6B3A4),
    onTertiary = Color(0xFF4E2418),
    tertiaryContainer = Color(0xFF6A3A2D),
    onTertiaryContainer = Color(0xFFF6DDD5),
    background = Palette.NightBg,
    onBackground = Color(0xFFE3E7E4),
    surface = Palette.NightBg,
    onSurface = Color(0xFFE3E7E4),
    surfaceVariant = Color(0xFF39423E),
    onSurfaceVariant = Color(0xFFBEC8C2),
    surfaceContainerLowest = Color(0xFF121715),
    surfaceContainerLow = Color(0xFF1C2320),
    surfaceContainer = Palette.NightSurface,
    surfaceContainerHigh = Color(0xFF2B3430),
    surfaceContainerHighest = Color(0xFF353F3B),
    outline = Color(0xFF89938D),
    outlineVariant = Color(0xFF3F4945),
)

private val AppTypography = Typography().let { t ->
    t.copy(
        displaySmall = t.displaySmall.copy(fontWeight = FontWeight.SemiBold),
        headlineMedium = t.headlineMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 26.sp),
        headlineSmall = t.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = t.bodyLarge.copy(lineHeight = 24.sp),
        bodyMedium = t.bodyMedium.copy(lineHeight = 21.sp),
        labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, letterSpacing = 0.2.sp),
    )
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun EstoyContigoTheme(mode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val extra = if (dark) ExtraColors(Color(0xFFF0A65E), Color(0xFF7CC59A), Color(0xFFE07A6E), Color(0xFF2B0B07), true)
    else ExtraColors(Palette.Pending, Palette.Synced, Palette.CrisisRed, Color.White, false)
    CompositionLocalProvider(LocalExtraColors provides extra) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

object AppTheme {
    val extra: ExtraColors @Composable get() = LocalExtraColors.current
}
