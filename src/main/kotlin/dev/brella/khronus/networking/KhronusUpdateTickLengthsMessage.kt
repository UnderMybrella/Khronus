package dev.brella.khronus.networking

import dev.brella.khronus.Khronus
import dev.brella.khronus.data.ChunkTickData
import dev.brella.khronus.proxy.ClientProxy
import dev.brella.khronus.readVarInt16
import dev.brella.khronus.writeVarInt16
import io.netty.buffer.ByteBuf
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class KhronusUpdateTickLengthsMessage : IMessage {
    var dimensionID: Int? = null

    val existingTickEntities: MutableList<ChunkTickData> = ArrayList()

    inline fun addTickEntity(te: TileEntity, timing: Int) =
        TileEntity.getKey(te.javaClass)
            ?.let { existingTickEntities.add(ChunkTickData.WithKey(te.pos.x, te.pos.y, te.pos.z, it, timing)) }

    override fun fromBytes(buf: ByteBuf) {
        dimensionID = buf.readIntLE().takeUnless { it == Int.MAX_VALUE }

        val tileEntitySize: Int = buf.readVarInt16()

        val tileMappings = Array(tileEntitySize) {
            ResourceLocation(buf.readCharSequence(buf.readVarInt16(), Charsets.UTF_8).toString())
        }

        val chunkCount = buf.readVarInt16()

        repeat(chunkCount) { i ->
            val chunkPos = buf.readIntLE()
            val chunkSize = buf.readVarInt16()

            val chunkX = (chunkPos shr 16).toShort().toInt() shl 4
            val chunkZ = chunkPos.toShort().toInt() shl 4

            repeat(chunkSize) { j ->
                existingTickEntities.add(ChunkTickData.fromBytes(buf, tileMappings, chunkX, chunkZ))
            }
        }
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeIntLE(dimensionID ?: Int.MAX_VALUE)

        val tileMappings: MutableMap<ResourceLocation, Int> = HashMap()
        val chunks: MutableMap<Int, MutableList<ChunkTickData>> = HashMap()

        existingTickEntities.forEach { tickEntity ->
            if (tickEntity is ChunkTickData.WithKey)
                tileMappings.compute(tickEntity.key) { _, v -> v?.plus(1) ?: 1 }
        }

        tileMappings.entries.sortedBy(Map.Entry<ResourceLocation, Int>::value).let { list ->
            tileMappings.clear()
            list.forEachIndexed { index, entry -> tileMappings[entry.key] = index }
        }

        existingTickEntities.forEach { tickEntity ->
            chunks.computeIfAbsent(((tickEntity.posX shr 4) shl 16) or (tickEntity.posZ shr 4)) { ArrayList() }
                .add(tickEntity)
        }

        if (tileMappings.size > 0xFFFF) {
            Khronus.logger.warn("Warning, more Tile Entities near us than we're prepared for: expected 0 ≤ n ≤ 65535, got 0 < 65535 < {}",
                tileMappings.size)
            return
        }

        buf.writeVarInt16(tileMappings.size)

        tileMappings.entries.sortedBy(Map.Entry<ResourceLocation, Int>::value).forEach { (key) ->
            val keyStr = key.toString().toByteArray(Charsets.UTF_8)
            buf.writeVarInt16(keyStr.size)
            buf.writeBytes(keyStr)
        }

        buf.writeVarInt16(chunks.size)

        chunks.entries.forEachIndexed { index, (chunkPos, list) ->
            buf.writeIntLE(chunkPos)

            if (list.size > 0xFFFF) {
                Khronus.logger.error("Error, more Tile Entities at ${chunkPos shr 16 and 0xFFFF},${chunkPos and 0xFFFF} than we're prepared for: expected 0 ≤ n ≤ 65535, got 0 < 65535 < {}",
                    tileMappings.size)
                return
            }

            val size = list.size

            buf.writeVarInt16(size)

            list.forEachIndexed { j, chunkTickData ->
                chunkTickData.toBytes(buf, tileMappings)
            }
        }

        println("Saved ${this.existingTickEntities.size} in ${buf.writerIndex()} bytes")
    }


    object Handler : IMessageHandler<KhronusUpdateTickLengthsMessage, IMessage?> {
        override fun onMessage(message: KhronusUpdateTickLengthsMessage, ctx: MessageContext): IMessage? {
            val proxy = Khronus.proxy as ClientProxy
            if (message.dimensionID != proxy.tickTimesDimension) {
                proxy.tickTimesDimension = message.dimensionID
            }

            proxy.tickTimes.clear()
            proxy.tickTimes.addAll(message.existingTickEntities)

            proxy.lagThreshold = proxy.tickTimes.let { list ->
                var avg = 0L

                list.forEach { data ->
                    if (data.timing != -1) {
                        avg += if (data.usingMilliseconds) data.timing * 1000 else data.timing
                    }
                }

                return@let avg / list.size.toDouble()
            }

            proxy.lagList.clear()
            proxy.tickTimes.sortedByDescending(ChunkTickData::timing)
                .filterTo(proxy.lagList) { data -> data.timing != -1 && (if (data.usingMilliseconds) data.timing * 1000 > proxy.lagThreshold else data.timing > proxy.lagThreshold) }

            proxy.waiting.set(false)

            return null
        }
    }
}