package dev.brella.khronus.examples.block

import dev.brella.khronus.examples.entity.TileEntityCounter
import dev.brella.khronus.examples.entity.TileEntityLavaFurnace
import dev.brella.khronus.examples.entity.TileEntityWarpDrive
import net.minecraft.block.BlockContainer
import net.minecraft.block.BlockFurnace
import net.minecraft.block.BlockHorizontal
import net.minecraft.block.material.Material
import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.SoundEvents
import net.minecraft.inventory.Container
import net.minecraft.inventory.InventoryHelper
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.stats.StatList
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentString
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

class BlockCounter : BlockContainer(Material.CIRCUITS) {
    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item {
        return Item.getItemFromBlock(KhronusBlocks.counter)
    }

    override fun hasComparatorInputOverride(state: IBlockState): Boolean {
        return true
    }

    override fun getComparatorInputOverride(blockState: IBlockState, worldIn: World, pos: BlockPos): Int {
        val counter = worldIn.getTileEntity(pos) as? TileEntityCounter ?: return 0
        val ticksPassed = ((System.currentTimeMillis() - counter.startupTime) / 50)
        val perNotch = ticksPassed / 16

        return (counter.counter / perNotch).toInt()
    }

    @ExperimentalTime
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
        if (worldIn.isRemote) {
            val te = worldIn.getTileEntity(pos) as? TileEntityCounter ?: return true

            playerIn.sendMessage(TextComponentString("This block was placed down around ${
                Instant.ofEpochMilli(te.startupTime).atZone(ZoneId.systemDefault()).format(
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
            }, and it's currently reporting that it's around ${
                Instant.ofEpochMilli(te.startupTime + (te.counter * 50)).atZone(ZoneId.systemDefault()).format(
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
            }"))

            playerIn.sendMessage(TextComponentString("Alternatively, it's been clicked for approximately ${(te.counter * 50).milliseconds}"))
        }

        return true
    }

    override fun createNewTileEntity(worldIn: World, meta: Int): TileEntity {
        return TileEntityCounter()
    }

    override fun getRenderType(state: IBlockState): EnumBlockRenderType {
        return EnumBlockRenderType.MODEL
    }

    override fun getRenderLayer(): BlockRenderLayer =
        BlockRenderLayer.SOLID

    init {
        this.defaultState = createBlockState().baseState
    }
}