package dev.brella.khronus.watchdogs

import dev.brella.khronus.api.TemporalBounds
import dev.brella.khronus.api.IKhronusTickable
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class KhronusWorld: KhronusWatchdog {
//    val khronusTileEntities = WeakHashMap<World, MutableMap<TileEntity, TickDog.TemporalBounds>>()
//    val khronusTickAcceleration = WeakHashMap<World, MutableMap<TileEntity, Int>>()

//    inline fun World.khronusTickAcceleration() =
//        khronusTickAcceleration.computeIfAbsent(this) { ConcurrentHashMap() }

    override fun addTileEntity(world: World, tileEntity: TileEntity) {
        when (tileEntity) {
            is IKhronusTickable -> world.khronusTickableTileEntities[tileEntity] =
                TemporalBounds(1, null, null)

            is ITickableTileEntity -> world.tickableTileEntities.add(tileEntity)
        }
    }

    override fun removeTileEntity(world: World, pos: BlockPos, tileEntity: TileEntity) {
        world.tickableTileEntities.remove(tileEntity)
        world.khronusTickableTileEntities.remove(tileEntity)
    }

    override fun onRemovedFrom(world: World, replacingWith: KhronusWatchdog) {
        if (replacingWith !is KhronusWorld) {
            world.khronusTickableTileEntities.forEach { (tile) -> world.tickableTileEntities.add(tile) }
        }
    }

    override fun onAddedTo(world: World, replacing: KhronusWatchdog) {
        if (replacing !is KhronusWorld) {
            val map = world.khronusTickableTileEntities
            map.clear()

            val iterator = world.tickableTileEntities.listIterator()

            while (iterator.hasNext()) {
                val tile = iterator.next()

                if (tile is IKhronusTickable) {
                    map[tile] = TemporalBounds(1, null, null)
                    iterator.remove()
                }
            }
        }
    }
}