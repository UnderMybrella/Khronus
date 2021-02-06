package dev.brella.khronus.examples.entity

import dev.brella.khronus.api.IKhronusTickable
import dev.brella.khronus.examples.block.KhronusBlocks
import net.minecraft.block.AbstractFurnaceBlock
import net.minecraft.block.Blocks
import net.minecraft.block.FlowingFluidBlock
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.fluid.Fluids
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.FurnaceContainer
import net.minecraft.item.crafting.AbstractCookingRecipe
import net.minecraft.item.crafting.IRecipe
import net.minecraft.item.crafting.IRecipeType
import net.minecraft.tileentity.AbstractFurnaceTileEntity
import net.minecraft.tileentity.FurnaceTileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import kotlin.math.floor
import kotlin.math.log
import kotlin.math.log2
import kotlin.math.min

class LavaFurnaceTileEntity : AbstractFurnaceTileEntity(KhronusBlocks.lavaFurnaceTile(), IRecipeType.SMELTING), IKhronusTickable {
    override fun getDefaultName(): ITextComponent {
        return TranslationTextComponent("container.furnace")
    }

    override fun createMenu(p_213906_1_: Int, p_213906_2_: PlayerInventory): Container {
        return FurnaceContainer(p_213906_1_, p_213906_2_, this, furnaceData)
    }

    override fun tick() = super<IKhronusTickable>.tick()
    override fun tick(ticks: Int, bonusTicks: Int) {
        val pos = BlockPos.Mutable(getPos().x - 8, getPos().y, getPos().z - 8)
        val lavaBlocks: MutableSet<BlockPos> = HashSet()

        val baseX = pos.x
        val baseY = pos.y
        val baseZ = pos.z

        val world = world ?: return

        for (x in 0 until 16) {
            for (z in 0 until 16) {
                val state = world.getBlockState(pos.setPos(baseX + x, baseY, baseZ + z))
                if (state.block is FlowingFluidBlock && state.fluidState.fluid == Fluids.LAVA && state.fluidState.isSource) {
                    lavaBlocks.add(pos.toImmutable())
                }
            }
        }

//        if (!world.isRemote) Thread.sleep(5) //simulate a slow task

        val flag = isBurning
        var flag1 = false

        val ticksToCookFor =
            if (isBurning)
                min(floor((ticks + bonusTicks) * log(lavaBlocks.size.toDouble() + 4.0, 4.0)).toInt(), burnTime)
            else 0

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

                        if (lavaBlocks.isNotEmpty() && world.rand.nextBoolean()) {
                            world.setBlockState(lavaBlocks.random(), Blocks.BASALT.defaultState)
                        }
                    }
                } else {
                    cookTime = 0
                }
            } else if (!isBurning && cookTime > 0) {
                cookTime = MathHelper.clamp(cookTime - (ticksToCookFor shl 1), 0, cookTimeTotal)
            }
            if (flag != isBurning) {
                flag1 = true
                world.setBlockState(this.pos,
                    world.getBlockState(this.pos).with(AbstractFurnaceBlock.LIT, isBurning),
                    3)
            }
        }

        if (flag1) {
            markDirty()
        }
    }
}