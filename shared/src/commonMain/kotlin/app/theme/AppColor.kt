package app.theme

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

val colorHomeDarkGreen = sumoBackground
val colorHomeLightGreen = sumoRicePaper
val appRed = sumoVermillion
val appGold = sumoStrawGold
val appClay = sumoClay
val appSand = sumoSand
val appWood = sumoDarkWood
val playerOneColor = sumoVermillion
val playerTwoColor = sumoIndigo

@Stable
data class AppColors(
    val primary: Color,
    val secondary: Color,
    val appRed: Color
)

val LightAppColors = AppColors(
    primary = appRed,
    secondary = appGold,
    appRed = appRed
)

val DarkAppColors = AppColors(
    primary = appRed,
    secondary = appGold,
    appRed = appRed
)

/* *
* United Kingdom (UK) - Multi-Terrain Pattern (MTP):
* * Light Tan: #D2B48C
* * Mid Brown: #A0522D
* * Dark Brown: #654321
* * Green: #556B2F
* * Dark Green: #2F4F4F
* United States (US) - Operational Camouflage Pattern (OCP):
* * Tan: #D2B48C
* * Light Green: #9ACD32
* * Dark Brown: #8B4513
* * Olive Drab: #6B8E23
* * Dark Green: #2E8B57
* * Black: #000000
* * RGB: (67, 112, 114) - A muted teal color.
* * RGB: (26, 39, 42) - A dark, almost black color.
* * RGB: (248, 240, 202) - A light cream color.
* * RGB: (149, 100, 52) - A brownish color.
* * RGB: (212, 170, 108) - A light brown color.
* */

// #2F4F4F - Green
// #8B0000 - Red

enum class AppColor(val color: Color) {
    LIGHT_BROWN(appClay),
    BLOOD_RED(appRed),
    GREEN(sumoPine),
    DARK_GREEN(appWood),
    LIGHT_GREEN(appGold),
}