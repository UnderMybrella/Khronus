package dev.brella.khronus.watchdogs

import dev.brella.khronus.*
import dev.brella.khronus.api.IKhronusTickable
import dev.brella.khronus.api.TemporalBounds
import net.minecraft.crash.CrashReport
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ITickable
import net.minecraft.util.ReportedException
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.ForgeModContainer
import net.minecraftforge.fml.common.FMLLog
import net.minecraftforge.server.timings.TimeTracker
import kotlin.math.max
import kotlin.system.measureNanoTime

object KhronusSynchronousWorldProcessingWithTiming : KhronusWorld() {
    override fun updateEntities(world: World) = with(world) {
        val tick = TickDog.ticks[world] ?: 0

        TickDog.worldTickLengths.computeIfAbsent(world) { ArrayList(20) }
            .add(System.currentTimeMillis() to measureNanoTime {
                val khronusTileEntities = khronusTickableTileEntities
                val khronusTickAcceleration = tickAcceleration
                val khronusTickCheckup = tickCheckup
                val khronusTickLength = tickLength

                profiler.section("Khronus Synchonous tickableTileEntities") {
                    val tickableIterator = tickableTileEntities.iterator()
                    while (tickableIterator.hasNext()) {
                        val tileEntity = tickableIterator.next()
                        if (!tileEntity.isInvalid && tileEntity.hasWorld()) {
                            val blockPos = tileEntity.pos
                            //Forge: Fix TE's getting an extra tick on the client side....
                            if (this.isBlockLoaded(
                                    blockPos,
                                    false
                                ) && worldBorder.contains(blockPos)
                            ) {
                                try {
                                    profiler.section("Khronus Watchdog") {
                                        val maxTime =
                                            TickDog.maxTickTime[tileEntity::class.java] ?: TickDog.defaultMaxTickTime
                                        var checkup = khronusTickCheckup[tileEntity]

                                        val taken =
                                            profiler.section({ TileEntity.getKey(tileEntity.javaClass).toString() }) {
                                                TimeTracker.TILE_ENTITY_UPDATE.trackStart(tileEntity)

                                                val tickable = tileEntity as ITickable

                                                val taken =
                                                    measureNanoTime { tickable.update() }.nanosecondsToMicrosecondApprox()

                                                khronusTickAcceleration.remove(tileEntity)?.let { acceleration ->
                                                    for (i in 0 until acceleration) tickable.update()
                                                }

                                                TimeTracker.TILE_ENTITY_UPDATE.trackEnd(tileEntity)

                                                taken
                                            }

                                        khronusTickLength[tileEntity] = taken

                                        if (checkup != null) {
                                            checkup += 1 + (taken.toInt() shl 8)

                                            if (checkup and 0xFF >= TickDog.checkupLimit) {
                                                val averageTickTime = (checkup shr 8) shr TickDog.checkupLimitExponent
                                                if (averageTickTime > maxTime) {
                                                    tickableIterator.remove()
                                                    khronusTileEntities[tileEntity] =
                                                        TemporalBounds(TickDog.microsecondsToTickDelay(
                                                            averageTickTime),
                                                            max(maxTime, (averageTickTime - maxTime).toInt()),
                                                            (averageTickTime + (maxTime shl 1)).toInt())
                                                }

                                                khronusTickCheckup.remove(tileEntity)
                                            } else {
                                                khronusTickCheckup[tileEntity] = checkup
                                            }
                                        } else if (taken > maxTime) {
                                            khronusTickCheckup[tileEntity] = 1L or (taken shl 8)
                                        }

                                        Unit
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
                            tickableIterator.remove()
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
                }

                profiler.section("Khronus Synchronous khronusTileEntities[world]") {
                    val khronusIterator = khronusTileEntities.iterator()

                    while (khronusIterator.hasNext()) {
                        val (tileEntity, tickRate) = khronusIterator.next()
                        if (!tileEntity.isInvalid && tileEntity.hasWorld()) {
                            if (tick % tickRate.tickRate != 0) continue

                            val blockPos = tileEntity.pos
                            //Forge: Fix TE's getting an extra tick on the client side....
                            if (this.isBlockLoaded(
                                    blockPos,
                                    false
                                ) && worldBorder.contains(blockPos)
                            ) {
                                try {
                                    profiler.section("Khronus Watchdog") {
                                        val maxTime =
                                            TickDog.maxTickTime[tileEntity::class.java] ?: TickDog.defaultMaxTickTime
                                        var checkup = khronusTickCheckup[tileEntity]

                                        val taken =
                                            profiler.section({ TileEntity.getKey(tileEntity.javaClass).toString() }) {
                                                TimeTracker.TILE_ENTITY_UPDATE.trackStart(tileEntity)

                                                val acceleration = khronusTickAcceleration.remove(tileEntity)

                                                val taken: Long

                                                when (tileEntity) {
                                                    is IKhronusTickable ->
                                                        taken = measureNanoTime {
                                                            tileEntity.update(tickRate.tickRate, acceleration ?: 0)
                                                        }.nanosecondsToMicrosecondApprox()
                                                    is ITickable -> {
                                                        taken = measureNanoTime {
                                                            tileEntity.update()
                                                        }.nanosecondsToMicrosecondApprox()

                                                        if (acceleration != null) for (i in 0 until acceleration) tileEntity.update()
                                                    }
                                                    else -> taken = 0
                                                }

                                                TimeTracker.TILE_ENTITY_UPDATE.trackEnd(tileEntity)

                                                taken
                                            }

                                        khronusTickLength[tileEntity] = taken

                                        if (checkup != null) {
                                            checkup += 1 + (taken.toInt() shl 8)

                                            if (checkup and 0xFF >= TickDog.checkupLimit) {
                                                val averageTickTime = (checkup shr 8) shr TickDog.checkupLimitExponent
                                                if (averageTickTime > maxTime) {
                                                    khronusTileEntities[tileEntity] =
                                                        TemporalBounds(TickDog.microsecondsToTickDelay(
                                                            averageTickTime),
                                                            max(maxTime, (averageTickTime - maxTime).toInt()),
                                                            (averageTickTime + (maxTime shl 1)).toInt())
                                                } else if (tickRate.minTime != null && averageTickTime < tickRate.minTime) {
                                                    if (tileEntity is IKhronusTickable) {
                                                        khronusTileEntities[tileEntity] =
                                                            tickRate.copy(tickRate = 1, minTime = null, maxTime = null)
                                                    } else {
                                                        khronusIterator.remove()
                                                        tickableTileEntities.add(tileEntity)
                                                    }
                                                }

                                                khronusTickCheckup.remove(tileEntity)
                                            } else {
                                                khronusTickCheckup[tileEntity] = checkup
                                            }
                                        } else if (taken > (tickRate.maxTime ?: maxTime)) {
                                            khronusTickCheckup[tileEntity] =
                                                1L or (taken shl 8)
                                        } else if (tickRate.minTime != null && taken < tickRate.minTime) {
                                            khronusTickCheckup[tileEntity] =
                                                1L or (taken shl 8)
                                        }

                                        Unit
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
                            khronusIterator.remove()
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
                }
            } / 1_000_000.0)

        TickDog.ticks[world] = tick + 1
    }
}