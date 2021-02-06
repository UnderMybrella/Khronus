package dev.brella.khronus.overrides.vanilla.te

import dev.brella.khronus.api.IKhronusTickable
import net.minecraft.block.AbstractFurnaceBlock
import net.minecraft.item.crafting.AbstractCookingRecipe
import net.minecraft.item.crafting.IRecipe
import net.minecraft.item.crafting.IRecipeType
import net.minecraft.tileentity.FurnaceTileEntity
import net.minecraft.util.math.MathHelper
import kotlin.math.min

class KhronusFurnaceTileEntity: FurnaceTileEntity(), IKhronusTickable {
    override fun tick() = tick(1, 0)
    override fun tick(ticks: Int, bonusTicks: Int) {
        val world = world ?: return

        val flag = isBurning
        var flag1 = false

        val ticksToCookFor = if (isBurning) min(ticks + bonusTicks, burnTime) else 0

        if (isBurning) {
            burnTime -= ticksToCookFor
        }

        if (!world.isRemote) {
            val itemstack = items[1]
            if (isBurning || !itemstack.isEmpty && !items[0].isEmpty) {
                val irecipe: IRecipe<*>? =
                    world.recipeManager.getRecipe(recipeType as IRecipeType<AbstractCookingRecipe>, this, world)
                        .orElse(null)
                if (!isBurning && canSmelt(irecipe)) {
                    burnTime = getBurnTime(itemstack)
                    recipesUsed = burnTime
                    if (isBurning) {
                        flag1 = true
                        if (itemstack.hasContainerItem()) items[1] =
                            itemstack.containerItem else if (!itemstack.isEmpty) {
//                            val item = itemstack.item
                            itemstack.shrink(1)
                            if (itemstack.isEmpty) {
                                items[1] = itemstack.containerItem
                            }
                        }
                    }
                }
                if (isBurning && canSmelt(irecipe)) {
                    cookTime += ticksToCookFor
                    if (cookTime >= cookTimeTotal) {
                        cookTime -= cookTimeTotal
                        cookTimeTotal = getCookTime()
                        smelt(irecipe)
                        flag1 = true
                    }
                } else {
                    cookTime = 0
                }
            } else if (!isBurning && cookTime > 0) {
                cookTime = MathHelper.clamp(cookTime - (ticksToCookFor shl 1), 0, cookTimeTotal)
            }
            if (flag != isBurning) {
                flag1 = true
                world.setBlockState(this.pos, world.getBlockState(this.pos).with(AbstractFurnaceBlock.LIT, isBurning), 3)
            }
        }

        if (flag1) {
            markDirty()
        }
    }
}