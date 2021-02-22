package dev.brella.khronus.data

import dev.brella.khronus.readVarInt16
import dev.brella.khronus.writeVarInt16
import io.netty.buffer.ByteBuf
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import java.io.File
import java.util.*

sealed class TickEntity {
    abstract val x: Int
    abstract val y: Int
    abstract val z: Int
    abstract val key: ResourceLocation

    data class TE(override val x: Int, override val y: Int, override val z: Int, override val key: ResourceLocation) :
        TickEntity()
}

sealed class ChunkTickData {
    abstract val posX: Int
    abstract val posZ: Int
    abstract val posY: Int
    abstract val timing: Int
    abstract val usingMilliseconds: Boolean

    companion object {
        const val MILLISECONDS_VALUE = 0x8000
        const val MILLISECONDS_MASK = 0x7FFF

        fun fromBytes(buf: ByteBuf): WithMapping {
            val pos = buf.readShortLE().toInt().and(0xFFFF)
            val tileID = buf.readVarInt16()
            val timing = buf.readUnsignedShortLE()

            return WithMapping(
                posX = (pos shr 0) and 0xF,
                posZ = (pos shr 4) and 0xF,
                posY = (pos shr 8) and 0xFF,
                tileID = tileID,
                timing = timing and MILLISECONDS_MASK,
                usingMilliseconds = timing and MILLISECONDS_VALUE == MILLISECONDS_VALUE
            )
        }

        fun fromBytes(buf: ByteBuf, mappings: Array<ResourceLocation>, chunkX: Int, chunkZ: Int): WithKey {
            val pos = buf.readShortLE().toInt().and(0xFFFF)
            val keyMapping = buf.readVarInt16()
            val timing = buf.readUnsignedShortLE()

            if (keyMapping !in mappings.indices) {
                println("Wuh oh >:(")
                println()
            }

            return WithKey(
                posX = (pos shr 0) and 0xF or chunkX,
                posZ = (pos shr 4) and 0xF or chunkZ,
                posY = (pos shr 8) and 0xFF,
                key = mappings[keyMapping],
                timing = timing and MILLISECONDS_MASK,
                usingMilliseconds = timing and MILLISECONDS_VALUE == MILLISECONDS_VALUE
            )
        }

        fun fromRequestBytes(buf: ByteBuf, mappings: Array<ResourceLocation>, chunkX: Int, chunkZ: Int): WithKey {
            val pos = buf.readShortLE().toInt().and(0xFFFF)
            val keyMapping = buf.readVarInt16()

            if (keyMapping !in mappings.indices) {
                println("Wuh oh >:(")
            }

            return WithKey(
                posX = (pos shr 0) and 0xF or chunkX,
                posZ = (pos shr 4) and 0xF or chunkZ,
                posY = (pos shr 8) and 0xFF,
                key = mappings[keyMapping],
                timing = -1,
                usingMilliseconds = false
            )
        }
    }

    data class WithKey(
        override val posX: Int, override val posY: Int, override val posZ: Int, val key: ResourceLocation,
        override val timing: Int, override val usingMilliseconds: Boolean
    ) : ChunkTickData() {
        constructor(posX: Int, posY: Int, posZ: Int, key: ResourceLocation, timing: Int): this(posX, posY, posZ, key, if (timing and MILLISECONDS_VALUE == MILLISECONDS_VALUE) timing / 1000 else timing, timing and MILLISECONDS_VALUE == MILLISECONDS_VALUE)

        override fun toBytes(buf: ByteBuf, mappings: Map<ResourceLocation, Int>) {
            buf.writeShortLE(
                (posX and 0xF)
                        or (posZ and 0xF shl 4)
                        or (posY and 0xFF shl 8)
            )

            buf.writeVarInt16(mappings[key] ?: -1)
            if (usingMilliseconds) {
                buf.writeShortLE((timing and MILLISECONDS_MASK) or MILLISECONDS_VALUE)
            } else {
                buf.writeShortLE(timing and MILLISECONDS_MASK)
            }
        }

        override fun toRequestBytes(buf: ByteBuf, mappings: Map<ResourceLocation, Int>) {
            buf.writeShortLE(
                (posX and 0xF)
                        or (posZ and 0xF shl 4)
                        or (posY and 0xFF shl 8)
            )

            buf.writeVarInt16(mappings[key] ?: -1)
        }
    }

    data class WithMapping(
        override val posX: Int, override val posY: Int, override val posZ: Int, val tileID: Int,
        override val timing: Int, override val usingMilliseconds: Boolean
    ) : ChunkTickData() {
        constructor(posX: Int, posY: Int, posZ: Int, tileID: Int, timing: Int): this(posX, posY, posZ, tileID, if (timing and MILLISECONDS_VALUE == MILLISECONDS_VALUE) timing / 1000 else timing, timing and MILLISECONDS_VALUE == MILLISECONDS_VALUE)

        override fun toBytes(buf: ByteBuf, mappings: Map<ResourceLocation, Int>) {
            buf.writeShortLE(
                (posX and 0xF)
                        or (posZ and 0xF shl 4)
                        or (posY and 0xFF shl 8)
            )

            buf.writeVarInt16(tileID)
            if (usingMilliseconds) {
                buf.writeIntLE((timing and MILLISECONDS_MASK) or MILLISECONDS_VALUE)
            } else {
                buf.writeIntLE(timing and MILLISECONDS_MASK)
            }
        }

        override fun toRequestBytes(buf: ByteBuf, mappings: Map<ResourceLocation, Int>) {
            buf.writeShortLE(
                (posX and 0xF)
                        or (posZ and 0xF shl 4)
                        or (posY and 0xFF shl 8)
            )

            buf.writeVarInt16(tileID)
        }
    }

    abstract fun toBytes(buf: ByteBuf, mappings: Map<ResourceLocation, Int>)
    abstract fun toRequestBytes(buf: ByteBuf, mappings: Map<ResourceLocation, Int>)
}