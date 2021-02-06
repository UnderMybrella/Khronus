package dev.brella.khronus.examples.block

import dev.brella.khronus.examples.entity.WarpDriveTileEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.ContainerBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.world.IBlockReader
import net.minecraft.world.World

class WarpDriveBlock(props: Properties) : ContainerBlock(props) {
    override fun onBlockActivated(
        state: BlockState,
        worldIn: World,
        pos: BlockPos,
        playerIn: PlayerEntity,
        hand: Hand,
        raytrace: BlockRayTraceResult
    ): ActionResultType {
        val te = worldIn.getTileEntity(pos) as? WarpDriveTileEntity ?: return ActionResultType.PASS
        te.active = !te.active
        te.markDirty()

        return ActionResultType.CONSUME
    }

    override fun createNewTileEntity(worldIn: IBlockReader): TileEntity = WarpDriveTileEntity()

    override fun getRenderType(state: BlockState): BlockRenderType = BlockRenderType.MODEL

    override fun neighborChanged(
        state: BlockState,
        world: World,
        pos: BlockPos,
        blockIn: Block,
        fromPos: BlockPos,
        isMoving: Boolean
    ) {
        val flag = world.isBlockPowered(pos)
        val te = world.getTileEntity(pos) as? WarpDriveTileEntity ?: return
        if (te.previousRedstoneState != flag) {
            te.previousRedstoneState = flag

            if (flag) te.active = !te.active
            te.markDirty()
        }
    }
}