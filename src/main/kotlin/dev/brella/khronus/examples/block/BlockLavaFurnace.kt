package dev.brella.khronus.examples.block

import dev.brella.khronus.examples.entity.TileEntityLavaFurnace
import net.minecraft.block.BlockContainer
import net.minecraft.block.BlockFurnace
import net.minecraft.block.BlockHorizontal
import net.minecraft.block.material.Material
import net.minecraft.block.properties.IProperty
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
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.*

class BlockLavaFurnace constructor(isBurning: Boolean) : BlockContainer(Material.ROCK) {
    private val isBurning: Boolean
    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item {
        return Item.getItemFromBlock(KhronusBlocks.lavaFurnace)
    }

    override fun onBlockAdded(worldIn: World, pos: BlockPos, state: IBlockState) {
        setDefaultFacing(worldIn, pos, state)
    }

    private fun setDefaultFacing(worldIn: World, pos: BlockPos, state: IBlockState) {
        if (!worldIn.isRemote) {
            val iblockstate = worldIn.getBlockState(pos.north())
            val iblockstate1 = worldIn.getBlockState(pos.south())
            val iblockstate2 = worldIn.getBlockState(pos.west())
            val iblockstate3 = worldIn.getBlockState(pos.east())
            var enumfacing = state.getValue<EnumFacing>(FACING)
            if (enumfacing == EnumFacing.NORTH && iblockstate.isFullBlock && !iblockstate1.isFullBlock) {
                enumfacing = EnumFacing.SOUTH
            } else if (enumfacing == EnumFacing.SOUTH && iblockstate1.isFullBlock && !iblockstate.isFullBlock) {
                enumfacing = EnumFacing.NORTH
            } else if (enumfacing == EnumFacing.WEST && iblockstate2.isFullBlock && !iblockstate3.isFullBlock) {
                enumfacing = EnumFacing.EAST
            } else if (enumfacing == EnumFacing.EAST && iblockstate3.isFullBlock && !iblockstate2.isFullBlock) {
                enumfacing = EnumFacing.WEST
            }
            worldIn.setBlockState(pos, state.withProperty(FACING, enumfacing), 2)
        }
    }

