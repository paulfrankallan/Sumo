package feature.game.joystick.ui.icons.background

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import feature.game.joystick.ui.icons.JoystickIcons

val JoystickIcons.Background.Modern: ImageVector
    get() {
        if (_Modern != null) {
            return _Modern!!
        }
        _Modern = ImageVector.Builder(
            name = "Modern",
            defaultWidth = 100.dp,
            defaultHeight = 100.dp,
            viewportWidth = 52.917f,
            viewportHeight = 52.917f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF0B0609)),
                strokeLineWidth = 0.254657f
            ) {
                moveTo(26.458f, 26.458f)
                moveToRelative(-25.929f, 0f)
                arcToRelative(25.929f, 25.929f, 0f, isMoreThanHalf = true, isPositiveArc = true, 51.858f, 0f)
                arcToRelative(25.929f, 25.929f, 0f, isMoreThanHalf = true, isPositiveArc = true, -51.858f, 0f)
            }
            path(
                fill = SolidColor(Color(0xFF2A2D34)),
                strokeLineWidth = 0.41144f
            ) {
                moveTo(26.458f, 26.458f)
                moveToRelative(-25.135f, 0f)
                arcToRelative(25.135f, 25.135f, 0f, isMoreThanHalf = true, isPositiveArc = true, 50.271f, 0f)
                arcToRelative(25.135f, 25.135f, 0f, isMoreThanHalf = true, isPositiveArc = true, -50.271f, 0f)
            }
            path(
                fill = SolidColor(Color(0xFF0B0609)),
                strokeLineWidth = 0.182304f
            ) {
                moveTo(43.951f, 8.537f)
                lineToRelative(0.561f, 0.561f)
                lineToRelative(-35.547f, 35.547f)
                lineToRelative(-0.561f, -0.561f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF0B0609)),
                strokeLineWidth = 0.182305f
            ) {
                moveTo(43.951f, 44.645f)
                lineToRelative(-35.547f, -35.547f)
                lineToRelative(0.561f, -0.561f)
                lineToRelative(35.547f, 35.547f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF0B0609)),
                strokeLineWidth = 0.567097f
            ) {
                moveTo(26.458f, 26.458f)
                moveToRelative(-12.171f, 0f)
                arcToRelative(12.171f, 12.171f, 0f, isMoreThanHalf = true, isPositiveArc = true, 24.342f, 0f)
                arcToRelative(12.171f, 12.171f, 0f, isMoreThanHalf = true, isPositiveArc = true, -24.342f, 0f)
            }
            path(
                fill = SolidColor(Color(0xFFDFDED9)),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.0375506f
            ) {
                moveToRelative(26.186f, 6.133f)
                lineToRelative(-2.355f, 2.53f)
                curveToRelative(0.026f, 0.559f, 0.356f, 0.859f, 0.906f, 0.973f)
                lineToRelative(1.721f, -1.946f)
                lineToRelative(1.721f, 1.946f)
                curveToRelative(0.503f, -0.096f, 0.816f, -0.408f, 0.906f, -0.973f)
                lineToRelative(-2.355f, -2.53f)
                curveToRelative(-0.189f, -0.218f, -0.37f, -0.213f, -0.543f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFDFDED9)),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.0375506f
            ) {
                moveToRelative(26.73f, 46.783f)
                lineToRelative(2.355f, -2.53f)
                curveToRelative(-0.026f, -0.559f, -0.356f, -0.859f, -0.906f, -0.973f)
                lineToRelative(-1.721f, 1.946f)
                lineToRelative(-1.721f, -1.946f)
                curveToRelative(-0.503f, 0.096f, -0.816f, 0.408f, -0.906f, 0.973f)
                lineToRelative(2.355f, 2.53f)
                curveToRelative(0.189f, 0.218f, 0.37f, 0.213f, 0.543f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFDFDED9)),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.0375506f
            ) {
                moveToRelative(46.783f, 26.186f)
                lineToRelative(-2.53f, -2.355f)
                curveToRelative(-0.559f, 0.026f, -0.859f, 0.356f, -0.973f, 0.906f)
                lineToRelative(1.946f, 1.721f)
                lineToRelative(-1.946f, 1.721f)
                curveToRelative(0.096f, 0.503f, 0.408f, 0.816f, 0.973f, 0.906f)
                lineToRelative(2.53f, -2.355f)
                curveToRelative(0.218f, -0.189f, 0.213f, -0.37f, 0f, -0.543f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFDFDED9)),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.0375506f
            ) {
                moveToRelative(6.133f, 26.73f)
                lineToRelative(2.53f, 2.355f)
                curveToRelative(0.559f, -0.026f, 0.859f, -0.356f, 0.973f, -0.906f)
                lineToRelative(-1.946f, -1.721f)
                lineToRelative(1.946f, -1.721f)
                curveToRelative(-0.096f, -0.503f, -0.408f, -0.816f, -0.973f, -0.906f)
                lineToRelative(-2.53f, 2.355f)
                curveToRelative(-0.218f, 0.189f, -0.213f, 0.37f, 0f, 0.543f)
                close()
            }
        }.build()

        return _Modern!!
    }

@Suppress("ObjectPropertyName")
private var _Modern: ImageVector? = null
