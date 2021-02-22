package dev.brella.khronus.examples.entity

import dev.brella.khronus.TickDog
import dev.brella.khronus.api.IKhronusTickable
import dev.brella.khronus.api.KhronusApi
import dev.brella.khronus.watchdogs.KhronusWorld
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ITickable
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

class TileEntityWarpDrive : TileEntity(), IKhronusTickable<TileEntityWarpDrive> {
    var warpDriveBonusTicks: Int = 18
    val blacklist = listOf("khronus:warp_drive", "projecte:dm_pedestal")

    var active = false
    var previousRedstoneState = false

    override fun readFromNBT(compound: NBTTagCompound) {
        super.readFromNBT(compound)

        active = compound.getBoolean("active")
        previousRedstoneState = compound.getBoolean("previous_redstone_state")
    }

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        return super.writeToNBT(compound).apply {
            setBoolean("active", active)
            setBoolean("previous_redstone_state", previousRedstoneState)
        }
    }

    override fun update(ticks: Int, bonusTicks: Int) {
        if (world.isRemote) return

        if (active) {
            val bounds = AxisAlignedBB(getPos().add(-4, -4, -4), getPos().add(4, 4, 4))
            val boundPos = BlockPos.getAllInBox(BlockPos(bounds.minX, bounds.minY, bounds.minZ),
                BlockPos(bounds.maxX, bounds.maxY, bounds.maxZ))

            if (TickDog.watchdog is KhronusWorld) {
                val tickAcceleration = KhronusApi.getTickAcceleration(world)
                val bonus = warpDriveBonusTicks * ticks //Do not support tick acceleration

                boundPos.forEach { pos ->
                    val tile = world.getTileEntity(pos) ?: return@forEach

                    if (tile.isInvalid || blacklist.contains(getKey(tile.javaClass).toString())) return@forEach

                    if (tile is ITickable) tickAcceleration.compute(tile) { _, ticks -> ticks?.plus(bonus) ?: bonus }
                }
            } else {
                val tickable: MutableList<ITickable> = ArrayList()
                val khronusTickable: MutableList<IKhronusTickable<*>> = ArrayList()

                boundPos.forEach { pos ->
                    val tile = world.getTileEntity(pos) ?: return@forEach

                    if (tile.isInvalid || blacklist.contains(getKey(tile.javaClass).toString())) return@forEach

                    if (tile is IKhronusTickable<*>) khronusTickable.add(tile)
                    else if (tile is ITickable) tickable.add(tile)
                }

                for (i in 0 until warpDriveBonusTicks) {
                    tickable.forEach(ITickable::update)
                }

                val bonus = warpDriveBonusTicks * ticks //Do not support tick acceleration

                khronusTickable.forEach { tickable -> tickable.update(1, bonus) }
            }
        }
    }

    override fun getSource(): TileEntityWarpDrive = this
}