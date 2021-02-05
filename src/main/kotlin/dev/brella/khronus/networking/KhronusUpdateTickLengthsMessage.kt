package dev.brella.khronus.networking

import dev.brella.khronus.Khronus
import dev.brella.khronus.proxy.ClientProxy
import io.netty.buffer.ByteBuf
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class KhronusUpdateTickLengthsMessage: IMessage {
    var dimensionID: Int? = null
    val tickLengths: MutableMap<Long, Long> = HashMap()

    override fun fromBytes(buf: ByteBuf) {
        dimensionID = buf.readIntLE()
        val len = buf.readIntLE()

        repeat(len) { tickLengths[buf.readLongLE()] = buf.readLongLE() }
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeIntLE(dimensionID ?: 0)
        buf.writeIntLE(tickLengths.size)

        tickLengths.forEach { (pos, len) -> buf.writeLongLE(pos); buf.writeLongLE(len) }
    }

    object Handler: IMessageHandler<KhronusUpdateTickLengthsMessage, IMessage?> {
        override fun onMessage(message: KhronusUpdateTickLengthsMessage, ctx: MessageContext): IMessage? {
            val proxy = Khronus.proxy as ClientProxy
            if (message.dimensionID != proxy.tickTimesDimension) {
                proxy.tickTimes.clear()
                proxy.tickTimesDimension = message.dimensionID
            }

            proxy.tickTimes.putAll(message.tickLengths)

            return null
        }
    }
}