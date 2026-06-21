package feature.game.joystick.ui.icons.background

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import feature.game.joystick.ui.icons.JoystickIcons

val JoystickIcons.Background.DpadStandard: ImageVector
    get() {
        if (_DpadStandard != null) {
            return _DpadStandard!!
        }
        _DpadStandard = ImageVector.Builder(
            name = "DpadStandard",
            defaultWidth = 200.dp,
            defaultHeight = 200.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF0B0609))) {
                moveToRelative(8.28f, 8.28f)
                lineToRelative(7.44f, 0f)
                lineToRelative(0f, 7.44f)
                lineToRelative(-7.44f, 0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF1E1E1E))) {
                moveToRelative(8.4f, 8.4f)
                lineToRelative(7.2f, -0f)
                lineToRelative(0f, 7.2f)
                lineToRelative(-7.2f, 0f)
                close()
            }
            path(
                stroke = SolidColor(Color(0xFFECECEC)),
                strokeLineWidth = 0.228f
            ) {
                moveToRelative(12f, 9.786f)
                lineToRelative(2.215f, 2.215f)
                lineToRelative(-2.215f, 2.215f)
                lineToRelative(-2.215f, -2.215f)
                close()
            }
            path(fill = SolidColor(Color(0xFF0B0609))) {
                moveToRelative(8.28f, 0.24f)
                lineToRelative(7.44f, 0f)
                lineToRelative(0f, 7.44f)
                lineToRelative(-7.44f, 0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF1E1E1E))) {
                moveToRelative(8.4f, 0.36f)
                lineToRelative(7.2f, -0f)
                lineToRelative(0f, 7.2f)
                lineToRelative(-7.2f, 0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFECECEC))) {
                moveToRelative(11.999f, 2.04f)
                lineToRelative(1.68f, 3.36f)
                lineToRelative(-3.36f, 0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF0B0609))) {
                moveToRelative(8.28f, 16.32f)
                lineToRelative(7.44f, 0f)
                lineToRelative(0f, 7.44f)
                lineToRelative(-7.44f, 0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF1E1E1E))) {
                moveToRelative(8.4f, 16.44f)
                lineToRelative(7.2f, -0f)
                lineToRelative(0f, 7.2f)
                lineToRelative(-7.2f, 0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFECECEC))) {
                moveToRelative(11.999f, 18.12f)
                lineToRelative(1.68f, 3.36f)
                lineToRelative(-3.36f, 0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF0B0609))) {
                moveToRelative(23.76f, 8.28f)
                lineToRelative(-0f, 7.44f)
                lineToRelative(-7.44f, 0f)
                lineToRelative(-0f, -7.44f)
                close()
            }
            path(fill = SolidColor(Color(0xFF1E1E1E))) {
                moveToRelative(23.64f, 8.4f)
                lineToRelative(0f, 7.2f)
                lineToRelative(-7.2f, 0f)
                lineToRelative(-0f, -7.2f)
                close()
            }
            path(fill = SolidColor(Color(0xFFECECEC))) {
                moveToRelative(21.96f, 11.999f)
                lineToRelative(-3.36f, 1.68f)
                lineToRelative(-0f, -3.36f)
                close()
            }
            path(fill = SolidColor(Color(0xFF0B0609))) {
                moveToRelative(0.24f, 15.72f)
                lineToRelative(0f, -7.44f)
                lineToRelative(7.44f, -0f)
                lineToRelative(0f, 7.44f)
                close()
            }
            path(fill = SolidColor(Color(0xFF1E1E1E))) {
                moveToRelative(0.36f, 15.6f)
                lineToRelative(-0f, -7.2f)
                lineToRelative(7.2f, -0f)
                lineToRelative(0f, 7.2f)
                close()
            }
            path(fill = SolidColor(Color(0xFFECECEC))) {
                moveToRelative(2.04f, 12f)
                lineToRelative(3.36f, -1.68f)
                lineToRelative(0f, 3.36f)
                close()
            }
        }.build()

        return _DpadStandard!!
    }

@Suppress("ObjectPropertyName")
private var _DpadStandard: ImageVector? = null
