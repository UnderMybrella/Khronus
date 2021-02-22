package dev.brella.khronus

import io.netty.buffer.ByteBuf
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.math.floor
import kotlin.reflect.KProperty

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

fun ByteBuf.readVarInt16(): Int {
    var num: Int = readByte().toInt() and 0xFF

    if (num > 0x7F) {
        num = (num and 0x7F) shl 7
        num = num or (readByte().toInt() and 0xFF)
    }

    return num
}

fun ByteBuf.writeVarInt16(num: Int) {
    if (num > 0x7F) {
        writeByte(0x80 or (num shr 7))
        writeByte(num and 0x7F)
    } else {
        writeByte(num)
    }
}

//fun ByteBuf.readVarInt24(): Int {
//    var num: Int = readByte().toInt() and 0xFF
//
//    if (num > 0x7F) {
//        num = (num and 0x7F) shl 7
//        num = num or (readByte().toInt() and 0xFF)
//    }
//
//    return num
//}
//
//fun ByteBuf.writeVarInt24(num: Int) {
//    if (num > 0x7F) {
//        writeByte(0x80 or (num shr 7))
//        writeByte(num and 0x7F)
//    } else {
//        writeByte(num)
//    }
//}

fun Method.toMethodHandle(): MethodHandle = MethodHandles.lookup().unreflect(this.apply { isAccessible = true })
inline fun <reified T, reified R> Field.toFieldHandle(): FieldHandle<T, R> {
    isAccessible = true
    return FieldHandle(Pair(MethodHandles.lookup().unreflectGetter(this).asType(MethodType.methodType(R::class.java, T::class.java)), MethodHandles.lookup().unreflectSetter(this).asType(MethodType.methodType(Void.TYPE, T::class.java, R::class.java))))
}

@Suppress("UNCHECKED_CAST")
inline class FieldHandle<T, R>(val handles: Pair<MethodHandle, MethodHandle>) {
    inline fun get(instance: T): R = handles.first.invoke(instance) as R
    inline operator fun set(instance: T, value: R): Any? = handles.second.invoke(instance, value)

    inline operator fun getValue(t: T, property: KProperty<*>): R = get(t)
    inline operator fun setValue(t: T, property: KProperty<*>, value: R) {
        set(t, value)
    }
}