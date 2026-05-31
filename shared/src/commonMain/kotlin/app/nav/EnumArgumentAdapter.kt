package app.nav

class EnumArgumentAdapter<T : Enum<T>> @PublishedApi internal constructor(
    private val enumValues: Array<out T>,
) : ArgumentAdapter<T, String> {
    override fun decode(value: String): T = enumValues.first { it.name == value }

    override fun encode(value: T) = value.name
}

inline fun <reified T : Enum<T>> EnumArgumentAdapter(): EnumArgumentAdapter<T> {
    return EnumArgumentAdapter(enumValues())
}
