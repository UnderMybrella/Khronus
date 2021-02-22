package dev.brella.khronus.overrides.projecte

import moze_intel.projecte.api.item.IPedestalItem
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface IKhronusPedestalItem: IPedestalItem {
    override fun updateInPedestal(p0: World, p1: BlockPos) = updateInPedestal(p0, p1, 1, 0)
    fun updateInPedestal(world: World, pos: BlockPos, ticks: Int, bonusTicks: Int)
}