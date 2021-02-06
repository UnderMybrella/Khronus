package dev.brella.khronus.examples.entity

import dev.brella.khronus.TickDog
import dev.brella.khronus.api.IKhronusTickable
import dev.brella.khronus.api.KhronusApi
import dev.brella.khronus.examples.block.KhronusBlocks
import dev.brella.khronus.watchdogs.KhronusWorld
import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import java.util.*
import kotlin.collections.ArrayList

class WarpDriveTileEntity : TileEntity(KhronusBlocks.warpDriveTile()), IKhronusTickable {
    var warpDriveBonusTicks: Int = 18
    val blacklist = listOf("khronus:warp_drive", "projecte:dm_pedestal")

    var active = false
    var previousRedstoneState = false

    override fun read(stateIn: BlockState, compound: CompoundNBT) {
        super.read(stateIn, compound)

        active = compound.getBoolean("active")
        previousRedstoneState = compound.getBoolean("previous_redstone_state")
    }

    override fun write(compound: CompoundNBT): CompoundNBT {
        return super.write(compound).apply {
            putBoolean("active", active)
            putBoolean("previous_redstone_state", previousRedstoneState)
        }
    }

    override fun tick(ticks: Int, bonusTicks: Int) {
        val world = this.world ?: return

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

                    if (tile.isRemoved || blacklist.contains(tile.type.registryName.toString())) return@forEach

                    if (tile is ITickableTileEntity) tickAcceleration.compute(tile) { _, ticks -> ticks?.plus(bonus) ?: bonus }
                }
            } else {
                val tickable: MutableList<ITickableTileEntity> = ArrayList()
                val khronusTickable: MutableList<IKhronusTickable> = ArrayList()

                boundPos.forEach { pos ->
                    val tile = world.getTileEntity(pos) ?: return@forEach

                    if (tile.isRemoved || blacklist.contains(tile.type.registryName.toString())) return@forEach

                    if (tile is IKhronusTickable) khronusTickable.add(tile)
                    else if (tile is ITickableTileEntity) tickable.add(tile)
                }

                for (i in 0 until warpDriveBonusTicks) {
                    tickable.forEach(ITickableTileEntity::tick)
                }

                val bonus = warpDriveBonusTicks * ticks //Do not support tick acceleration

                khronusTickable.forEach { it.tick(1, bonus) }
            }
        }
    }
}