package dev.brella.khronus.watchdogs

import dev.brella.khronus.section
import dev.brella.khronus.getTileEntitiesToBeRemoved
import net.minecraft.crash.CrashReport
import net.minecraft.crash.ReportedException
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.ForgeConfig
import net.minecraftforge.server.timings.TimeTracker
import org.apache.logging.log4j.LogManager

object VanillaWorldProcessing : KhronusWatchdog {
    override fun tickBlockEntities(world: World) = with(world) {
        val iprofiler = profiler
        iprofiler.startSection("blockEntities")
        processingLoadedTiles = true // Forge: Move above remove to prevent CMEs

        getTileEntitiesToBeRemoved { tileEntitiesToBeRemoved ->
            if (tileEntitiesToBeRemoved.isNotEmpty()) {
                tileEntitiesToBeRemoved.forEach(TileEntity::onChunkUnloaded)
                tickableTileEntities.removeAll(tileEntitiesToBeRemoved)
                loadedTileEntityList.removeAll(tileEntitiesToBeRemoved)
                tileEntitiesToBeRemoved.clear()
            }
        }

        val iterator = tickableTileEntities.iterator()

        while (iterator.hasNext()) {
            val tileentity = iterator.next()
            if (!tileentity.isRemoved && tileentity.hasWorld()) {
                val blockpos = tileentity.pos
                if (this.chunkProvider.canTick(blockpos) && worldBorder.contains(blockpos)) {
                    try {
                        TimeTracker.TILE_ENTITY_UPDATE.trackStart(tileentity)
                        iprofiler.section({ tileentity.type.registryName.toString() }) {
                            //TODO: Optimise this in other versions
                            if (tileentity.type.isValidBlock(getBlockState(blockpos).block)) {
                                (tileentity as ITickableTileEntity).tick()
                            } else {
                                tileentity.warnInvalidBlock()
                            }
                        }
                    } catch (throwable: Throwable) {
                        val crashreport = CrashReport.makeCrashReport(throwable, "Ticking block entity")
                        val crashreportcategory = crashreport.makeCategory("Block entity being ticked")
                        tileentity.addInfoToCrashReport(crashreportcategory)
                        if (ForgeConfig.SERVER.removeErroringTileEntities.get()) {
                            LogManager.getLogger().fatal("{}", crashreport.completeReport)
                            tileentity.remove()
                            this.removeTileEntity(tileentity.pos)
                        } else throw ReportedException(crashreport)
                    } finally {
                        TimeTracker.TILE_ENTITY_UPDATE.trackEnd(tileentity)
                    }
                }
            }
            if (tileentity.isRemoved) {
                iterator.remove()
                loadedTileEntityList.remove(tileentity)
                if (isBlockLoaded(tileentity.pos)) {
                    //Forge: Bugfix: If we set the tile entity it immediately sets it in the chunk, so we could be desyned
                    val chunk = getChunkAt(tileentity.pos)
                    if (chunk.getTileEntity(tileentity.pos, Chunk.CreateEntityType.CHECK) === tileentity)
                        chunk.removeTileEntity(tileentity.pos)
                }
            }
        }

        processingLoadedTiles = false
        iprofiler.endStartSection("pendingBlockEntities")
        if (addedTileEntityList.isNotEmpty()) {
            for (i in addedTileEntityList.indices) {
                val tileentity1 = addedTileEntityList[i]
                if (!tileentity1.isRemoved) {
                    if (!loadedTileEntityList.contains(tileentity1)) {
                        this.addTileEntity(tileentity1)
                    }
                    if (isBlockLoaded(tileentity1.pos)) {
                        val chunk = getChunkAt(tileentity1.pos)
                        val blockstate = chunk.getBlockState(tileentity1.pos)
                        chunk.addTileEntity(tileentity1.pos, tileentity1)
                        notifyBlockUpdate(tileentity1.pos, blockstate, blockstate, 3)
                    }
                }
            }
            addedTileEntityList.clear()
        }

        iprofiler.endSection()
    }
}