package dev.brella.khronus.examples.entity

import dev.brella.khronus.api.IKhronusTickable
import dev.brella.khronus.examples.block.BlockLavaFurnace
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.*
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntityFurnace
import net.minecraft.tileentity.TileEntityLockable
import net.minecraft.util.EnumFacing
import net.minecraft.util.NonNullList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.wrapper.SidedInvWrapper
import kotlin.math.floor
import kotlin.math.log

class TileEntityLavaFurnace : TileEntityLockable(), IKhronusTickable, ISidedInventory {
    private var furnaceItemStacks = NonNullList.withSize(3, ItemStack.EMPTY)
    private var furnaceBurnTime = 0
    private var currentItemBurnTime = 0
    private var cookTime = 0
    private var totalCookTime = 0
    private var furnaceCustomName: String? = null
    override fun getSizeInventory(): Int {
        return furnaceItemStacks.size
    }

    override fun isEmpty(): Boolean {
        for (itemstack in furnaceItemStacks) {
            if (!itemstack.isEmpty) {
                return false
            }
        }
        return true
    }

    override fun getStackInSlot(index: Int): ItemStack {
        return furnaceItemStacks[index]
    }

    override fun decrStackSize(index: Int, count: Int): ItemStack {
        return ItemStackHelper.getAndSplit(furnaceItemStacks, index, count)
    }

    override fun removeStackFromSlot(index: Int): ItemStack {
        return ItemStackHelper.getAndRemove(furnaceItemStacks, index)
    }

    override fun setInventorySlotContents(index: Int, stack: ItemStack) {
        val itemstack = furnaceItemStacks[index]
        val flag = !stack.isEmpty && stack.isItemEqual(itemstack) && ItemStack.areItemStackTagsEqual(stack, itemstack)
        furnaceItemStacks[index] = stack
        if (stack.count > this.inventoryStackLimit) {
            stack.count = this.inventoryStackLimit
        }
        if (index == 0 && !flag) {
            totalCookTime = getCookTime(stack)
            cookTime = 0
            markDirty()
        }
    }

    override fun getName(): String {
        return if (hasCustomName()) furnaceCustomName!! else "container.furnace"
    }

    override fun hasCustomName(): Boolean {
        return furnaceCustomName != null && !furnaceCustomName!!.isEmpty()
    }

    fun setCustomInventoryName(p_145951_1_: String?) {
        furnaceCustomName = p_145951_1_
    }

