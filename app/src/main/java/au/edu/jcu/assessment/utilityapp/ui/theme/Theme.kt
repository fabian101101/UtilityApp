package au.edu.jcu.assessment.utilityapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = MossPrimary,
    onPrimary = MossOnPrimary,
    primaryContainer = MossContainer,
    onPrimaryContainer = MossOnContainer,
    secondary = SageSecondary,
    onSecondary = MossOnPrimary,
    secondaryContainer = SageContainer,
    onSecondaryContainer = SageOnContainer,
    tertiary = MarigoldTertiary,
    onTertiary = MossOnPrimary,
    tertiaryContainer = MarigoldContainer,
    background = MistBackground,
    onBackground = MistInk,
    surface = MistBackground,
    onSurface = MistInk,
    surfaceVariant = MistVariant,
    onSurfaceVariant = MistInkMuted,
    surfaceContainer = MistBar,
    surfaceContainerHigh = MistCard,
    outline = MistOutline,
    outlineVariant = MistOutlineSoft
)

private val DarkColors = darkColorScheme(
    primary = DarkMossPrimary,
    onPrimary = DarkMossOnPrimary,
    primaryContainer = DarkMossContainer,
    onPrimaryContainer = DarkMossOnContainer,
    secondary = DarkSageSecondary,
    onSecondary = DarkMossOnPrimary,
    secondaryContainer = DarkSageContainer,
    onSecondaryContainer = DarkSageOnContainer,
    tertiary = DarkMarigoldTertiary,
    onTertiary = DarkMossOnPrimary,
    tertiaryContainer = DarkMarigoldContainer,
    background = NightBackground,
    onBackground = NightInk,
    surface = NightBackground,
    onSurface = NightInk,
    surfaceVariant = NightVariant,
    onSurfaceVariant = NightInkMuted,
    surfaceContainer = NightBar,
    surfaceContainerHigh = NightCard,
    outline = NightOutline,
    outlineVariant = NightOutlineSoft
)

/** Material 3 theme for the app. Follows the system light/dark setting. */
@Composable
fun CalmSkiesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
