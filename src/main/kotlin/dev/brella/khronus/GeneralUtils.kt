package dev.brella.khronus

import kotlin.math.floor
import kotlin.math.round
import kotlin.math.roundToInt

typealias PredicateKt<T> = (obj: T) -> Boolean

inline fun <T> Array<T>.getWrapped(index: Int): T = this[Math.floorMod(index, size)]
inline fun <T> Array<T>.getWrapped(index: Number): T = this[Math.floorMod(index.toInt(), size)]

inline fun <T> debug(line: String, block: () -> T): T {
    println(line)
    return block()
}

const val MICROSECOND_APPROX = (1.0 / 0.967)

inline fun Long.nanosecondsToMicrosecondApprox(): Long =
    this shr 10

inline fun Long.microsecondApproxToFloat(): Float =
    (this * MICROSECOND_APPROX).toFloat()

inline fun Double.toTwoDecimalPlaces(): String =
    (floor(this * 100) / 100.0).toString()

inline fun Float.toTwoDecimalPlaces(): String =
    (floor(this * 100) / 100.0).toString()