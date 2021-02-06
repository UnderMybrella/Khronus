package dev.brella.khronus.networking

import dev.brella.khronus.Khronus.MOD_ID
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.network.NetworkDirection
import net.minecraftforge.fml.network.NetworkRegistry
import java.util.*

object KhronusNetworking {
    const val PROTOCOL_VERSION = "1"
    val INSTANCE = NetworkRegistry.newSimpleChannel(
        ResourceLocation(MOD_ID, "main"),
        { PROTOCOL_VERSION },
        { true },
        { true }
    )

    const val UPDATE_TICK_LENGTHS = 0

    @Suppress("INACCESSIBLE_TYPE")
    fun registerMessages() {
        INSTANCE.registerMessage(UPDATE_TICK_LENGTHS,
            KhronusUpdateTickLengthsMessage::class.java,
            KhronusUpdateTickLengthsMessage.Companion::encode,
            KhronusUpdateTickLengthsMessage.Companion::decode,
            KhronusUpdateTickLengthsMessage.Companion::handle,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        )
    }
}