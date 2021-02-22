package dev.brella.khronus.networking

import dev.brella.khronus.api.KhronusApi
import io.netty.buffer.ByteBuf
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import kotlin.math.roundToInt

class KhronusRequestTicksMessage : IMessage {
    var requestRadius: Int = 4
    var dimensionID: Int? = null

    override fun fromBytes(buf: ByteBuf) {
        requestRadius = buf.readIntLE()
        dimensionID = buf.readIntLE().takeUnless { it == Int.MAX_VALUE }
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeIntLE(requestRadius)
        buf.writeIntLE(dimensionID ?: Int.MAX_VALUE)
    }

    object Handler : IMessageHandler<KhronusRequestTicksMessage, KhronusUpdateTickLengthsMessage?> {
        override fun onMessage(
            message: KhronusRequestTicksMessage,
            ctx: MessageContext
        ): KhronusUpdateTickLengthsMessage? {
            if (message.requestRadius > 0) {
                val player = ctx.serverHandler.player
                val world = player.world

                val xRange = (player.posX - (message.requestRadius shl 4)).roundToInt() .. (player.posX + (message.requestRadius shl 4)).roundToInt()
                val zRange = (player.posZ - (message.requestRadius shl 4)).roundToInt() .. (player.posZ + (message.requestRadius shl 4)).roundToInt()

                KhronusApi.getTickLength(world)
                    .let { map ->
                        if (map.isNotEmpty()) {
                            val msg = KhronusUpdateTickLengthsMessage()
                            msg.dimensionID = message.dimensionID ?: world.provider.dimension

                            map.forEach { (te, long) -> if (te.pos.x in xRange && te.pos.z in zRange) msg.addTickEntity(te, long.toInt()) }

                            if (msg.existingTickEntities.isNotEmpty()) return msg
                        }
                    }
            }


            return null
        }
    }
}