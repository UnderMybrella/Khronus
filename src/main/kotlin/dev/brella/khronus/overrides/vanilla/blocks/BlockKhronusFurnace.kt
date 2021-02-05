package dev.brella.khronus.overrides.vanilla.blocks

import dev.brella.khronus.overrides.vanilla.te.TileEntityKhronusFurnace
import net.minecraft.block.BlockFurnace
import net.minecraft.block.state.IBlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

class BlockKhronusFurnace(isLit: Boolean): BlockFurnace(isLit) {
    override fun createNewTileEntity(worldIn: World, meta: Int): TileEntity = TileEntityKhronusFurnace()
}