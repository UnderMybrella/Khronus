package dev.brella.khronus.examples.entity

import dev.brella.khronus.api.IKhronusTickable
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import java.util.*

class TileEntityCounter : TileEntity(), IKhronusTickable<TileEntityCounter> {
    var startupTime: Long = System.currentTimeMillis()
    var counter: Long = 0

    override fun readFromNBT(compound: NBTTagCompound) {
        super.readFromNBT(compound)

        startupTime = compound.getLong("startup_time")
        counter = compound.getLong("counter")
    }

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        val compound = super.writeToNBT(compound)

        compound.setLong("startup_time", startupTime)
        compound.setLong("counter", counter)

        return compound
    }

    override fun update(ticks: Int, bonusTicks: Int) {
        counter += ticks + bonusTicks

        markDirty()
    }

    override fun getSource(): TileEntityCounter = this
}