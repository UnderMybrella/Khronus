package dev.brella.khronus.watchdogs

import dev.brella.khronus.TickDog
import dev.brella.khronus.api.KhronusApi
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ITickable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface KhronusWatchdog {
    fun updateEntities(world: World)
    fun addTileEntity(world: World, tileEntity: TileEntity) {
        if (tileEntity is ITickable) world.tickableTileEntities.add(tileEntity)
    }

    fun removeTileEntity(world: World, pos: BlockPos, tileEntity: TileEntity) {
        world.tickableTileEntities.remove(tileEntity)
    }

    fun onRemovedFrom(world: World, replacingWith: KhronusWatchdog) {}
    fun onAddedTo(world: World, replacing: KhronusWatchdog) {}
}

inline val World.delayedTickableTileEntities get() = KhronusApi.getDelayedTileEntities(this)
inline val World.khronusTickableTileEntities get() = KhronusApi.getKhronusTileEntities(this)
inline val World.tickAcceleration get() = KhronusApi.getTickAcceleration(this)
inline val World.tickCheckup get() = KhronusApi.getTickCheckup(this)
inline val World.tickLength get() = KhronusApi.getTickLength(this)