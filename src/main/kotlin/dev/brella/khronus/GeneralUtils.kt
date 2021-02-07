package dev.brella.khronus

import kotlin.math.floor

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

internal inline val logger get() = Khronus.logger

inline fun hsvToRgb(hue: Float, saturation: Float, brightness: Float, block: (red: Float, green: Float, blue: Float) -> Unit) {

    // normalize the hue
    val normalizedHue = ((hue % 360 + 360) % 360).toDouble()
    val hue = (normalizedHue / 360).toFloat()

    if (saturation == 0f) {
        block(brightness, brightness, brightness)
    } else {
        val h = (hue - floor(hue)) * 6f
        val f = h - floor(h)
        val p = brightness * (1f - saturation)
        val q = brightness * (1f - saturation * f)
        val t = brightness * (1f - saturation * (1f - f))
        when (h.toInt()) {
            0 -> block(brightness, t, p)
            1 -> block(q, brightness, p)
            2 -> block(p, brightness, t)
            3 -> block(p, q, brightness)
            4 -> block(t, p, brightness)
            5 -> block(brightness, p, q)
        }
    }
}