package feature.game.joystick.extensions

import kotlin.enums.EnumEntries
import kotlin.jvm.JvmOverloads

@JvmOverloads
fun <E : Enum<E>> EnumEntries<E>.firstOrdinal(ordinal: Int, default: E? = null): E {
    return firstOrNull { it.ordinal == ordinal } ?: (default ?: first { it.ordinal == 0 })
}
