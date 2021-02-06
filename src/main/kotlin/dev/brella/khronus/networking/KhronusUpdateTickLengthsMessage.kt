package dev.brella.khronus.networking

import dev.brella.khronus.Khronus
import net.minecraft.network.PacketBuffer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.network.NetworkEvent
import java.util.function.Supplier

data class KhronusUpdateTickLengthsMessage(var dimension: ResourceLocation, val tickLengths: Map<Long, Long>) {
    companion object {
        fun encode(msg: KhronusUpdateTickLengthsMessage, buf: PacketBuffer) {
            buf.writeResourceLocation(msg.dimension)
            buf.writeIntLE(msg.tickLengths.size)

            msg.tickLengths.forEach { (pos, len) -> buf.writeLongLE(pos); buf.writeLongLE(len) }
        }

        fun decode(buf: PacketBuffer): KhronusUpdateTickLengthsMessage =
            KhronusUpdateTickLengthsMessage(buf.readResourceLocation(), HashMap<Long, Long>().apply {
                repeat(buf.readIntLE()) {
                    this[buf.readLongLE()] = buf.readLongLE()
                }
            })

        fun handle(msg: KhronusUpdateTickLengthsMessage, context: Supplier<NetworkEvent.Context>) {
            val proxy = Khronus.clientProxy ?: return
            if (msg.dimension != proxy.tickTimesDimension) {
                proxy.tickTimes.clear()
                proxy.tickTimesDimension = msg.dimension
            }

            proxy.tickTimes.putAll(msg.tickLengths)
        }
    }
}