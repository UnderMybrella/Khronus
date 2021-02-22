package dev.brella.khronus.networking

import dev.brella.khronus.Khronus.MOD_ID
import net.minecraft.entity.Entity
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.relauncher.Side

object KhronusNetworking {
    val INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID)

    const val UPDATE_TICK_LENGTHS = 0
    const val REQUEST_TICKS = 1

//    fun setPortalController(portal: TileEntityGatePortal) {
//        INSTANCE.sendToAllTracking(
//                GatePortalSetControllerMessage(portal.world.provider.dimension, portal.pos, portal.controller ?: return),
//                portal
//        )
//    }

    fun registerMessages() {
        INSTANCE.registerMessage(KhronusUpdateTickLengthsMessage.Handler, KhronusUpdateTickLengthsMessage::class.java, UPDATE_TICK_LENGTHS, Side.CLIENT)
        INSTANCE.registerMessage(KhronusRequestTicksMessage.Handler, KhronusRequestTicksMessage::class.java, REQUEST_TICKS, Side.SERVER)
    }
}