    @SideOnly(Side.CLIENT)
    override fun randomDisplayTick(stateIn: IBlockState, worldIn: World, pos: BlockPos, rand: Random) {
        if (isBurning) {
            val enumfacing = stateIn.getValue<EnumFacing>(FACING) as EnumFacing
            val d0 = pos.x.toDouble() + 0.5
            val d1 = pos.y.toDouble() + rand.nextDouble() * 6.0 / 16.0
            val d2 = pos.z.toDouble() + 0.5
            val d3 = 0.52
            val d4 = rand.nextDouble() * 0.6 - 0.3
            if (rand.nextDouble() < 0.1) {
                worldIn.playSound(
                    pos.x.toDouble() + 0.5,
                    pos.y.toDouble(),
                    pos.z.toDouble() + 0.5,
                    SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE,
                    SoundCategory.BLOCKS,
                    1.0f,
                    1.0f,
                    false
                )
            }
            when (enumfacing) {
                EnumFacing.WEST -> {
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 - 0.52, d1, d2 + d4, 0.0, 0.0, 0.0)
                    worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 - 0.52, d1, d2 + d4, 0.0, 0.0, 0.0)
                }
                EnumFacing.EAST -> {
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + 0.52, d1, d2 + d4, 0.0, 0.0, 0.0)
                    worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 + 0.52, d1, d2 + d4, 0.0, 0.0, 0.0)
                }
                EnumFacing.NORTH -> {
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4, d1, d2 - 0.52, 0.0, 0.0, 0.0)
                    worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 + d4, d1, d2 - 0.52, 0.0, 0.0, 0.0)
                }
                EnumFacing.SOUTH -> {
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4, d1, d2 + 0.52, 0.0, 0.0, 0.0)
                    worldIn.spawnParticle(EnumParticleTypes.FLAME, d0 + d4, d1, d2 + 0.52, 0.0, 0.0, 0.0)
                }
            }
        }
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
        return if (worldIn.isRemote) {
            true
        } else {
            val tileentity = worldIn.getTileEntity(pos)
            if (tileentity is TileEntityLavaFurnace) {
                playerIn.displayGUIChest(tileentity)
                playerIn.addStat(StatList.FURNACE_INTERACTION)
            }
            true
        }
    }

    override fun createNewTileEntity(worldIn: World, meta: Int): TileEntity {
        return TileEntityLavaFurnace()
    }

    override fun getStateForPlacement(
        worldIn: World,
        pos: BlockPos,
        facing: EnumFacing,
        hitX: Float,
        hitY: Float,
        hitZ: Float,
        meta: Int,
        placer: EntityLivingBase
    ): IBlockState {
        return this.defaultState.withProperty(FACING, placer.horizontalFacing.opposite)
    }

    override fun onBlockPlacedBy(
        worldIn: World,
        pos: BlockPos,
        state: IBlockState,
        placer: EntityLivingBase,
        stack: ItemStack
    ) {
        worldIn.setBlockState(
            pos,
            state.withProperty(FACING, placer.horizontalFacing.opposite),
            2
        )
        if (stack.hasDisplayName()) {
            val tileentity = worldIn.getTileEntity(pos)
            if (tileentity is TileEntityLavaFurnace) {
                tileentity.setCustomInventoryName(stack.displayName)
            }
        }
    }

    override fun breakBlock(worldIn: World, pos: BlockPos, state: IBlockState) {
        if (!keepInventory) {
            val tileentity = worldIn.getTileEntity(pos)
            if (tileentity is TileEntityLavaFurnace) {
                InventoryHelper.dropInventoryItems(worldIn, pos, tileentity)
                worldIn.updateComparatorOutputLevel(pos, this)
            }
        }
        super.breakBlock(worldIn, pos, state)
    }

    override fun hasComparatorInputOverride(state: IBlockState): Boolean {
        return true
    }

    override fun getComparatorInputOverride(blockState: IBlockState, worldIn: World, pos: BlockPos): Int {
        return Container.calcRedstone(worldIn.getTileEntity(pos))
    }

    override fun getItem(worldIn: World, pos: BlockPos, state: IBlockState): ItemStack {
        return ItemStack(KhronusBlocks.lavaFurnace)
    }

    override fun getRenderType(state: IBlockState): EnumBlockRenderType {
        return EnumBlockRenderType.MODEL
    }

    override fun getStateFromMeta(meta: Int): IBlockState {
        var enumfacing = EnumFacing.byIndex(meta)
        if (enumfacing.axis == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH
        }
        return this.defaultState.withProperty(FACING, enumfacing)
    }

    override fun getMetaFromState(state: IBlockState): Int {
        return (state.getValue<EnumFacing>(FACING) as EnumFacing).index
    }

    override fun withRotation(state: IBlockState, rot: Rotation): IBlockState {
        return state.withProperty(
            FACING,
            rot.rotate(state.getValue<EnumFacing>(FACING) as EnumFacing)
        )
    }

    override fun withMirror(state: IBlockState, mirrorIn: Mirror): IBlockState {
        return state.withRotation(mirrorIn.toRotation(state.getValue<EnumFacing>(FACING) as EnumFacing))
    }

    override fun createBlockState(): BlockStateContainer {
        return BlockStateContainer(this, FACING)
    }

    companion object {
        val FACING = BlockHorizontal.FACING
        private var keepInventory = false

        fun setState(active: Boolean, worldIn: World, pos: BlockPos) {
            val iblockstate = worldIn.getBlockState(pos)
            val tileentity = worldIn.getTileEntity(pos)
            keepInventory = true
            if (iblockstate.block is BlockLavaFurnace) {
                if (active) {
                    worldIn.setBlockState(
                        pos, KhronusBlocks.litLavaFurnace.defaultState.withProperty(
                            FACING, iblockstate.getValue<EnumFacing>(
                                FACING
                            )
                        ), 3
                    )
                    worldIn.setBlockState(
                        pos, KhronusBlocks.litLavaFurnace.defaultState.withProperty(
                            FACING, iblockstate.getValue<EnumFacing>(
                                FACING
                            )
                        ), 3
                    )
                } else {
                    worldIn.setBlockState(
                        pos, KhronusBlocks.lavaFurnace.defaultState.withProperty(
                            FACING, iblockstate.getValue<EnumFacing>(
                                FACING
                            )
                        ), 3
                    )
                    worldIn.setBlockState(
                        pos, KhronusBlocks.lavaFurnace.defaultState.withProperty(
                            FACING, iblockstate.getValue<EnumFacing>(
                                FACING
                            )
                        ), 3
                    )
                }
                keepInventory = false
                if (tileentity != null) {
                    tileentity.validate()
                    worldIn.setTileEntity(pos, tileentity)
                }
            }
        }
    }

    init {
        this.defaultState = createBlockState().baseState.withProperty(FACING, EnumFacing.NORTH)
        this.isBurning = isBurning
    }
}