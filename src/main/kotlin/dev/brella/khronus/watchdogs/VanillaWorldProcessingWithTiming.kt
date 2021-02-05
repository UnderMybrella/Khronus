package dev.brella.khronus.watchdogs

import dev.brella.khronus.TickDog
import dev.brella.khronus.nanosecondsToMicrosecondApprox
import dev.brella.khronus.section
import dev.brella.khronus.set
import net.minecraft.crash.CrashReport
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ITickable
import net.minecraft.util.ReportedException
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.ForgeModContainer
import net.minecraftforge.fml.common.FMLLog
import net.minecraftforge.server.timings.TimeTracker
import kotlin.system.measureNanoTime

object VanillaWorldProcessingWithTiming : KhronusWatchdog {
    override fun updateEntities(world: World) = with(world) {
        val iterator = tickableTileEntities.iterator()
        val khronusTickLength = tickLength

        TickDog.worldTickLengths.computeIfAbsent(world) { ArrayList(20) }
            .add(System.currentTimeMillis() to measureNanoTime {
                while (iterator.hasNext()) {
                    val tileEntity = iterator.next()
                    if (!tileEntity.isInvalid && tileEntity.hasWorld()) {
                        val blockPos = tileEntity.pos
                        //Forge: Fix TE's getting an extra tick on the client side....
                        if (this.isBlockLoaded(
                                blockPos,
                                false
                            ) && worldBorder.contains(blockPos)
                        ) {
                            try {
                                profiler.section({ TileEntity.getKey(tileEntity.javaClass).toString() }) {
                                    TimeTracker.TILE_ENTITY_UPDATE.trackStart(tileEntity)
                                    val nanos = measureNanoTime { (tileEntity as ITickable).update() }
                                    TimeTracker.TILE_ENTITY_UPDATE.trackEnd(tileEntity)
                                    val taken = nanos.nanosecondsToMicrosecondApprox()

                                    khronusTickLength[tileEntity] = taken
                                }
                            } catch (throwable: Throwable) {
                                val crashReport = CrashReport.makeCrashReport(throwable, "Ticking block entity")
                                val crashReportCategory = crashReport.makeCategory("Block entity being ticked")
                                tileEntity.addInfoToCrashReport(crashReportCategory)
                                if (ForgeModContainer.removeErroringTileEntities) {
                                    FMLLog.log.fatal("{}", crashReport.completeReport)
                                    tileEntity.invalidate()
                                    removeTileEntity(tileEntity.pos)
                                } else throw ReportedException(crashReport)
                            }
                        }
                    }

                    if (tileEntity.isInvalid) {
                        iterator.remove()
                        loadedTileEntityList.remove(tileEntity)
                        if (this.isBlockLoaded(tileEntity.pos)) {
                            //Forge: Bugfix: If we set the tile entity it immediately sets it in the chunk, so we could be desyned
                            val chunk = this.getChunk(tileEntity.pos)
                            if (chunk.getTileEntity(
                                    tileEntity.pos,
                                    Chunk.EnumCreateEntityType.CHECK
                                ) === tileEntity
                            ) chunk.removeTileEntity(tileEntity.pos)
                        }
                    }
                }
            } / 1_000_000.0)

        TickDog.ticks[world] = TickDog.ticks[world]?.plus(1) ?: 1

        tickableTileEntities.sortBy(khronusTickLength::get)
    }
}