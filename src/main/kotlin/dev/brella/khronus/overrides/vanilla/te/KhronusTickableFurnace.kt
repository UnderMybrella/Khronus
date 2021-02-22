package dev.brella.khronus.overrides.vanilla.te

import dev.brella.khronus.api.IKhronusTickable
import net.minecraft.block.BlockFurnace
import net.minecraft.tileentity.TileEntityFurnace
import net.minecraft.util.math.MathHelper
import kotlin.math.min

class KhronusTickableFurnace(val base: TileEntityFurnace): IKhronusTickable<TileEntityFurnace> {
    override fun update() = update(1, 0)
    override fun update(ticks: Int, bonusTicks: Int) {
        val flag = base.isBurning
        var flag1 = false

        val burnTime = min(base.furnaceBurnTime, ticks + bonusTicks)

        if (base.isBurning) {
            base.furnaceBurnTime -= burnTime
        }

        if (!base.world.isRemote) {
            val itemstack = base.furnaceItemStacks[1]
            if (base.isBurning || !itemstack.isEmpty && !base.furnaceItemStacks[0].isEmpty) {
                if (!base.isBurning && base.canSmelt()) {
                    base.furnaceBurnTime = TileEntityFurnace.getItemBurnTime(itemstack)
                    base.currentItemBurnTime = base.furnaceBurnTime
                    if (base.isBurning) {
                        flag1 = true
                        if (!itemstack.isEmpty) {
                            val item = itemstack.item
                            itemstack.shrink(1)
                            if (itemstack.isEmpty) {
                                val item1 = item.getContainerItem(itemstack)
                                base.furnaceItemStacks[1] = item1
                            }
                        }
                    }
                }
                if (base.isBurning && base.canSmelt()) {
                    base.cookTime += burnTime
                    if (base.cookTime >= base.totalCookTime) {
                        base.cookTime -= base.totalCookTime
                        base.totalCookTime = base.getCookTime(base.furnaceItemStacks[0])
                        base.smeltItem()
                        flag1 = true
                    }
                } else {
                    base.cookTime = 0
                }
            } else if (!base.isBurning && base.cookTime > 0) {
                base.cookTime = MathHelper.clamp(base.cookTime - ((ticks + bonusTicks) * 2), 0, base.totalCookTime)
            }
            if (flag != base.isBurning) {
                flag1 = true
                BlockFurnace.setState(base.isBurning, base.world, base.pos)
            }
        }

        if (flag1) {
            base.markDirty()
        }
    }

    override fun getSource(): TileEntityFurnace = base
}