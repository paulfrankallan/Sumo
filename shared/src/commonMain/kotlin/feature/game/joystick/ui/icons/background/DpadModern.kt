package feature.game.joystick.ui.icons.background

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import feature.game.joystick.ui.icons.JoystickIcons

val JoystickIcons.Background.DpadModern: ImageVector
    get() {
        if (_DpadModern != null) {
            return _DpadModern!!
        }
        _DpadModern = ImageVector.Builder(
            name = "DpadModern",
            defaultWidth = 100.dp,
            defaultHeight = 100.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFFECECEC))) {
                moveToRelative(9.421f, 0.24f)
                lineToRelative(5.16f, 0f)
                lineToRelative(0.84f, 0.84f)
                lineToRelative(0f, 7.14f)
                lineToRelative(-3.42f, 3.42f)
                lineToRelative(-3.42f, -3.42f)
                lineToRelative(0f, -7.14f)
                close()
            }
            path(fill = SolidColor(Color(0xFF1E1E1E))) {
                moveToRelative(9.511f, 0.36f)
                lineToRelative(4.979f, 0f)
                lineToRelative(0.811f, 0.822f)
                lineToRelative(0f, 6.99f)
                lineToRelative(-3.3f, 3.348f)
                lineToRelative(-3.3f, -3.348f)
                lineToRelative(0f, -6.99f)
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
            path(fill = SolidColor(Color(0xFFECECEC))) {
                moveToRelative(9.421f, 23.76f)
                lineToRelative(5.16f, -0f)
                lineToRelative(0.84f, -0.84f)
                lineToRelative(0f, -7.14f)
                lineToRelative(-3.42f, -3.42f)
                lineToRelative(-3.42f, 3.42f)
                lineToRelative(0f, 7.14f)
                close()
            }
            path(fill = SolidColor(Color(0xFF1E1E1E))) {
                moveToRelative(9.511f, 23.64f)
                lineToRelative(4.979f, -0f)
                lineToRelative(0.811f, -0.822f)
                lineToRelative(0f, -6.99f)
                lineToRelative(-3.3f, -3.348f)
                lineToRelative(-3.3f, 3.348f)
                lineToRelative(0f, 6.99f)
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
            path(fill = SolidColor(Color(0xFFECECEC))) {
                moveToRelative(0.24f, 14.579f)
                lineToRelative(0f, -5.16f)
                lineToRelative(0.84f, -0.84f)
                lineToRelative(7.14f, -0f)
                lineToRelative(3.42f, 3.42f)
                lineToRelative(-3.42f, 3.42f)
                lineToRelative(-7.14f, -0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF1E1E1E))) {
                moveToRelative(0.36f, 14.489f)
                lineToRelative(0f, -4.979f)
                lineToRelative(0.822f, -0.811f)
                lineToRelative(6.99f, -0f)
                lineToRelative(3.348f, 3.3f)
                lineToRelative(-3.348f, 3.3f)
                lineToRelative(-6.99f, -0f)
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
            path(fill = SolidColor(Color(0xFFECECEC))) {
                moveToRelative(23.76f, 14.579f)
                lineToRelative(-0f, -5.16f)
                lineToRelative(-0.84f, -0.84f)
                lineToRelative(-7.14f, -0f)
                lineToRelative(-3.42f, 3.42f)
                lineToRelative(3.42f, 3.42f)
                lineToRelative(7.14f, -0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF1E1E1E))) {
                moveToRelative(23.64f, 14.489f)
                lineToRelative(-0f, -4.979f)
                lineToRelative(-0.822f, -0.811f)
                lineToRelative(-6.99f, -0f)
                lineToRelative(-3.348f, 3.3f)
                lineToRelative(3.348f, 3.3f)
                lineToRelative(6.99f, -0f)
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

        return _DpadModern!!
    }

@Suppress("ObjectPropertyName")
private var _DpadModern: ImageVector? = null
