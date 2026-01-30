package rs.moma.therminator.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    outline = OutlineColor,
    primary = PrimaryColor,
    onPrimary = CardColor,
    secondary = AccentColor,
    background = BackgroundColor,
    onBackground = Color.White,
    surface = BackgroundColor,
    onSurface = Color.White,
    onSurfaceVariant = PrimaryColor,
    surfaceContainer = MenuColor,
    surfaceContainerHigh = MenuColor,
    surfaceContainerHighest = CardColor,
    primaryContainer = ButtonColor,
    onPrimaryContainer = Color.White,
    secondaryContainer = ButtonColor,
    onSecondaryContainer = AccentColor
)

@Composable
fun TherminatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography(),
        content = content
    )
}
