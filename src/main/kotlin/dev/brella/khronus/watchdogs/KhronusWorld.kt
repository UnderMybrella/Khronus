package dev.brella.khronus.watchdogs

import dev.brella.khronus.api.TemporalBounds
import dev.brella.khronus.api.IKhronusTickable
import dev.brella.khronus.api.KhronusApi
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ITickable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

abstract class KhronusWorld : KhronusWatchdog {
//    val khronusTileEntities = WeakHashMap<World, MutableMap<TileEntity, TickDog.TemporalBounds>>()
//    val khronusTickAcceleration = WeakHashMap<World, MutableMap<TileEntity, Int>>()

//    inline fun World.khronusTickAcceleration() =
//        khronusTickAcceleration.computeIfAbsent(this) { ConcurrentHashMap() }

    override fun addTileEntity(world: World, tileEntity: TileEntity) {
        when {
            tileEntity is IKhronusTickable<*> -> {
                world.khronusTickableTileEntities[tileEntity] =
                    TemporalBounds(1, null, null)

                tileEntity.onAddedTo(world)
            }

            tileEntity.hasCapability(KhronusApi.KHRONUS_TICKABLE, null) ->
                tileEntity.getCapability(KhronusApi.KHRONUS_TICKABLE, null)?.let { kte ->
                    world.khronusTickableTileEntities[kte] = TemporalBounds(1, null, null)

                    kte.onAddedTo(world)
                }

            tileEntity is ITickable -> world.tickableTileEntities.add(tileEntity)
        }
    }

    override fun removeTileEntity(world: World, pos: BlockPos, tileEntity: TileEntity) {
        when {
            tileEntity is IKhronusTickable<*> -> {
                world.khronusTickableTileEntities.remove(tileEntity)
                tileEntity.onRemovedFrom(world)
            }
            tileEntity.hasCapability(KhronusApi.KHRONUS_TICKABLE, null) ->
                tileEntity.getCapability(KhronusApi.KHRONUS_TICKABLE, null)?.let { kte ->
                    world.khronusTickableTileEntities.remove(kte)

                    kte.onRemovedFrom(world)
                }
            tileEntity is ITickable -> world.tickableTileEntities.remove(tileEntity)
        }

//        world.tickableTileEntities.remove(tileEntity)
//        world.khronusTickableTileEntities.remove(tileEntity)
    }

    override fun onRemovedFrom(world: World, replacingWith: KhronusWatchdog) {
        if (replacingWith !is KhronusWorld) {
            world.delayedTickableTileEntities.forEach { (tile) -> world.tickableTileEntities.add(tile) }
            world.khronusTickableTileEntities.forEach { (tile) -> world.tickableTileEntities.add(tile.source) }
        }
    }

    override fun onAddedTo(world: World, replacing: KhronusWatchdog) {
        if (replacing !is KhronusWorld) {
//            val map = world.khronusTickableTileEntities
//            map.clear()

            //TODO: Optimise slightly
            val temporalEntities: MutableMap<TileEntity, IKhronusTickable<*>> = HashMap()

            world.tickableTileEntities.forEach { tile ->
                if (tile is IKhronusTickable<*>) {
//                    map[tile] = TemporalBounds(1, null, null)
//                    iterator.remove()

                    temporalEntities[tile] = tile
                    tile.onWatchdogChanged(world, replacing, this)
                } else if (tile.hasCapability(KhronusApi.KHRONUS_TICKABLE, null)) {
                    tile.getCapability(KhronusApi.KHRONUS_TICKABLE, null)?.let { kte ->
//                        map[kte] = TemporalBounds(1, null, null)
//                        iterator.remove()

                        temporalEntities[tile] = kte
                        kte.onWatchdogChanged(world, replacing, this)
                    }
                }
            }

            world.tickableTileEntities.removeAll(temporalEntities.keys)

            world.khronusTickableTileEntities.clear()
            world.khronusTickableTileEntities.putAll(temporalEntities
                .mapKeys(Map.Entry<TileEntity, IKhronusTickable<*>>::value)
                .mapValues { TemporalBounds(1, null, null) }
            )
        }
    }
}