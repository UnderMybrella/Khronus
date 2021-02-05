package dev.brella.khronus.proxy

import dev.brella.khronus.ThreadListenerDispatcher
import dev.brella.khronus.TickDog
import dev.brella.khronus.api.KhronusApi
import dev.brella.khronus.networking.KhronusNetworking
import dev.brella.khronus.networking.KhronusUpdateTickLengthsMessage
import kotlinx.coroutines.*
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.event.FMLServerStartedEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

class ServerProxy : KhronusProxy() {
    private val worldDispatchers: MutableMap<Int, CoroutineDispatcher> = ConcurrentHashMap()

    private var fallbackDispatcher: CoroutineDispatcher = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            logger.error(IllegalStateException("Error, attempting to dispatch $block on a main thread before server has started, this is a very bad bug!").fillInStackTrace())
        }
    }

    override fun serverStarting(event: FMLServerStartingEvent) {
        fallbackDispatcher = ThreadListenerDispatcher(event.server)

//        serverUpdateJob?.cancel()
//        serverUpdateJob = TickDog.launch {
//            while (isActive) {
//                delay(5_000)
//
//
//            }
//        }
    }

    override suspend fun <T> runIn(world: World, block: suspend CoroutineScope.() -> T): T =
            withContext(when (world) {
                is WorldServer -> worldDispatchers.computeIfAbsent(world.provider.dimension) { ThreadListenerDispatcher(world) }
                else -> fallbackDispatcher
            }, block)
}