    override fun readFromNBT(compound: NBTTagCompound) {
        super.readFromNBT(compound)
        furnaceItemStacks = NonNullList.withSize(this.sizeInventory, ItemStack.EMPTY)
        ItemStackHelper.loadAllItems(compound, furnaceItemStacks)
        furnaceBurnTime = compound.getInteger("BurnTime")
        cookTime = compound.getInteger("CookTime")
        totalCookTime = compound.getInteger("CookTimeTotal")
        currentItemBurnTime = TileEntityFurnace.getItemBurnTime(furnaceItemStacks[1])
        if (compound.hasKey("CustomName", 8)) {
            furnaceCustomName = compound.getString("CustomName")
        }
    }

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        super.writeToNBT(compound)
        compound.setInteger("BurnTime", furnaceBurnTime)
        compound.setInteger("CookTime", cookTime)
        compound.setInteger("CookTimeTotal", totalCookTime)
        ItemStackHelper.saveAllItems(compound, furnaceItemStacks)
        if (hasCustomName()) {
            compound.setString("CustomName", furnaceCustomName)
        }
        return compound
    }

    override fun getInventoryStackLimit(): Int {
        return 64
    }

    val isBurning: Boolean
        get() = furnaceBurnTime > 0

    override fun update(ticks: Int, bonusTicks: Int) {
        val pos = BlockPos.MutableBlockPos(getPos().x - 8, getPos().y, getPos().z - 8)
        val lavaBlocks: MutableSet<BlockPos> = HashSet()

        val baseX = pos.x
        val baseY = pos.y
        val baseZ = pos.z

        for (x in 0 until 16) {
            for (z in 0 until 16) {
                pos.setPos(baseX + x, baseY, baseZ + z)
                if (world.getBlockState(pos).block == Blocks.LAVA) {
                    lavaBlocks.add(pos.toImmutable())
                }
            }
        }

//        if (!world.isRemote) Thread.sleep(5) //simulate a slow task

        val flag = isBurning
        var flag1 = false
        val cookIncr = minOf(furnaceBurnTime, floor((ticks + bonusTicks) * log(lavaBlocks.size.toDouble() + 4.0, 4.0)).toInt())

        if (isBurning) {
            furnaceBurnTime -= cookIncr
        }
        if (!world.isRemote) {
            val itemstack = furnaceItemStacks[1]
            if (isBurning || !itemstack.isEmpty && !furnaceItemStacks[0].isEmpty) {
                if (!isBurning && canSmelt()) {
                    furnaceBurnTime = TileEntityFurnace.getItemBurnTime(itemstack)
                    currentItemBurnTime = furnaceBurnTime
                    if (isBurning) {
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
                if (isBurning && canSmelt()) {
                    cookTime += cookIncr
                    if (cookTime >= totalCookTime) {
                        cookTime -= totalCookTime
                        totalCookTime = getCookTime(furnaceItemStacks[0])
                        smeltItem()
                        flag1 = true

                        if (lavaBlocks.isNotEmpty() && world.rand.nextBoolean()) {
                            val harden = lavaBlocks.random()
                            world.setBlockState(harden, Blocks.STONE.defaultState)
                        }
                    }
                } else {
                    cookTime = 0
                }
            } else if (!isBurning && cookTime > 0) {
                cookTime = MathHelper.clamp(cookTime - ((ticks + bonusTicks) * 2), 0, totalCookTime)
            }
            if (flag != isBurning) {
                flag1 = true
                BlockLavaFurnace.setState(isBurning, world, pos)
            }
        }
        if (flag1) {
            markDirty()
        }
    }

    fun getCookTime(stack: ItemStack?): Int {
        return 200
    }

    private fun canSmelt(): Boolean {
        return if (furnaceItemStacks[0].isEmpty) {
            false
        } else {
            val itemstack = FurnaceRecipes.instance().getSmeltingResult(furnaceItemStacks[0])
            if (itemstack.isEmpty) {
                false
            } else {
                val itemstack1 = furnaceItemStacks[2]
                if (itemstack1.isEmpty) {
                    true
                } else if (!itemstack1.isItemEqual(itemstack)) {
                    false
                } else if (itemstack1.count + itemstack.count <= this.inventoryStackLimit && itemstack1.count + itemstack.count <= itemstack1.maxStackSize) // Forge fix: make furnace respect stack sizes in furnace recipes
                {
                    true
                } else {
                    itemstack1.count + itemstack.count <= itemstack.maxStackSize // Forge fix: make furnace respect stack sizes in furnace recipes
                }
            }
        }
    }

    fun smeltItem() {
        if (canSmelt()) {
            val itemstack = furnaceItemStacks[0]
            val itemstack1 = FurnaceRecipes.instance().getSmeltingResult(itemstack)
            val itemstack2 = furnaceItemStacks[2]
            if (itemstack2.isEmpty) {
                furnaceItemStacks[2] = itemstack1.copy()
            } else if (itemstack2.item === itemstack1.item) {
                itemstack2.grow(itemstack1.count)
            }
            if (itemstack.item === Item.getItemFromBlock(Blocks.SPONGE) && itemstack.metadata == 1 && !furnaceItemStacks[1].isEmpty && furnaceItemStacks[1].item === Items.BUCKET) {
                furnaceItemStacks[1] = ItemStack(Items.WATER_BUCKET)
            }
            itemstack.shrink(1)
        }
    }

    override fun isUsableByPlayer(player: EntityPlayer): Boolean {
        return if (world.getTileEntity(pos) !== this) {
            false
        } else {
            player.getDistanceSq(
                pos.x.toDouble() + 0.5, pos.y.toDouble() + 0.5, pos.z
                    .toDouble() + 0.5
            ) <= 64.0
        }
    }

    override fun openInventory(player: EntityPlayer) {}
    override fun closeInventory(player: EntityPlayer) {}
    override fun isItemValidForSlot(index: Int, stack: ItemStack): Boolean {
        return if (index == 2) {
            false
        } else if (index != 1) {
            true
        } else {
            val itemstack = furnaceItemStacks[1]
            TileEntityFurnace.isItemFuel(stack) || SlotFurnaceFuel.isBucket(stack) && itemstack.item !== Items.BUCKET
        }
    }

    override fun getSlotsForFace(side: EnumFacing): IntArray {
        return if (side == EnumFacing.DOWN) {
            SLOTS_BOTTOM
        } else {
            if (side == EnumFacing.UP) SLOTS_TOP else SLOTS_SIDES
        }
    }

    override fun canInsertItem(index: Int, itemStackIn: ItemStack, direction: EnumFacing): Boolean {
        return isItemValidForSlot(index, itemStackIn)
    }

    override fun canExtractItem(index: Int, stack: ItemStack, direction: EnumFacing): Boolean {
        if (direction == EnumFacing.DOWN && index == 1) {
            val item = stack.item
            if (item !== Items.WATER_BUCKET && item !== Items.BUCKET) {
                return false
            }
        }
        return true
    }

    override fun getGuiID(): String {
        return "minecraft:furnace"
    }

    override fun createContainer(playerInventory: InventoryPlayer, playerIn: EntityPlayer): Container {
        return ContainerFurnace(playerInventory, this)
    }

    override fun getField(id: Int): Int {
        return when (id) {
            0 -> furnaceBurnTime
            1 -> currentItemBurnTime
            2 -> cookTime
            3 -> totalCookTime
            else -> 0
        }
    }

    override fun setField(id: Int, value: Int) {
        when (id) {
            0 -> furnaceBurnTime = value
            1 -> currentItemBurnTime = value
            2 -> cookTime = value
            3 -> totalCookTime = value
        }
    }

    override fun getFieldCount(): Int {
        return 4
    }

    override fun clear() {
        furnaceItemStacks.clear()
    }

    override fun shouldRefresh(world: World, pos: BlockPos, oldState: IBlockState, newState: IBlockState): Boolean =
        newState.block !is BlockLavaFurnace

    var handlerTop: IItemHandler = SidedInvWrapper(this, EnumFacing.UP)
    var handlerBottom: IItemHandler = SidedInvWrapper(this, EnumFacing.DOWN)
    var handlerSide: IItemHandler = SidedInvWrapper(this, EnumFacing.WEST)

    override fun <T> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
        return if (facing != null && capability === CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) if (facing == EnumFacing.DOWN) handlerBottom as T else if (facing == EnumFacing.UP) handlerTop as T else handlerSide as T else super.getCapability(
            capability,
            facing
        )
    }

    companion object {
        private val SLOTS_TOP = intArrayOf(0)
        private val SLOTS_BOTTOM = intArrayOf(2, 1)
        private val SLOTS_SIDES = intArrayOf(1)

        @SideOnly(Side.CLIENT)
        fun isBurning(inventory: IInventory): Boolean {
            return inventory.getField(0) > 0
        }
    }
}