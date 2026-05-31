package app.util

import androidx.navigation.NavController
import kotlin.random.Random

inline fun <reified T : Enum<T>> random(): T =
    enumValues<T>()[Random.nextInt(enumValues<T>().size)]

fun NavController.popBackStackOrNavToRoute(route: String) {
    if (!popBackStack()) navigate(route)
}

fun Double.truncateToDecimalPlaces(places: Int): String {
    val stringValue = this.toString()
    val indexOfDecimal = stringValue.indexOf('.')
    return if (indexOfDecimal != -1) {
        stringValue.substring(0, minOf(indexOfDecimal + places + 1, stringValue.length))
    } else {
        stringValue
    }
}

fun Boolean?.isTrue() = this == true
fun Boolean?.isFalse() = this == false
fun Any?.isNull() = this == null
fun Any?.isNotNull() = this != null
fun Boolean?.isNullOrFalse() = (this == null) || (this == false)
fun Any?.returnIfNull() {
    this ?: return
}

fun Boolean?.ifTrue(block: () -> Unit) {
    if (this == true) {
        block()
    }
}

fun Boolean?.ifFalse(block: () -> Unit) {
    if (this == false) {
        block()
    }
}

fun Boolean?.ifNull(block: () -> Unit) {
    if (this == null) {
        block()
    }
}

fun Boolean?.ifNotNull(block: () -> Unit) {
    if (this != null) {
        block()
    }
}

fun String?.isNotNullOrEmpty() = !this.isNullOrEmpty()

fun <T, K, V> List<T>.groupByNotNull(
    keySelector: (T) -> K?,
    valueTransform: (T) -> V
): Map<K, Set<V>> {
    return this.mapNotNull { item ->
        val key = keySelector(item)
        if (key != null) key to valueTransform(item) else null
    }.groupBy({ it.first }, { it.second }).mapValues { it.value.toSet() }
}

fun Int.toOrdinal(): String {
    val suffixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
    return when (this % 100) {
        11, 12, 13 -> this.toString() + "th"
        else -> this.toString() + suffixes[this % 10]
    }
}

fun Long.toOrdinal(): String {
    val suffixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
    return when (this % 100) {
        11L, 12L, 13L -> this.toString() + "th"
        else -> this.toString() + suffixes[(this % 10L).toInt()]
    }
}

fun <TYPE> List<TYPE>.padEndOrCompact(size: Int, fallback: TYPE): List<TYPE> {
    if (size < 0) throw IllegalArgumentException("Desired length $size is less than zero.")
    return (0 until size).map { this.getOrNull(it) ?: fallback }
}

fun <TYPE> List<TYPE>.padEnd(size: Int, fallback: TYPE): List<TYPE> {
    if (size < 0) throw IllegalArgumentException("Desired length $size is less than zero.")
    return when {
        size > this.size -> this.padEndOrCompact(size, fallback)
        else -> this
    }
}

fun <TYPE> List<TYPE>.padEnd(size: Int, fallback: (Int) -> TYPE): List<TYPE> {
    if (size < 0) throw IllegalArgumentException("Desired length $size is less than zero.")
    return if (size > this.size) {
        this + List(size - this.size) { index -> fallback(this.size + index) }
    } else {
        this
    }
}

fun <TYPE> List<TYPE>.padStartOrCompact(size: Int, fallback: TYPE): List<TYPE> {
    if (size < 0) throw IllegalArgumentException("Desired length $size is less than zero.")
    return ((0 until size - this.size).map { fallback } + this).takeLast(size)
}

fun <TYPE> List<TYPE>.padStart(size: Int, fallback: TYPE): List<TYPE> {
    if (size < 0) throw IllegalArgumentException("Desired length $size is less than zero.")
    return when {
        size > this.size -> this.padStartOrCompact(size, fallback)
        else -> this
    }
}