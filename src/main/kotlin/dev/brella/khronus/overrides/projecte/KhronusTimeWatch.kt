package dev.brella.khronus.overrides.projecte

import com.google.common.collect.Sets
import dev.brella.khronus.TickDog
import dev.brella.khronus.api.IKhronusTickable
import dev.brella.khronus.api.KhronusApi
import dev.brella.khronus.asKhronusTickable
import dev.brella.khronus.watchdogs.KhronusWorld
import dev.brella.khronus.watchdogs.tickAcceleration
import moze_intel.projecte.config.ProjectEConfig
import moze_intel.projecte.gameObjs.tiles.DMPedestalTile
import moze_intel.projecte.utils.WorldHelper
import net.minecraft.block.BlockLiquid
import net.minecraft.block.IGrowable
import net.minecraft.client.resources.I18n
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ITickable
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import net.minecraftforge.common.IPlantable
import net.minecraftforge.fluids.BlockFluidBase
import java.util.*

class KhronusTimeWatch : IKhronusPedestalItem {
    companion object {
        private val internalBlacklist: Set<String> =
            Sets.newHashSet(
                "Reika.ChromatiCraft.TileEntity.AOE.TileEntityAccelerator",
                "com.sci.torcherino.tile.TileTorcherino",
                "com.sci.torcherino.tile.TileCompressedTorcherino",
                "thaumcraft.common.tiles.crafting.TileSmelter"
            )
    }

    override fun updateInPedestal(world: World, pos: BlockPos, ticks: Int, bonusTicks: Int) {
        if (!world.isRemote && ProjectEConfig.items.enableTimeWatch) {
            val te = world.getTileEntity(pos)
            if (te is DMPedestalTile) {
                val bBox = te.effectBounds
                if (ProjectEConfig.effects.timePedBonus > 0) {
                    this.speedUpTileEntities(world, ProjectEConfig.effects.timePedBonus, bBox, ticks, bonusTicks)
                    this.speedUpRandomTicks(world, ProjectEConfig.effects.timePedBonus * ticks, bBox)
                }
                if (ProjectEConfig.effects.timePedMobSlowness < 1.0f) {
                    this.slowMobs(world, bBox, ProjectEConfig.effects.timePedMobSlowness * ticks)
                }
            }
        }
    }

    private fun speedUpTileEntities(world: World, bonusTicks: Int, box: AxisAlignedBB?, khronusTicks: Int, khronusBonusTicks: Int) {
        if (box != null && bonusTicks != 0) {
            val blacklist = ProjectEConfig.effects.timeWatchTEBlacklist.toList()
//            val tickable: MutableList<ITickable> = ArrayList()
//            val khronusTickable: MutableList<IKhronusTickable<*>> = ArrayList()

            if (TickDog.watchdog is KhronusWorld) {
                val acceleration = world.tickAcceleration
                val ticks = bonusTicks * khronusTicks + khronusBonusTicks

                BlockPos.getAllInBox(BlockPos(box.minX, box.minY, box.minZ), BlockPos(box.maxX, box.maxY, box.maxZ))
                    .forEach { pos ->
                        val tile = world.getTileEntity(pos)
                        if (tile != null
                            && !tile.isInvalid
                            && !internalBlacklist.contains(tile.javaClass.toString())
                            && !blacklist.contains(TileEntity.getKey(tile.javaClass).toString())
                        ) {
                            acceleration.compute(tile) { _, t -> t?.plus(ticks) ?: ticks }
                        }
                    }
            } else {
                val kTicks = bonusTicks * khronusTicks
                BlockPos.getAllInBox(BlockPos(box.minX, box.minY, box.minZ), BlockPos(box.maxX, box.maxY, box.maxZ))
                    .forEach { pos ->
                        val tile = world.getTileEntity(pos)
                        if (tile != null
                            && !tile.isInvalid
                            && !internalBlacklist.contains(tile.javaClass.toString())
                            && !blacklist.contains(TileEntity.getKey(tile.javaClass).toString())
                        ) {
//                        when {
//                            tile is IKhronusTickable<*> -> khronusTickable.add(tile)
//                            tile.hasCapability(KhronusApi.KHRONUS_TICKABLE, null) -> tile.asKhronusTickable()
//                                ?.let(khronusTickable::add)
//                            tile is ITickable -> tickable.add(tile)
//                        }

                            when {
                                tile is IKhronusTickable<*> -> tile.update(kTicks, khronusBonusTicks)
                                tile.hasCapability(KhronusApi.KHRONUS_TICKABLE, null) -> tile.asKhronusTickable()
                                    ?.update(kTicks, khronusBonusTicks)
                                tile is ITickable -> repeat(bonusTicks) { tile.update() }
                            }
                        }
                    }
            }
        }
    }

    private fun speedUpRandomTicks(world: World, bonusTicks: Int, box: AxisAlignedBB?) {
        if (box != null && bonusTicks != 0) {
            val blacklist = ProjectEConfig.effects.timeWatchBlockBlacklist.toList()

            BlockPos.getAllInBox(BlockPos(box.minX, box.minY, box.minZ), BlockPos(box.maxX, box.maxY, box.maxZ))
                .forEach { pos ->
                    val state = world.getBlockState(pos)
                    val block = state.block
                    if (block.tickRandomly && !blacklist.contains(block.registryName.toString()) && block !is BlockLiquid && block !is BlockFluidBase && block !is IGrowable && block !is IPlantable) {
                        repeat(bonusTicks) { block.updateTick(world, pos, state, world.rand) }
                    }
                }
        }
    }

    private fun slowMobs(world: World, bBox: AxisAlignedBB?, mobSlowdown: Float) {
        if (bBox != null) {
            world.getEntitiesWithinAABB(EntityLiving::class.java, bBox)
                .forEach { entity ->
                    if (entity.motionX != 0.0) {
                        entity.motionX *= mobSlowdown.toDouble()
                    }
                    if (entity.motionZ != 0.0) {
                        entity.motionZ *= mobSlowdown.toDouble()
                    }
                }
        }
    }

    override fun getPedestalDescription(): MutableList<String> {
        val list: MutableList<String> = ArrayList()
        if (ProjectEConfig.effects.timePedBonus > 0) {
            list.add(TextFormatting.BLUE.toString() + I18n.format("pe.timewatch.pedestal1",
                ProjectEConfig.effects.timePedBonus))
        }

        if (ProjectEConfig.effects.timePedMobSlowness < 1.0f) {
            list.add(TextFormatting.BLUE.toString() + I18n.format("pe.timewatch.pedestal2",
                ProjectEConfig.effects.timePedMobSlowness))
        }

        return list
    }
}