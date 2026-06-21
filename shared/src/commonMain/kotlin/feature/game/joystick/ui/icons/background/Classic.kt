package feature.game.joystick.ui.icons.background

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import feature.game.joystick.ui.icons.JoystickIcons

val JoystickIcons.Background.Classic: ImageVector
    get() {
        if (_Classic != null) {
            return _Classic!!
        }
        _Classic = ImageVector.Builder(
            name = "Classic",
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
                fill = SolidColor(Color(0xFF202126)),
                strokeLineWidth = 0.378297f
            ) {
                moveTo(26.458f, 26.458f)
                moveToRelative(-14.817f, 0f)
                arcToRelative(14.817f, 14.817f, 0f, isMoreThanHalf = true, isPositiveArc = true, 29.633f, 0f)
                arcToRelative(14.817f, 14.817f, 0f, isMoreThanHalf = true, isPositiveArc = true, -29.633f, 0f)
            }
            path(
                fill = SolidColor(Color(0xFF0B0609)),
                strokeLineWidth = 0.504054f
            ) {
                moveTo(26.458f, 26.458f)
                moveToRelative(-12.965f, 0f)
                arcToRelative(12.965f, 12.965f, 0f, isMoreThanHalf = true, isPositiveArc = true, 25.929f, 0f)
                arcToRelative(12.965f, 12.965f, 0f, isMoreThanHalf = true, isPositiveArc = true, -25.929f, 0f)
            }
            path(
                fill = SolidColor(Color(0xFF2A2D34)),
                strokeLineWidth = 0.567097f
            ) {
                moveTo(26.458f, 26.458f)
                moveToRelative(-12.171f, 0f)
                arcToRelative(12.171f, 12.171f, 0f, isMoreThanHalf = true, isPositiveArc = true, 24.342f, 0f)
                arcToRelative(12.171f, 12.171f, 0f, isMoreThanHalf = true, isPositiveArc = true, -24.342f, 0f)
            }
            path(
                fill = SolidColor(Color(0xFFDFDED9)),
                strokeLineWidth = 0.04209f
            ) {
                moveToRelative(26.991f, 43.789f)
                verticalLineToRelative(3.793f)
                lineToRelative(0.736f, -0.843f)
                curveToRelative(0.496f, -0.125f, 0.692f, 0.201f, 0.525f, 0.632f)
                lineToRelative(-1.566f, 1.586f)
                curveToRelative(-0.136f, 0.142f, -0.3f, 0.175f, -0.456f, 0.027f)
                lineToRelative(-1.551f, -1.613f)
                curveToRelative(-0.21f, -0.48f, 0.056f, -0.726f, 0.525f, -0.632f)
                lineToRelative(0.736f, 0.843f)
                verticalLineToRelative(-3.793f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFDFDED9)),
                strokeLineWidth = 0.04209f
            ) {
                moveToRelative(25.925f, 9.128f)
                verticalLineToRelative(-3.793f)
                lineToRelative(-0.736f, 0.843f)
                curveToRelative(-0.496f, 0.125f, -0.692f, -0.201f, -0.525f, -0.632f)
                lineToRelative(1.566f, -1.586f)
                curveToRelative(0.136f, -0.142f, 0.3f, -0.175f, 0.456f, -0.027f)
                lineToRelative(1.551f, 1.613f)
                curveToRelative(0.21f, 0.48f, -0.056f, 0.726f, -0.525f, 0.632f)
                lineToRelative(-0.736f, -0.843f)
                verticalLineToRelative(3.793f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFDFDED9)),
                strokeLineWidth = 0.04209f
            ) {
                moveToRelative(43.789f, 25.925f)
                horizontalLineToRelative(3.793f)
                lineToRelative(-0.843f, -0.736f)
                curveToRelative(-0.125f, -0.496f, 0.201f, -0.692f, 0.632f, -0.525f)
                lineToRelative(1.586f, 1.566f)
                curveToRelative(0.142f, 0.136f, 0.175f, 0.3f, 0.027f, 0.456f)
                lineToRelative(-1.613f, 1.551f)
                curveToRelative(-0.48f, 0.21f, -0.726f, -0.056f, -0.632f, -0.525f)
                lineToRelative(0.843f, -0.736f)
                horizontalLineToRelative(-3.793f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFDFDED9)),
                strokeLineWidth = 0.04209f
            ) {
                moveTo(9.128f, 26.991f)
                horizontalLineTo(5.335f)
                lineToRelative(0.843f, 0.736f)
                curveToRelative(0.125f, 0.496f, -0.201f, 0.692f, -0.632f, 0.525f)
                lineTo(3.96f, 26.686f)
                curveTo(3.818f, 26.55f, 3.785f, 26.386f, 3.933f, 26.23f)
                lineToRelative(1.613f, -1.551f)
                curveToRelative(0.48f, -0.21f, 0.726f, 0.056f, 0.632f, 0.525f)
                lineToRelative(-0.843f, 0.736f)
                horizontalLineToRelative(3.793f)
                close()
            }
        }.build()

        return _Classic!!
    }

@Suppress("ObjectPropertyName")
private var _Classic: ImageVector? = null
