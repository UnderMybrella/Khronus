package dev.brella.khronus.overrides.vanilla.te

import dev.brella.khronus.api.IKhronusTickable
import net.minecraft.block.BlockFurnace
import net.minecraft.tileentity.TileEntityFurnace
import net.minecraft.util.math.MathHelper
import kotlin.math.min

class TileEntityKhronusFurnace: TileEntityFurnace(), IKhronusTickable {
    override fun update() = update(1, 0)
    override fun update(ticks: Int, bonusTicks: Int) {
        val flag = this.isBurning
        var flag1 = false

        val burnTime = min(furnaceBurnTime, ticks + bonusTicks)

        if (this.isBurning) {
            furnaceBurnTime -= burnTime
        }

        if (!world.isRemote) {
            val itemstack = furnaceItemStacks[1]
            if (this.isBurning || !itemstack.isEmpty && !furnaceItemStacks[0].isEmpty) {
                if (!this.isBurning && canSmelt()) {
                    furnaceBurnTime = getItemBurnTime(itemstack)
                    currentItemBurnTime = furnaceBurnTime
                    if (this.isBurning) {
                        flag1 = true
                        if (!itemstack.isEmpty) {
                            val item = itemstack.item
                            itemstack.shrink(1)
                            if (itemstack.isEmpty) {
                                val item1 = item.getContainerItem(itemstack)
                                furnaceItemStacks[1] = item1
                            }
                        }
                    }
                }
                if (this.isBurning && canSmelt()) {
                    cookTime += burnTime
                    if (cookTime >= totalCookTime) {
                        cookTime -= totalCookTime
                        totalCookTime = getCookTime(furnaceItemStacks[0])
                        smeltItem()
                        flag1 = true
                    }
                } else {
                    cookTime = 0
                }
            } else if (!this.isBurning && cookTime > 0) {
                cookTime = MathHelper.clamp(cookTime - ((ticks + bonusTicks) * 2), 0, totalCookTime)
            }
            if (flag != this.isBurning) {
                flag1 = true
                BlockFurnace.setState(this.isBurning, world, pos)
            }
        }

        if (flag1) {
            markDirty()
        }
    }
}