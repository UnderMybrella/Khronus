package dev.brella.khronus.examples.block

import dev.brella.khronus.examples.entity.TileEntityWarpDrive
import net.minecraft.block.Block
import net.minecraft.block.BlockContainer
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.*
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*

class BlockWarpDrive : BlockContainer(Material.PORTAL) {
    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item {
        return Item.getItemFromBlock(KhronusBlocks.warpDrive)
    }

    override fun onBlockActivated(
        worldIn: World,
        pos: BlockPos,
        state: IBlockState,
        playerIn: EntityPlayer,
        hand: EnumHand,
        facing: EnumFacing,
        hitX: Float,
        hitY: Float,
        hitZ: Float
    ): Boolean {
        val te = worldIn.getTileEntity(pos) as? TileEntityWarpDrive ?: return true
        te.active = !te.active
        te.markDirty()

        return true
    }

    override fun createNewTileEntity(worldIn: World, meta: Int): TileEntity {
        return TileEntityWarpDrive()
    }

    override fun getItem(worldIn: World, pos: BlockPos, state: IBlockState): ItemStack {
        return ItemStack(KhronusBlocks.warpDrive)
    }

    override fun getRenderType(state: IBlockState): EnumBlockRenderType {
        return EnumBlockRenderType.MODEL
    }

    override fun neighborChanged(
        state: IBlockState,
        worldIn: World,
        pos: BlockPos,
        blockIn: Block,
        fromPos: BlockPos?
    ) {
        val flag = worldIn.isBlockPowered(pos)
        val te = worldIn.getTileEntity(pos) as? TileEntityWarpDrive ?: return
        if (te.previousRedstoneState != flag) {
            te.previousRedstoneState = flag

            if (flag) te.active = !te.active
            te.markDirty()
        }
    }

    init {
        this.defaultState = createBlockState().baseState
    }
}