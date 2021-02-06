package dev.brella.khronus.examples.entity

import dev.brella.khronus.api.IKhronusTickable
import dev.brella.khronus.examples.block.KhronusBlocks
import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.TileEntity
import java.util.*

class CounterTileEntity : TileEntity(KhronusBlocks.counterTile.get()), IKhronusTickable {
    var startupTime: Long = System.currentTimeMillis()
    var counter: Long = 0

    override fun read(stateIn: BlockState, compound: CompoundNBT) {
        super.read(stateIn, compound)

        startupTime = compound.getLong("startup_time")
        counter = compound.getLong("counter")
    }

    override fun write(compound: CompoundNBT): CompoundNBT {
        val compound = super.write(compound)

        compound.putLong("startup_time", startupTime)
        compound.putLong("counter", counter)

        return compound
    }

    override fun tick(ticks: Int, bonusTicks: Int) {
        counter += ticks + bonusTicks

        markDirty()
    }
}