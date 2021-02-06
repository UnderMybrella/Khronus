package dev.brella.khronus.overrides.vanilla.blocks

import dev.brella.khronus.overrides.vanilla.te.KhronusFurnaceTileEntity
import net.minecraft.block.FurnaceBlock
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.IBlockReader

class KhronusFurnaceBlock(builder: Properties): FurnaceBlock(builder) {
    override fun createNewTileEntity(worldIn: IBlockReader): TileEntity = KhronusFurnaceTileEntity()
}