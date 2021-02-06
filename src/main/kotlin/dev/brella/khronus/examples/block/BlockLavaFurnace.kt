package dev.brella.khronus.examples.block

import dev.brella.khronus.examples.entity.LavaFurnaceTileEntity
import net.minecraft.block.AbstractFurnaceBlock
import net.minecraft.block.BlockState
import net.minecraft.block.FurnaceBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.particles.ParticleTypes
import net.minecraft.stats.Stats
import net.minecraft.tileentity.FurnaceTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Direction
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import java.util.*

class BlockLavaFurnace (builder: Properties): AbstractFurnaceBlock(builder) {
    override fun interactWith(p_220089_1_: World, p_220089_2_: BlockPos?, p_220089_3_: PlayerEntity) {
        val lvt_4_1_ = p_220089_1_.getTileEntity(p_220089_2_)
        if (lvt_4_1_ is LavaFurnaceTileEntity) {
            p_220089_3_.openContainer(lvt_4_1_ as INamedContainerProvider?)
            p_220089_3_.addStat(Stats.INTERACT_WITH_FURNACE)
        }
    }

    override fun animateTick(stateIn: BlockState, worldIn: World, pos: BlockPos, rand: Random) {
        if (stateIn.get(LIT)) {
            val d0 = pos.x.toDouble() + 0.5
            val d1 = pos.y.toDouble()
            val d2 = pos.z.toDouble() + 0.5
            if (rand.nextDouble() < 0.1) {
                worldIn.playSound(d0,
                    d1,
                    d2,
                    SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE,
                    SoundCategory.BLOCKS,
                    1.0f,
                    1.0f,
                    false)
            }
            val direction = stateIn.get(FACING)
            val `direction$axis` = direction.axis
            val d3 = 0.52
            val d4 = rand.nextDouble() * 0.6 - 0.3
            val d5 = if (`direction$axis` === Direction.Axis.X) direction.xOffset.toDouble() * 0.52 else d4
            val d6 = rand.nextDouble() * 6.0 / 16.0
            val d7 = if (`direction$axis` === Direction.Axis.Z) direction.zOffset.toDouble() * 0.52 else d4

            worldIn.addParticle(ParticleTypes.SMOKE, d0 + d5, d1 + d6, d2 + d7, 0.0, 0.0, 0.0)
            worldIn.addParticle(ParticleTypes.LAVA, d0 + d5, d1 + d6, d2 + d7, 0.0, 0.0, 0.0)
        }
    }

    override fun createNewTileEntity(worldIn: IBlockReader): TileEntity = LavaFurnaceTileEntity()
}