package feature.game.joystick.ui.icons.background

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import feature.game.joystick.ui.icons.JoystickIcons

val JoystickIcons.Background.DpadClassic: ImageVector
    get() {
        if (_DpadClassic != null) {
            return _DpadClassic!!
        }
        _DpadClassic = ImageVector.Builder(
            name = "DpadClassic",
            defaultWidth = 100.dp,
            defaultHeight = 100.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF0B0609))) {
                moveToRelative(8.58f, 8.28f)
                lineToRelative(0f, -0.12f)
                lineToRelative(6.84f, -0f)
                lineToRelative(0f, 0.12f)
                arcToRelative(0.3f, 0.3f, 103.31f, isMoreThanHalf = false, isPositiveArc = false, 0.3f, 0.3f)
                lineToRelative(0.12f, 0f)
                lineToRelative(0f, 6.84f)
                lineToRelative(-0.12f, 0f)
                arcToRelative(0.3f, 0.3f, 129.93f, isMoreThanHalf = false, isPositiveArc = false, -0.3f, 0.3f)
                lineToRelative(0f, 0.12f)
                lineToRelative(-6.84f, 0f)
                lineToRelative(0f, -0.121f)
                arcToRelative(0.299f, 0.299f, 103.31f, isMoreThanHalf = false, isPositiveArc = false, -0.299f, -0.299f)
                lineToRelative(-0.121f, 0f)
                lineToRelative(0f, -6.84f)
                lineToRelative(0.12f, 0f)
                arcToRelative(0.3f, 0.3f, 129.93f, isMoreThanHalf = false, isPositiveArc = false, 0.3f, -0.3f)
                close()
            }
            path(fill = SolidColor(Color(0xFF1E1E1E))) {
                moveToRelative(8.7f, 8.28f)
                lineToRelative(6.6f, -0f)
                arcToRelative(0.42f, 0.42f, 103.31f, isMoreThanHalf = false, isPositiveArc = false, 0.42f, 0.42f)
                lineToRelative(0f, 6.6f)
                arcToRelative(0.42f, 0.42f, 129.93f, isMoreThanHalf = false, isPositiveArc = false, -0.42f, 0.42f)
                lineToRelative(-6.6f, 0f)
                arcToRelative(0.42f, 0.42f, 103.31f, isMoreThanHalf = false, isPositiveArc = false, -0.42f, -0.42f)
                lineToRelative(0f, -6.6f)
                arcToRelative(0.42f, 0.42f, 129.93f, isMoreThanHalf = false, isPositiveArc = false, 0.42f, -0.42f)
                close()
            }
            path(fill = SolidColor(Color(0xFF0B0609))) {
                moveToRelative(9.18f, 0.24f)
                lineToRelative(5.64f, 0f)
                lineToRelative(0.6f, 0.6f)
                lineToRelative(0f, 7.38f)
                lineToRelative(-6.84f, 0f)
                lineToRelative(0f, -7.38f)
                close()
            }
            path(fill = SolidColor(Color(0xFF1E1E1E))) {
                moveToRelative(9.279f, 0.36f)
                lineToRelative(5.442f, 0f)
                lineToRelative(0.579f, 0.579f)
                lineToRelative(0f, 7.401f)
                lineToRelative(-6.6f, 0f)
                lineToRelative(0f, -7.401f)
                close()
            }
            path(fill = SolidColor(Color(0xFFECECEC))) {
                moveToRelative(10.86f, 4.02f)
                lineToRelative(1.2f, -2.28f)
                lineToRelative(1.2f, 2.28f)
                lineToRelative(0f, 0.72f)
                lineToRelative(-2.4f, 0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF0B0609))) {
                moveToRelative(9.18f, 23.76f)
                lineToRelative(5.64f, -0f)
                lineToRelative(0.6f, -0.6f)
                lineToRelative(0f, -7.38f)
                lineToRelative(-6.84f, -0f)
                lineToRelative(0f, 7.38f)
                close()
            }
            path(fill = SolidColor(Color(0xFF1E1E1E))) {
                moveToRelative(9.279f, 23.64f)
                lineToRelative(5.442f, -0f)
                lineToRelative(0.579f, -0.579f)
                lineToRelative(0f, -7.401f)
                lineToRelative(-6.6f, -0f)
                lineToRelative(0f, 7.401f)
                close()
            }
            path(fill = SolidColor(Color(0xFFECECEC))) {
                moveToRelative(10.86f, 19.98f)
                lineToRelative(1.2f, 2.28f)
                lineToRelative(1.2f, -2.28f)
                lineToRelative(0f, -0.72f)
                lineToRelative(-2.4f, -0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF0B0609))) {
                moveToRelative(0.24f, 14.82f)
                lineToRelative(0f, -5.64f)
                lineToRelative(0.6f, -0.6f)
                lineToRelative(7.38f, -0f)
                lineToRelative(0f, 6.84f)
                lineToRelative(-7.38f, -0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF1E1E1E))) {
                moveToRelative(0.36f, 14.721f)
                lineToRelative(0f, -5.442f)
                lineToRelative(0.579f, -0.579f)
                lineToRelative(7.401f, -0f)
                lineToRelative(0f, 6.6f)
                lineToRelative(-7.401f, -0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFECECEC))) {
                moveToRelative(4.02f, 13.14f)
                lineToRelative(-2.28f, -1.2f)
                lineToRelative(2.28f, -1.2f)
                lineToRelative(0.72f, -0f)
                lineToRelative(0f, 2.4f)
                close()
            }
            path(fill = SolidColor(Color(0xFF0B0609))) {
                moveToRelative(23.76f, 14.82f)
                lineToRelative(-0f, -5.64f)
                lineToRelative(-0.6f, -0.6f)
                lineToRelative(-7.38f, -0f)
                lineToRelative(-0f, 6.84f)
                lineToRelative(7.38f, -0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF1E1E1E))) {
                moveToRelative(23.64f, 14.721f)
                lineToRelative(-0f, -5.442f)
                lineToRelative(-0.579f, -0.579f)
                lineToRelative(-7.401f, -0f)
                lineToRelative(-0f, 6.6f)
                lineToRelative(7.401f, -0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFECECEC))) {
                moveToRelative(19.98f, 13.14f)
                lineToRelative(2.28f, -1.2f)
                lineToRelative(-2.28f, -1.2f)
                lineToRelative(-0.72f, -0f)
                lineToRelative(-0f, 2.4f)
                close()
            }
        }.build()

        return _DpadClassic!!
    }

@Suppress("ObjectPropertyName")
private var _DpadClassic: ImageVector? = null
