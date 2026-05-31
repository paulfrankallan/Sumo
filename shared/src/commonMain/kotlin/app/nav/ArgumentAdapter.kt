package app.nav

interface ArgumentAdapter<T : Any, S> {
    fun decode(value: S): T
    fun encode(value: T): S
}