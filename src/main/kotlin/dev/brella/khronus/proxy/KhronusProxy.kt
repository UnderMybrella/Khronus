package dev.brella.khronus.proxy

import com.google.common.collect.BiMap
import dev.brella.khronus.Khronus
import dev.brella.khronus.TickDog
import dev.brella.khronus.watchdogs.KhronusWatchdog
import kotlinx.coroutines.*
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.world.World
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.registries.GameData
import java.util.*

abstract class KhronusProxy {
    inline val logger get() = Khronus.logger
    val ITEMS_TO_BLOCKS: BiMap<Item, Block> by lazy { GameData.getBlockItemMap().inverse() }

    private val worldJobs: MutableMap<World, Job> = WeakHashMap()
    private val worldWatchdogs: MutableMap<World, KhronusWatchdog> = WeakHashMap()

    private var serverUpdateJob: Job? = null

    abstract suspend fun <T> runIn(world: World, block: suspend CoroutineScope.() -> T): T

    open fun serverStarting(event: FMLServerStartingEvent) {}

    open fun registerItemRenderer(item: Item, meta: Int, id: String) {}

    //TODO: Allow different watchdogs per world
    fun onWatchdogSwitch(old: KhronusWatchdog, new: KhronusWatchdog) {
        val entries = worldWatchdogs.entries

        entries.forEach { (world, watchdog) -> if (watchdog == old) old.onRemovedFrom(world, new) }
        entries.forEach { (world, watchdog) -> if (watchdog == old) new.onAddedTo(world, old) }
        entries.forEach { (world, watchdog) -> if (watchdog == old) worldWatchdogs[world] = new }
    }

    @SubscribeEvent
    fun loadWorld(event: WorldEvent.Load) {
        val world = event.world

        worldWatchdogs[world] = TickDog.watchdog

        if (world.isRemote) return

        worldJobs.remove(world)?.cancel()
//        worldJobs[world] = TickDog.launch {
//            loop@ while (isActive) {
//                delay(5_000)
//
//                try {
//                    if (world.playerEntities.isNotEmpty()) {
//                        KhronusApi.getTickLength(world)
//                            .let { map ->
//                                if (map.isNotEmpty()) {
//                                    val updateTicks = KhronusUpdateTickLengthsMessage()
//                                    updateTicks.dimensionID = world.provider.dimension
//
//                                    val tmp = KhronusRequestTicksMessage()
//
//                                    map.forEach { (te, long) ->
//                                        updateTicks.tickLengths[te.pos.toLong()] = long
//
//                                        tmp.addTickEntity(te, long.toInt())
//                                    }
//
//                                        KhronusNetworking.INSTANCE.sendToDimension(updateTicks,
//                                            world.provider.dimension)
//                                        KhronusNetworking.INSTANCE.sendToDimension(tmp, world.provider.dimension)
//                                } else {
//                                    delay(100)
//                                }
//                            }
//                    }
//                } catch (th: Throwable) {
//                    th.printStackTrace()
//                }
//            }
//        }
    }

    @SubscribeEvent
    fun unloadWorld(event: WorldEvent.Unload) {
        worldWatchdogs.remove(event.world)
        worldJobs.remove(event.world)?.cancel()
    }
}