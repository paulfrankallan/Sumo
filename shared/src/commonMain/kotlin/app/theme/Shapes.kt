package app.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(percent = 50),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
)

data class AppShapes(

    val paddingSmall: Dp = 4.dp,
    val paddingMedium: Dp = 8.dp,
    val paddingLarge: Dp = 24.dp,

    val cornersSmall: Dp = 2.dp,
    val paddingIntra: Dp = 10.dp, // Padding used for between content in views
    val paddingInner: Dp = 16.dp, // Used to surround content in views.
)

internal val LocalShapes = staticCompositionLocalOf { Shapes }
