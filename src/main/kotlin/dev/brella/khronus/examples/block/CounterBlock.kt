package dev.brella.khronus.examples.block

import dev.brella.khronus.examples.entity.CounterTileEntity
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.ContainerBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.text.StringTextComponent
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

class CounterBlock(builder: Properties) : ContainerBlock(builder) {
    override fun hasComparatorInputOverride(state: BlockState): Boolean {
        return true
    }

    override fun getComparatorInputOverride(blockState: BlockState, worldIn: World, pos: BlockPos): Int {
        val counter = worldIn.getTileEntity(pos) as? CounterTileEntity ?: return 0
        val ticksPassed = ((System.currentTimeMillis() - counter.startupTime) / 50)
        val perNotch = ticksPassed / 16

        return (counter.counter / perNotch).toInt()
    }

    @ExperimentalTime
    override fun onBlockActivated(
        state: BlockState,
        worldIn: World,
        pos: BlockPos,
        playerIn: PlayerEntity,
        hand: Hand,
        raytrace: BlockRayTraceResult,
    ): ActionResultType {
        if (worldIn.isRemote) {
            val te = worldIn.getTileEntity(pos) as? CounterTileEntity ?: return ActionResultType.PASS

            playerIn.sendMessage(StringTextComponent("This block was placed down around ${
                Instant.ofEpochMilli(te.startupTime).atZone(ZoneId.systemDefault()).format(
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
            }, and it's currently reporting that it's around ${
                Instant.ofEpochMilli(te.startupTime + (te.counter * 50)).atZone(ZoneId.systemDefault()).format(
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
            }"), Util.DUMMY_UUID)

            playerIn.sendMessage(StringTextComponent("Alternatively, it's been clicked for approximately ${(te.counter * 50).milliseconds}"), Util.DUMMY_UUID)
        }

        return ActionResultType.PASS
    }

    override fun createNewTileEntity(worldIn: IBlockReader): TileEntity =
        CounterTileEntity()

    override fun getRenderType(state: BlockState?): BlockRenderType? {
        return BlockRenderType.MODEL
    }
}