package rs.moma.therminator.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

val AccentColor = Color(0xFFC23535)
val PrimaryColor = Color(0xFFE6E5E3)
val OutlineColor = Color(0xFFB6B3B0)
val ButtonColor = Color(0xFF4B4A49)
val CardColor = Color(0xFF2B2A29)
val MenuColor = Color(0xFF1D1C1B)
val BackgroundColor = Color(0xFF111110)


fun Color.toCss(): String = "rgba(${(red * 255).roundToInt()}, ${(green * 255).roundToInt()}, ${(blue * 255).roundToInt()}, $alpha)"