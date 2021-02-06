package dev.brella.khronus.watchdogs

import dev.brella.khronus.*
import net.minecraft.crash.CrashReport
import net.minecraft.crash.ReportedException
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.ForgeConfig
import net.minecraftforge.server.timings.TimeTracker
import org.apache.logging.log4j.LogManager
import kotlin.system.measureNanoTime

object KhronusSynchronousWorldProcessingWithTiming : KhronusWorld() {
//    override fun updateEntities(world: World) = with(world) {
//        val tick = TickDog.ticks[world] ?: 0
//
//        TickDog.worldTickLengths.computeIfAbsent(world) { ArrayList(20) }
//            .add(System.currentTimeMillis() to measureNanoTime {
//                val khronusTileEntities = khronusTickableTileEntities
//                val khronusTickAcceleration = tickAcceleration
//                val khronusTickCheckup = tickCheckup
//                val khronusTickLength = tickLength
//
//                profiler.section("Khronus Synchonous tickableTileEntities") {
//                    val tickableIterator = tickableTileEntities.iterator()
//                    while (tickableIterator.hasNext()) {
//                        val tileEntity = tickableIterator.next()
//                        if (!tileEntity.isRemoved && tileEntity.hasWorld()) {
//                            val blockPos = tileEntity.pos
//                            //Forge: Fix TE's getting an extra tick on the client side....
//                            if (this.isBlockLoaded(
//                                    blockPos,
//                                    false
//                                ) && worldBorder.contains(blockPos)
//                            ) {
//                                try {
//                                    profiler.section("Khronus Watchdog") {
//                                        val maxTime =
//                                            TickDog.maxTickTime[tileEntity::class.java] ?: TickDog.defaultMaxTickTime
//                                        var checkup = khronusTickCheckup[tileEntity]
//
//                                        val taken =
//                                            profiler.section({ TileEntity.getKey(tileEntity.javaClass).toString() }) {
//                                                TimeTracker.TILE_ENTITY_UPDATE.trackStart(tileEntity)
//
//                                                val tickable = tileEntity as ITickable
//
//                                                val taken =
//                                                    measureNanoTime { tickable.update() }.nanosecondsToMicrosecondApprox()
//
//                                                khronusTickAcceleration.remove(tileEntity)?.let { acceleration ->
//                                                    for (i in 0 until acceleration) tickable.update()
//                                                }
//
//                                                TimeTracker.TILE_ENTITY_UPDATE.trackEnd(tileEntity)
//
//                                                taken
//                                            }
//
//                                        khronusTickLength[tileEntity] = taken
//
//                                        if (checkup != null) {
//                                            checkup += 1 + (taken.toInt() shl 8)
//
//                                            if (checkup and 0xFF >= TickDog.checkupLimit) {
//                                                val averageTickTime = (checkup shr 8) shr TickDog.checkupLimitExponent
//                                                if (averageTickTime > maxTime) {
//                                                    tickableIterator.remove()
//                                                    khronusTileEntities[tileEntity] =
//                                                        TemporalBounds(TickDog.microsecondsToTickDelay(
//                                                            averageTickTime),
//                                                            max(maxTime, (averageTickTime - maxTime).toInt()),
//                                                            (averageTickTime + (maxTime shl 1)).toInt())
//                                                }
//
//                                                khronusTickCheckup.remove(tileEntity)
//                                            } else {
//                                                khronusTickCheckup[tileEntity] = checkup
//                                            }
//                                        } else if (taken > maxTime) {
//                                            khronusTickCheckup[tileEntity] = 1L or (taken shl 8)
//                                        }
//
//                                        Unit
//                                    }
//                                } catch (throwable: Throwable) {
//                                    val crashReport = CrashReport.makeCrashReport(throwable, "Ticking block entity")
//                                    val crashReportCategory = crashReport.makeCategory("Block entity being ticked")
//                                    tileEntity.addInfoToCrashReport(crashReportCategory)
//                                    if (ForgeModContainer.removeErroringTileEntities) {
//                                        FMLLog.log.fatal("{}", crashReport.completeReport)
//                                        tileEntity.invalidate()
//                                        removeTileEntity(tileEntity.pos)
//                                    } else throw ReportedException(crashReport)
//                                }
//                            }
//                        }
//
//                        if (tileEntity.isInvalid) {
//                            tickableIterator.remove()
//                            loadedTileEntityList.remove(tileEntity)
//                            if (this.isBlockLoaded(tileEntity.pos)) {
//                                //Forge: Bugfix: If we set the tile entity it immediately sets it in the chunk, so we could be desyned
//                                val chunk = this.getChunk(tileEntity.pos)
//                                if (chunk.getTileEntity(
//                                        tileEntity.pos,
//                                        Chunk.EnumCreateEntityType.CHECK
//                                    ) === tileEntity
//                                ) chunk.removeTileEntity(tileEntity.pos)
//                            }
//                        }
//                    }
//                }
//
//                profiler.section("Khronus Synchronous khronusTileEntities[world]") {
//                    val khronusIterator = khronusTileEntities.iterator()
//
//                    while (khronusIterator.hasNext()) {
//                        val (tileEntity, tickRate) = khronusIterator.next()
//                        if (!tileEntity.isInvalid && tileEntity.hasWorld()) {
//                            if (tick % tickRate.tickRate != 0) continue
//
//                            val blockPos = tileEntity.pos
//                            //Forge: Fix TE's getting an extra tick on the client side....
//                            if (this.isBlockLoaded(
//                                    blockPos,
//                                    false
//                                ) && worldBorder.contains(blockPos)
//                            ) {
//                                try {
//                                    profiler.section("Khronus Watchdog") {
//                                        val maxTime =
//                                            TickDog.maxTickTime[tileEntity::class.java] ?: TickDog.defaultMaxTickTime
//                                        var checkup = khronusTickCheckup[tileEntity]
//
//                                        val taken =
//                                            profiler.section({ TileEntity.getKey(tileEntity.javaClass).toString() }) {
//                                                TimeTracker.TILE_ENTITY_UPDATE.trackStart(tileEntity)
//
//                                                val acceleration = khronusTickAcceleration.remove(tileEntity)
//
//                                                val taken: Long
//
//                                                when (tileEntity) {
//                                                    is IKhronusTickable ->
//                                                        taken = measureNanoTime {
//                                                            tileEntity.tick(tickRate.tickRate, acceleration ?: 0)
//                                                        }.nanosecondsToMicrosecondApprox()
//                                                    is ITickable -> {
//                                                        taken = measureNanoTime {
//                                                            tileEntity.update()
//                                                        }.nanosecondsToMicrosecondApprox()
//
//                                                        if (acceleration != null) for (i in 0 until acceleration) tileEntity.update()
//                                                    }
//                                                    else -> taken = 0
//                                                }
//
//                                                TimeTracker.TILE_ENTITY_UPDATE.trackEnd(tileEntity)
//
//                                                taken
//                                            }
//
//                                        khronusTickLength[tileEntity] = taken
//
//                                        if (checkup != null) {
//                                            checkup += 1 + (taken.toInt() shl 8)
//
//                                            if (checkup and 0xFF >= TickDog.checkupLimit) {
//                                                val averageTickTime = (checkup shr 8) shr TickDog.checkupLimitExponent
//                                                if (averageTickTime > maxTime) {
//                                                    khronusTileEntities[tileEntity] =
//                                                        TemporalBounds(TickDog.microsecondsToTickDelay(
//                                                            averageTickTime),
//                                                            max(maxTime, (averageTickTime - maxTime).toInt()),
//                                                            (averageTickTime + (maxTime shl 1)).toInt())
//                                                } else if (tickRate.minTime != null && averageTickTime < tickRate.minTime) {
//                                                    if (tileEntity is IKhronusTickable) {
//                                                        khronusTileEntities[tileEntity] =
//                                                            tickRate.copy(tickRate = 1, minTime = null, maxTime = null)
//                                                    } else {
//                                                        khronusIterator.remove()
//                                                        tickableTileEntities.add(tileEntity)
//                                                    }
//                                                }
//
//                                                khronusTickCheckup.remove(tileEntity)
//                                            } else {
//                                                khronusTickCheckup[tileEntity] = checkup
//                                            }
//                                        } else if (taken > (tickRate.maxTime ?: maxTime)) {
//                                            khronusTickCheckup[tileEntity] =
//                                                1L or (taken shl 8)
//                                        } else if (tickRate.minTime != null && taken < tickRate.minTime) {
//                                            khronusTickCheckup[tileEntity] =
//                                                1L or (taken shl 8)
//                                        }
//
//                                        Unit
//                                    }
//                                } catch (throwable: Throwable) {
//                                    val crashReport = CrashReport.makeCrashReport(throwable, "Ticking block entity")
//                                    val crashReportCategory = crashReport.makeCategory("Block entity being ticked")
//                                    tileEntity.addInfoToCrashReport(crashReportCategory)
//                                    if (ForgeModContainer.removeErroringTileEntities) {
//                                        FMLLog.log.fatal("{}", crashReport.completeReport)
//                                        tileEntity.invalidate()
//                                        removeTileEntity(tileEntity.pos)
//                                    } else throw ReportedException(crashReport)
//                                }
//                            }
//                        }
//
//                        if (tileEntity.isInvalid) {
//                            khronusIterator.remove()
//                            loadedTileEntityList.remove(tileEntity)
//                            if (this.isBlockLoaded(tileEntity.pos)) {
//                                //Forge: Bugfix: If we set the tile entity it immediately sets it in the chunk, so we could be desyned
//                                val chunk = this.getChunk(tileEntity.pos)
//                                if (chunk.getTileEntity(
//                                        tileEntity.pos,
//                                        Chunk.EnumCreateEntityType.CHECK
//                                    ) === tileEntity
//                                ) chunk.removeTileEntity(tileEntity.pos)
//                            }
//                        }
//                    }
//                }
//            } / 1_000_000.0)
//
//        TickDog.ticks[world] = tick + 1
//    }

    override fun tickBlockEntities(world: World) = with(world) {
        TickDog.worldTickLengths.computeIfAbsent(world) { ArrayList(20) }
            .add(System.currentTimeMillis() to measureNanoTime {
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
                val khronusTickLength = tickLength

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
            } / 1_000_000.0)

        TickDog.ticks[world] = TickDog.ticks[world]?.plus(1) ?: 1

//        tickableTileEntities.sortBy(khronusTickLength::get)
    }
}