package dev.brella.khronus.proxy

import com.google.common.collect.BiMap
import dev.brella.khronus.Khronus
import dev.brella.khronus.TickDog
import dev.brella.khronus.api.KhronusApi
import dev.brella.khronus.networking.KhronusNetworking
import dev.brella.khronus.networking.KhronusUpdateTickLengthsMessage
import kotlinx.coroutines.*
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.world.World
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.registries.GameData
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class KhronusProxy {
    inline val logger get() = Khronus.logger
    val ITEMS_TO_BLOCKS: BiMap<Item, Block> by lazy { GameData.getBlockItemMap().inverse() }

    private val worldJobs: MutableMap<World, Job> = WeakHashMap()

    private var serverUpdateJob: Job? = null

    abstract suspend fun <T> runIn(world: World, block: suspend CoroutineScope.() -> T): T

    open fun serverStarting(event: FMLServerStartingEvent) {}

    open fun registerItemRenderer(item: Item, meta: Int, id: String) {}

    @SubscribeEvent
    fun loadWorld(event: WorldEvent.Load) {
        val world = event.world

        if (world.isRemote) return

        worldJobs.remove(world)?.cancel()
        worldJobs[world] = TickDog.launch {
            while (isActive) {
                delay(5_000)

                if (world.playerEntities.isNotEmpty()) {
                    val updateTicks = KhronusUpdateTickLengthsMessage()
                    updateTicks.dimensionID = world.provider.dimension

                    KhronusApi.getTickLength(world)
                        .forEach { (te, long) -> updateTicks.tickLengths[te.pos.toLong()] = long }

                    KhronusNetworking.INSTANCE.sendToDimension(updateTicks, world.provider.dimension)
                }
            }
        }
    }

    @SubscribeEvent
    fun unloadWorld(event: WorldEvent.Unload) {
        worldJobs.remove(event.world)?.cancel()
    }
}