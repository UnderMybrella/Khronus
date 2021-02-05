package dev.brella.khronus

import com.google.common.base.Function
import com.google.common.base.Predicate
import com.google.common.collect.ImmutableSetMultimap
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.crash.CrashReport
import net.minecraft.crash.CrashReportCategory
import net.minecraft.entity.Entity
import net.minecraft.entity.EnumCreatureType
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraft.scoreboard.Scoreboard
import net.minecraft.server.MinecraftServer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.*
import net.minecraft.village.VillageCollection
import net.minecraft.world.*
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.BiomeProvider
import net.minecraft.world.border.WorldBorder
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.IChunkProvider
import net.minecraft.world.gen.structure.StructureBoundingBox
import net.minecraft.world.storage.ISaveHandler
import net.minecraft.world.storage.MapStorage
import net.minecraft.world.storage.WorldInfo
import net.minecraft.world.storage.WorldSavedData
import net.minecraft.world.storage.loot.LootTableManager
import net.minecraftforge.common.ForgeChunkManager
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fml.common.ObfuscationReflectionHelper
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.ref.WeakReference
import java.lang.reflect.Method
import java.util.*

open class WorldDelegate<T: World>(protected val world: WeakReference<T>) :
    World(world.get()!!.saveHandler, world.get()!!.worldInfo, world.get()!!.provider, world.get()!!.profiler, world.get()!!.isRemote) {
    companion object {
        private val LOOKUP = MethodHandles.lookup()
        fun Method.toMethodHandle(): MethodHandle = LOOKUP.unreflect(this.apply { isAccessible = true })

        val TICK_PLAYERS = ObfuscationReflectionHelper.findMethod(World::class.java, "tickPlayers", Void.TYPE).toMethodHandle()
        val UPDATE_WEATHER = ObfuscationReflectionHelper.findMethod(World::class.java, "updateWeather", Void.TYPE).toMethodHandle()
        val PLAY_MOOD_SOUND_AND_CHECK_LIGHT = ObfuscationReflectionHelper.findMethod(World::class.java, "playMoodSoundAndCheckLight", Void.TYPE, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Chunk::class.java).toMethodHandle()
        val UPDATE_BLOCKS = ObfuscationReflectionHelper.findMethod(World::class.java, "updateBlocks", Void.TYPE).toMethodHandle()
        val IS_CHUNK_LOADED = ObfuscationReflectionHelper.findMethod(World::class.java, "isChunkLoaded", Boolean::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Boolean::class.javaPrimitiveType).toMethodHandle()

//        val UPDATE_WEATHER
    }

    override fun getBiome(pos: BlockPos): Biome = world.get()!!.getBiome(pos)
    override fun getBiomeForCoordsBody(pos: BlockPos): Biome = world.get()!!.getBiomeForCoordsBody(pos)
    override fun getBiomeProvider(): BiomeProvider = world.get()!!.biomeProvider
    override fun createChunkProvider(): IChunkProvider = world.get()!!.chunkProvider
    override fun initialize(settings: WorldSettings) {} //Already initialised
    override fun getMinecraftServer(): MinecraftServer? = world.get()!!.minecraftServer
    override fun setInitialSpawnLocation() = world.get()!!.setInitialSpawnLocation()
    override fun getGroundAboveSeaLevel(pos: BlockPos): IBlockState = world.get()!!.getGroundAboveSeaLevel(pos)
    override fun isValid(pos: BlockPos): Boolean = world.get()!!.isValid(pos)
    override fun isOutsideBuildHeight(pos: BlockPos): Boolean = world.get()!!.isOutsideBuildHeight(pos)
    override fun isAirBlock(pos: BlockPos): Boolean = world.get()!!.isAirBlock(pos)
    override fun isBlockLoaded(pos: BlockPos): Boolean = world.get()!!.isBlockLoaded(pos)
    override fun isBlockLoaded(pos: BlockPos, allowEmpty: Boolean): Boolean = world.get()!!.isBlockLoaded(pos, allowEmpty)
    override fun isAreaLoaded(center: BlockPos, radius: Int): Boolean = world.get()!!.isAreaLoaded(center, radius)
    override fun isAreaLoaded(center: BlockPos, radius: Int, allowEmpty: Boolean): Boolean =
        world.get()!!.isAreaLoaded(center, radius, allowEmpty)

    override fun isAreaLoaded(from: BlockPos, to: BlockPos): Boolean = world.get()!!.isAreaLoaded(from, to)
    override fun isAreaLoaded(from: BlockPos, to: BlockPos, allowEmpty: Boolean): Boolean =
        world.get()!!.isAreaLoaded(from, to, allowEmpty)

    override fun isAreaLoaded(box: StructureBoundingBox): Boolean = world.get()!!.isAreaLoaded(box)
    override fun isAreaLoaded(box: StructureBoundingBox, allowEmpty: Boolean): Boolean =
        world.get()!!.isAreaLoaded(box, allowEmpty)

    override fun isChunkLoaded(x: Int, z: Int, allowEmpty: Boolean): Boolean =
        IS_CHUNK_LOADED.invokeExact(world, x, z, allowEmpty) as Boolean
    override fun getChunk(pos: BlockPos): Chunk = world.get()!!.getChunk(pos)
    override fun getChunk(chunkX: Int, chunkZ: Int): Chunk = world.get()!!.getChunk(chunkX, chunkZ)
    override fun isChunkGeneratedAt(x: Int, z: Int): Boolean = world.get()!!.isChunkGeneratedAt(x, z)
    override fun setBlockState(pos: BlockPos, state: IBlockState): Boolean = world.get()!!.setBlockState(pos, state)
    override fun setBlockState(pos: BlockPos, newState: IBlockState, flags: Int): Boolean =
        world.get()!!.setBlockState(pos, newState, flags)

    override fun markAndNotifyBlock(
        pos: BlockPos,
        chunk: Chunk?,
        iblockstate: IBlockState,
        newState: IBlockState,
        flags: Int
    ) = world.get()!!.markAndNotifyBlock(pos, chunk, iblockstate, newState, flags)

    override fun setBlockToAir(pos: BlockPos): Boolean = world.get()!!.setBlockToAir(pos)
    override fun destroyBlock(pos: BlockPos, dropBlock: Boolean): Boolean = world.get()!!.destroyBlock(pos, dropBlock)
    override fun notifyBlockUpdate(pos: BlockPos, oldState: IBlockState, newState: IBlockState, flags: Int) =
        world.get()!!.notifyBlockUpdate(pos, oldState, newState, flags)

    override fun notifyNeighborsRespectDebug(pos: BlockPos, blockType: Block, updateObservers: Boolean) =
        world.get()!!.notifyNeighborsRespectDebug(pos, blockType, updateObservers)

    override fun markBlocksDirtyVertical(x: Int, z: Int, y1: Int, y2: Int) =
        world.get()!!.markBlocksDirtyVertical(x, z, y1, y2)

    override fun markBlockRangeForRenderUpdate(rangeMin: BlockPos, rangeMax: BlockPos) =
        world.get()!!.markBlockRangeForRenderUpdate(rangeMin, rangeMax)

    override fun markBlockRangeForRenderUpdate(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int) =
        world.get()!!.markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2)

    override fun updateObservingBlocksAt(pos: BlockPos, blockType: Block) =
        world.get()!!.updateObservingBlocksAt(pos, blockType)

    override fun notifyNeighborsOfStateChange(pos: BlockPos, blockType: Block, updateObservers: Boolean) =
        world.get()!!.notifyNeighborsOfStateChange(pos, blockType, updateObservers)

    override fun notifyNeighborsOfStateExcept(pos: BlockPos, blockType: Block, skipSide: EnumFacing) =
        world.get()!!.notifyNeighborsOfStateExcept(pos, blockType, skipSide)

    override fun neighborChanged(pos: BlockPos, blockIn: Block, fromPos: BlockPos) =
        world.get()!!.neighborChanged(pos, blockIn, fromPos)

    override fun observedNeighborChanged(pos: BlockPos, changedBlock: Block, changedBlockPos: BlockPos) =
        world.get()!!.observedNeighborChanged(pos, changedBlock, changedBlockPos)

    override fun isBlockTickPending(pos: BlockPos, blockType: Block): Boolean =
        world.get()!!.isBlockTickPending(pos, blockType)

    override fun canSeeSky(pos: BlockPos): Boolean =
        world.get()!!.canSeeSky(pos)

    override fun canBlockSeeSky(pos: BlockPos): Boolean =
        world.get()!!.canBlockSeeSky(pos)

    override fun getLight(pos: BlockPos): Int =
        world.get()!!.getLight(pos)

    override fun getLightFromNeighbors(pos: BlockPos): Int =
        world.get()!!.getLightFromNeighbors(pos)

    override fun getLight(pos: BlockPos, checkNeighbors: Boolean): Int =
        world.get()!!.getLight(pos, checkNeighbors)

    override fun getHeight(pos: BlockPos): BlockPos =
        world.get()!!.getHeight(pos)

    override fun getHeight(x: Int, z: Int): Int =
        world.get()!!.getHeight(x, z)

    override fun getChunksLowestHorizon(x: Int, z: Int): Int =
        world.get()!!.getChunksLowestHorizon(x, z)

    override fun getLightFromNeighborsFor(type: EnumSkyBlock, pos: BlockPos): Int =
        world.get()!!.getLightFromNeighborsFor(type, pos)

    override fun getLightFor(type: EnumSkyBlock, pos: BlockPos): Int =
        world.get()!!.getLightFor(type, pos)

    override fun setLightFor(type: EnumSkyBlock, pos: BlockPos, lightValue: Int) =
        world.get()!!.setLightFor(type, pos, lightValue)

    override fun notifyLightSet(pos: BlockPos) =
        world.get()!!.notifyLightSet(pos)

    override fun getCombinedLight(pos: BlockPos, lightValue: Int): Int =
        world.get()!!.getCombinedLight(pos, lightValue)

    override fun getLightBrightness(pos: BlockPos): Float =
        world.get()!!.getLightBrightness(pos)

    override fun getBlockState(pos: BlockPos): IBlockState =
        world.get()!!.getBlockState(pos)

    override fun isDaytime(): Boolean =
        world.get()!!.isDaytime

    override fun rayTraceBlocks(start: Vec3d, end: Vec3d): RayTraceResult? =
        world.get()!!.rayTraceBlocks(start, end)

    override fun rayTraceBlocks(start: Vec3d, end: Vec3d, stopOnLiquid: Boolean): RayTraceResult? =
        world.get()!!.rayTraceBlocks(start, end, stopOnLiquid)

    override fun rayTraceBlocks(
        vec31: Vec3d,
        vec32: Vec3d,
        stopOnLiquid: Boolean,
        ignoreBlockWithoutBoundingBox: Boolean,
        returnLastUncollidableBlock: Boolean
    ): RayTraceResult? =
        world.get()!!.rayTraceBlocks(vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock)

    override fun playSound(
        player: EntityPlayer?,
        pos: BlockPos,
        soundIn: SoundEvent,
        category: SoundCategory,
        volume: Float,
        pitch: Float
    ) = world.get()!!.playSound(player, pos, soundIn, category, volume, pitch)

    override fun playSound(
        player: EntityPlayer?,
        x: Double,
        y: Double,
        z: Double,
        soundIn: SoundEvent,
        category: SoundCategory,
        volume: Float,
        pitch: Float
    ) = world.get()!!.playSound(player, x, y, z, soundIn, category, volume, pitch)

    override fun playSound(
        x: Double,
        y: Double,
        z: Double,
        soundIn: SoundEvent,
        category: SoundCategory,
        volume: Float,
        pitch: Float,
        distanceDelay: Boolean
    ) = world.get()!!.playSound(x, y, z, soundIn, category, volume, pitch, distanceDelay)

    override fun playRecord(blockPositionIn: BlockPos, soundEventIn: SoundEvent?) =
        world.get()!!.playRecord(blockPositionIn, soundEventIn)

    override fun spawnParticle(
        particleType: EnumParticleTypes,
        xCoord: Double,
        yCoord: Double,
        zCoord: Double,
        xSpeed: Double,
        ySpeed: Double,
        zSpeed: Double,
        vararg parameters: Int
    ) = world.get()!!.spawnParticle(particleType, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, *parameters)

    override fun spawnAlwaysVisibleParticle(
        id: Int,
        x: Double,
        y: Double,
        z: Double,
        xSpeed: Double,
        ySpeed: Double,
        zSpeed: Double,
        vararg parameters: Int
    ) = world.get()!!.spawnAlwaysVisibleParticle(id, x, y, z, xSpeed, ySpeed, zSpeed, *parameters)

    override fun spawnParticle(
        particleType: EnumParticleTypes,
        ignoreRange: Boolean,
        xCoord: Double,
        yCoord: Double,
        zCoord: Double,
        xSpeed: Double,
        ySpeed: Double,
        zSpeed: Double,
        vararg parameters: Int
    ) = world.get()!!.spawnParticle(particleType, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, *parameters)

    override fun addWeatherEffect(entityIn: Entity): Boolean =
        world.get()!!.addWeatherEffect(entityIn)

    override fun spawnEntity(entityIn: Entity): Boolean =
        world.get()!!.spawnEntity(entityIn)

    override fun onEntityAdded(entityIn: Entity) =
        world.get()!!.onEntityAdded(entityIn)

    override fun onEntityRemoved(entityIn: Entity) =
        world.get()!!.onEntityRemoved(entityIn)

    override fun removeEntity(entityIn: Entity) =
        world.get()!!.removeEntity(entityIn)

    override fun removeEntityDangerously(entityIn: Entity) =
        world.get()!!.removeEntityDangerously(entityIn)

    override fun addEventListener(listener: IWorldEventListener) =
        world.get()!!.addEventListener(listener)

    override fun getCollisionBoxes(entityIn: Entity?, aabb: AxisAlignedBB): MutableList<AxisAlignedBB> =
        world.get()!!.getCollisionBoxes(entityIn, aabb)

    override fun removeEventListener(listener: IWorldEventListener) =
        world.get()!!.removeEventListener(listener)

    override fun isInsideWorldBorder(entityToCheck: Entity): Boolean =
        world.get()!!.isInsideWorldBorder(entityToCheck)

    override fun collidesWithAnyBlock(bbox: AxisAlignedBB): Boolean =
        world.get()!!.collidesWithAnyBlock(bbox)

    override fun calculateSkylightSubtracted(partialTicks: Float): Int =
        world.get()!!.calculateSkylightSubtracted(partialTicks)

    override fun getSunBrightness(partialTicks: Float): Float =
        world.get()!!.getSunBrightness(partialTicks)

    override fun getSunBrightnessFactor(partialTicks: Float): Float =
        world.get()!!.getSunBrightnessFactor(partialTicks)

    override fun getSunBrightnessBody(partialTicks: Float): Float =
        world.get()!!.getSunBrightnessBody(partialTicks)

    override fun getSkyColor(entityIn: Entity, partialTicks: Float): Vec3d =
        world.get()!!.getSkyColor(entityIn, partialTicks)

    override fun getSkyColorBody(entityIn: Entity, partialTicks: Float): Vec3d =
        world.get()!!.getSkyColorBody(entityIn, partialTicks)

    override fun getCelestialAngle(partialTicks: Float): Float =
        world.get()!!.getCelestialAngle(partialTicks)

    override fun getMoonPhase(): Int =
        world.get()!!.moonPhase

    override fun getCurrentMoonPhaseFactor(): Float =
        world.get()!!.currentMoonPhaseFactor

    override fun getCurrentMoonPhaseFactorBody(): Float =
        world.get()!!.currentMoonPhaseFactorBody

    override fun getCelestialAngleRadians(partialTicks: Float): Float =
        world.get()!!.getCelestialAngleRadians(partialTicks)

    override fun getCloudColour(partialTicks: Float): Vec3d =
        world.get()!!.getCloudColour(partialTicks)

    override fun getCloudColorBody(partialTicks: Float): Vec3d =
        world.get()!!.getCloudColorBody(partialTicks)

    override fun getFogColor(partialTicks: Float): Vec3d =
        world.get()!!.getFogColor(partialTicks)

    override fun getPrecipitationHeight(pos: BlockPos): BlockPos =
        world.get()!!.getPrecipitationHeight(pos)

    override fun getTopSolidOrLiquidBlock(pos: BlockPos): BlockPos =
        world.get()!!.getTopSolidOrLiquidBlock(pos)

    override fun getHeight(): Int = world.get()!!.height

    override fun getTileEntity(pos: BlockPos): TileEntity? = world.get()!!.getTileEntity(pos)

    override fun getStrongPower(pos: BlockPos, direction: EnumFacing): Int = world.get()!!.getStrongPower(pos, direction)

    override fun getStrongPower(pos: BlockPos): Int = world.get()!!.getStrongPower(pos)

    override fun getWorldType(): WorldType = world.get()!!.worldType

    override fun isSideSolid(pos: BlockPos, side: EnumFacing): Boolean = world.get()!!.isSideSolid(pos, side)

    override fun isSideSolid(pos: BlockPos, side: EnumFacing, _default: Boolean): Boolean =
        world.get()!!.isSideSolid(pos, side, _default)

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        world.get()!!.hasCapability(capability, facing)

    override fun <T : Any?> getCapability(capability: Capability<T>, facing: EnumFacing?): T? =
        world.get()!!.getCapability(capability, facing)

    override fun init(): World = world.get()!!.init()

    override fun getStarBrightness(partialTicks: Float): Float = world.get()!!.getStarBrightness(partialTicks)

    override fun getStarBrightnessBody(partialTicks: Float): Float = world.get()!!.getStarBrightnessBody(partialTicks)

    override fun isUpdateScheduled(pos: BlockPos, blk: Block): Boolean = world.get()!!.isUpdateScheduled(pos, blk)

    override fun scheduleUpdate(pos: BlockPos, blockIn: Block, delay: Int) = world.get()!!.scheduleUpdate(pos, blockIn, delay)

    override fun updateBlockTick(pos: BlockPos, blockIn: Block, delay: Int, priority: Int) =
        world.get()!!.updateBlockTick(pos, blockIn, delay, priority)

    override fun scheduleBlockUpdate(pos: BlockPos, blockIn: Block, delay: Int, priority: Int) =
        world.get()!!.scheduleBlockUpdate(pos, blockIn, delay, priority)

    override fun updateEntities() = world.get()!!.updateEntities()

    override fun tickPlayers() { TICK_PLAYERS.invokeExact(world) }

    override fun addTileEntity(tile: TileEntity): Boolean = world.get()!!.addTileEntity(tile)

    override fun addTileEntities(tileEntityCollection: MutableCollection<TileEntity>) =
        world.get()!!.addTileEntities(tileEntityCollection)

    override fun updateEntity(ent: Entity) = world.get()!!.updateEntity(ent)

    override fun updateEntityWithOptionalForce(entityIn: Entity, forceUpdate: Boolean) =
        world.get()!!.updateEntityWithOptionalForce(entityIn, forceUpdate)

    override fun checkNoEntityCollision(bb: AxisAlignedBB): Boolean = world.get()!!.checkNoEntityCollision(bb)

    override fun checkNoEntityCollision(bb: AxisAlignedBB, entityIn: Entity?): Boolean =
        world.get()!!.checkNoEntityCollision(bb, entityIn)

    override fun checkBlockCollision(bb: AxisAlignedBB): Boolean = world.get()!!.checkBlockCollision(bb)

    override fun containsAnyLiquid(bb: AxisAlignedBB): Boolean = world.get()!!.containsAnyLiquid(bb)

    override fun isFlammableWithin(bb: AxisAlignedBB): Boolean = world.get()!!.isFlammableWithin(bb)

    override fun handleMaterialAcceleration(bb: AxisAlignedBB, materialIn: Material, entityIn: Entity): Boolean =
        world.get()!!.handleMaterialAcceleration(bb, materialIn, entityIn)

    override fun isMaterialInBB(bb: AxisAlignedBB, materialIn: Material): Boolean = world.get()!!.isMaterialInBB(bb, materialIn)

    override fun createExplosion(
        entityIn: Entity?,
        x: Double,
        y: Double,
        z: Double,
        strength: Float,
        damagesTerrain: Boolean
    ): Explosion = world.get()!!.createExplosion(entityIn, x, y, z, strength, damagesTerrain)

    override fun newExplosion(
        entityIn: Entity?,
        x: Double,
        y: Double,
        z: Double,
        strength: Float,
        causesFire: Boolean,
        damagesTerrain: Boolean
    ): Explosion = world.get()!!.newExplosion(entityIn, x, y, z, strength, causesFire, damagesTerrain)

    override fun getBlockDensity(vec: Vec3d, bb: AxisAlignedBB): Float = world.get()!!.getBlockDensity(vec, bb)

    override fun extinguishFire(player: EntityPlayer?, pos: BlockPos, side: EnumFacing): Boolean =
        world.get()!!.extinguishFire(player, pos, side)

    override fun getDebugLoadedEntities(): String = world.get()!!.debugLoadedEntities

    override fun getProviderName(): String = world.get()!!.providerName

    override fun setTileEntity(pos: BlockPos, tileEntityIn: TileEntity?) = world.get()!!.setTileEntity(pos, tileEntityIn)

    override fun removeTileEntity(pos: BlockPos) = world.get()!!.removeTileEntity(pos)

    override fun markTileEntityForRemoval(tileEntityIn: TileEntity) = world.get()!!.markTileEntityForRemoval(tileEntityIn)

    override fun isBlockFullCube(pos: BlockPos): Boolean = world.get()!!.isBlockFullCube(pos)

    override fun isBlockNormalCube(pos: BlockPos, _default: Boolean): Boolean = world.get()!!.isBlockNormalCube(pos, _default)

    override fun calculateInitialSkylight() = world.get()!!.calculateInitialSkylight()

    override fun setAllowedSpawnTypes(hostile: Boolean, peaceful: Boolean) =
        world.get()!!.setAllowedSpawnTypes(hostile, peaceful)

    override fun tick() = world.get()!!.tick()

    //Should already be done
    override fun calculateInitialWeather() {}

    override fun calculateInitialWeatherBody() = world.get()!!.calculateInitialWeatherBody()

    override fun updateWeather() {
        UPDATE_WEATHER.invokeExact(world)
    }

    override fun updateWeatherBody() = world.get()!!.updateWeatherBody()

    override fun playMoodSoundAndCheckLight(x: Int, z: Int, chunkIn: Chunk) {
        PLAY_MOOD_SOUND_AND_CHECK_LIGHT.invokeExact(world, x, z, chunkIn)
    }

    override fun updateBlocks() {
        UPDATE_BLOCKS.invokeExact(world)
    }

    override fun immediateBlockTick(pos: BlockPos, state: IBlockState, random: Random) =
        world.get()!!.immediateBlockTick(pos, state, random)

    override fun canBlockFreezeWater(pos: BlockPos): Boolean = world.get()!!.canBlockFreezeWater(pos)

    override fun canBlockFreezeNoWater(pos: BlockPos): Boolean = world.get()!!.canBlockFreezeNoWater(pos)

    override fun canBlockFreeze(pos: BlockPos, noWaterAdj: Boolean): Boolean = world.get()!!.canBlockFreeze(pos, noWaterAdj)

    override fun canBlockFreezeBody(pos: BlockPos, noWaterAdj: Boolean): Boolean =
        world.get()!!.canBlockFreezeBody(pos, noWaterAdj)

    override fun canSnowAt(pos: BlockPos, checkLight: Boolean): Boolean = world.get()!!.canSnowAt(pos, checkLight)

    override fun canSnowAtBody(pos: BlockPos, checkLight: Boolean): Boolean = world.get()!!.canSnowAtBody(pos, checkLight)

    override fun checkLight(pos: BlockPos): Boolean = world.get()!!.checkLight(pos)

    override fun checkLightFor(lightType: EnumSkyBlock, pos: BlockPos): Boolean = world.get()!!.checkLightFor(lightType, pos)

    override fun tickUpdates(runAllPending: Boolean): Boolean = world.get()!!.tickUpdates(runAllPending)

    override fun getPendingBlockUpdates(chunkIn: Chunk, remove: Boolean): MutableList<NextTickListEntry>? =
        world.get()!!.getPendingBlockUpdates(chunkIn, remove)

    override fun getPendingBlockUpdates(
        structureBB: StructureBoundingBox,
        remove: Boolean
    ): MutableList<NextTickListEntry>? = world.get()!!.getPendingBlockUpdates(structureBB, remove)

    override fun getEntitiesWithinAABBExcludingEntity(entityIn: Entity?, bb: AxisAlignedBB): MutableList<Entity> =
        world.get()!!.getEntitiesWithinAABBExcludingEntity(entityIn, bb)

    override fun getEntitiesInAABBexcluding(
        entityIn: Entity?,
        boundingBox: AxisAlignedBB,
        predicate: Predicate<in Entity>?
    ): MutableList<Entity> = world.get()!!.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate)

    override fun <T : Entity?> getEntities(entityType: Class<out T>, filter: Predicate<in T>): MutableList<T> =
        world.get()!!.getEntities(entityType, filter)

    override fun <T : Entity?> getPlayers(playerType: Class<out T>, filter: Predicate<in T>): MutableList<T> =
        world.get()!!.getPlayers(playerType, filter)

    override fun <T : Entity?> getEntitiesWithinAABB(classEntity: Class<out T>, bb: AxisAlignedBB): MutableList<T> =
        world.get()!!.getEntitiesWithinAABB(classEntity, bb)

    override fun <T : Entity?> getEntitiesWithinAABB(
        clazz: Class<out T>,
        aabb: AxisAlignedBB,
        filter: Predicate<in T>?
    ): MutableList<T> = world.get()!!.getEntitiesWithinAABB(clazz, aabb, filter)

    override fun <T : Entity?> findNearestEntityWithinAABB(
        entityType: Class<out T>,
        aabb: AxisAlignedBB,
        closestTo: T
    ): T? = world.get()!!.findNearestEntityWithinAABB(entityType, aabb, closestTo)

    override fun getEntityByID(id: Int): Entity? = world.get()!!.getEntityByID(id)

    override fun getLoadedEntityList(): MutableList<Entity> = world.get()!!.getLoadedEntityList()

    override fun markChunkDirty(pos: BlockPos, unusedTileEntity: TileEntity) =
        world.get()!!.markChunkDirty(pos, unusedTileEntity)

    override fun countEntities(entityType: Class<*>): Int = world.get()!!.countEntities(entityType)

    override fun countEntities(type: EnumCreatureType, forSpawnCount: Boolean): Int =
        world.get()!!.countEntities(type, forSpawnCount)

    override fun loadEntities(entityCollection: MutableCollection<Entity>) = world.get()!!.loadEntities(entityCollection)

    override fun unloadEntities(entityCollection: MutableCollection<Entity>) = world.get()!!.unloadEntities(entityCollection)

    override fun mayPlace(
        blockIn: Block,
        pos: BlockPos,
        skipCollisionCheck: Boolean,
        sidePlacedOn: EnumFacing,
        placer: Entity?
    ): Boolean = world.get()!!.mayPlace(blockIn, pos, skipCollisionCheck, sidePlacedOn, placer)

    override fun getSeaLevel(): Int = world.get()!!.seaLevel

    override fun setSeaLevel(seaLevelIn: Int) {
        world.get()!!.seaLevel = seaLevelIn
    }

    override fun isSidePowered(pos: BlockPos, side: EnumFacing): Boolean = world.get()!!.isSidePowered(pos, side)

    override fun getRedstonePower(pos: BlockPos, facing: EnumFacing): Int = world.get()!!.getRedstonePower(pos, facing)

    override fun isBlockPowered(pos: BlockPos): Boolean = world.get()!!.isBlockPowered(pos)

    override fun getRedstonePowerFromNeighbors(pos: BlockPos): Int = world.get()!!.getRedstonePowerFromNeighbors(pos)

    override fun getClosestPlayerToEntity(entityIn: Entity, distance: Double): EntityPlayer? =
        world.get()!!.getClosestPlayerToEntity(entityIn, distance)

    override fun getNearestPlayerNotCreative(entityIn: Entity, distance: Double): EntityPlayer? =
        world.get()!!.getNearestPlayerNotCreative(entityIn, distance)

    override fun getClosestPlayer(
        posX: Double,
        posY: Double,
        posZ: Double,
        distance: Double,
        spectator: Boolean
    ): EntityPlayer? = world.get()!!.getClosestPlayer(posX, posY, posZ, distance, spectator)

    override fun getClosestPlayer(
        x: Double,
        y: Double,
        z: Double,
        distance: Double,
        predicate: Predicate<Entity>
    ): EntityPlayer? = world.get()!!.getClosestPlayer(x, y, z, distance, predicate)

    override fun isAnyPlayerWithinRangeAt(x: Double, y: Double, z: Double, range: Double): Boolean =
        world.get()!!.isAnyPlayerWithinRangeAt(x, y, z, range)

    override fun getNearestAttackablePlayer(
        entityIn: Entity,
        maxXZDistance: Double,
        maxYDistance: Double
    ): EntityPlayer? = world.get()!!.getNearestAttackablePlayer(entityIn, maxXZDistance, maxYDistance)

    override fun getNearestAttackablePlayer(pos: BlockPos, maxXZDistance: Double, maxYDistance: Double): EntityPlayer? =
        world.get()!!.getNearestAttackablePlayer(pos, maxXZDistance, maxYDistance)

    override fun getNearestAttackablePlayer(
        posX: Double,
        posY: Double,
        posZ: Double,
        maxXZDistance: Double,
        maxYDistance: Double,
        playerToDouble: Function<EntityPlayer, Double>?,
        predicate: Predicate<EntityPlayer>?
    ): EntityPlayer? {
        return super.getNearestAttackablePlayer(
            posX,
            posY,
            posZ,
            maxXZDistance,
            maxYDistance,
            playerToDouble,
            predicate
        )
    }

    override fun getPlayerEntityByName(name: String): EntityPlayer? = world.get()!!.getPlayerEntityByName(name)

    override fun getPlayerEntityByUUID(uuid: UUID): EntityPlayer? = world.get()!!.getPlayerEntityByUUID(uuid)

    override fun sendQuittingDisconnectingPacket() = world.get()!!.sendQuittingDisconnectingPacket()

    override fun checkSessionLock() = world.get()!!.checkSessionLock()

    override fun setTotalWorldTime(worldTime: Long) {
        world.get()!!.totalWorldTime = worldTime
    }

    override fun getSeed(): Long = world.get()!!.seed

    override fun getTotalWorldTime(): Long = world.get()!!.totalWorldTime

    override fun getWorldTime(): Long = world.get()!!.worldTime

    override fun setWorldTime(time: Long) {
        world.get()!!.worldTime = time
    }

    override fun getSpawnPoint(): BlockPos = world.get()!!.spawnPoint

    override fun setSpawnPoint(pos: BlockPos) {
        world.get()!!.spawnPoint = pos
    }

    override fun joinEntityInSurroundings(entityIn: Entity) = world.get()!!.joinEntityInSurroundings(entityIn)

    override fun isBlockModifiable(player: EntityPlayer, pos: BlockPos): Boolean = world.get()!!.isBlockModifiable(player, pos)

    override fun canMineBlockBody(player: EntityPlayer, pos: BlockPos): Boolean = world.get()!!.canMineBlockBody(player, pos)

    override fun setEntityState(entityIn: Entity, state: Byte) = world.get()!!.setEntityState(entityIn, state)

    override fun getChunkProvider(): IChunkProvider = world.get()!!.chunkProvider

    override fun addBlockEvent(pos: BlockPos, blockIn: Block, eventID: Int, eventParam: Int) =
        world.get()!!.addBlockEvent(pos, blockIn, eventID, eventParam)

    override fun getSaveHandler(): ISaveHandler = world.get()!!.saveHandler

    override fun getWorldInfo(): WorldInfo = world.get()!!.worldInfo

    override fun getGameRules(): GameRules = world.get()!!.gameRules

    override fun updateAllPlayersSleepingFlag() = world.get()!!.updateAllPlayersSleepingFlag()

    override fun getThunderStrength(delta: Float): Float = world.get()!!.getThunderStrength(delta)

    override fun setThunderStrength(strength: Float) = world.get()!!.setThunderStrength(strength)

    override fun getRainStrength(delta: Float): Float = world.get()!!.getRainStrength(delta)

    override fun setRainStrength(strength: Float) = world.get()!!.setRainStrength(strength)

    override fun isThundering(): Boolean = world.get()!!.isThundering

    override fun isRaining(): Boolean = world.get()!!.isRaining

    override fun isRainingAt(position: BlockPos): Boolean = world.get()!!.isRainingAt(position)

    override fun isBlockinHighHumidity(pos: BlockPos): Boolean = world.get()!!.isBlockinHighHumidity(pos)

    override fun getMapStorage(): MapStorage? = world.get()!!.mapStorage

    override fun setData(dataID: String, worldSavedDataIn: WorldSavedData) = world.get()!!.setData(dataID, worldSavedDataIn)

    override fun loadData(clazz: Class<out WorldSavedData>, dataID: String): WorldSavedData? =
        world.get()!!.loadData(clazz, dataID)

    override fun getUniqueDataId(key: String): Int = world.get()!!.getUniqueDataId(key)

    override fun playBroadcastSound(id: Int, pos: BlockPos, data: Int) = world.get()!!.playBroadcastSound(id, pos, data)

    override fun playEvent(type: Int, pos: BlockPos, data: Int) = world.get()!!.playEvent(type, pos, data)

    override fun playEvent(player: EntityPlayer?, type: Int, pos: BlockPos, data: Int) =
        world.get()!!.playEvent(player, type, pos, data)

    override fun getActualHeight(): Int = world.get()!!.actualHeight

    override fun setRandomSeed(seedX: Int, seedY: Int, seedZ: Int): Random = world.get()!!.setRandomSeed(seedX, seedY, seedZ)

    override fun addWorldInfoToCrashReport(report: CrashReport): CrashReportCategory =
        world.get()!!.addWorldInfoToCrashReport(report)

    override fun getHorizon(): Double = world.get()!!.horizon

    override fun sendBlockBreakProgress(breakerId: Int, pos: BlockPos, progress: Int) =
        world.get()!!.sendBlockBreakProgress(breakerId, pos, progress)

    override fun getCurrentDate(): Calendar = world.get()!!.currentDate

    override fun makeFireworks(
        x: Double,
        y: Double,
        z: Double,
        motionX: Double,
        motionY: Double,
        motionZ: Double,
        compound: NBTTagCompound?
    ) = world.get()!!.makeFireworks(x, y, z, motionX, motionY, motionZ, compound)

    override fun getScoreboard(): Scoreboard = world.get()!!.scoreboard

    override fun updateComparatorOutputLevel(pos: BlockPos, blockIn: Block) =
        world.get()!!.updateComparatorOutputLevel(pos, blockIn)

    override fun getDifficultyForLocation(pos: BlockPos): DifficultyInstance = world.get()!!.getDifficultyForLocation(pos)

    override fun getDifficulty(): EnumDifficulty = world.get()!!.difficulty

    override fun getSkylightSubtracted(): Int = world.get()!!.skylightSubtracted

    override fun setSkylightSubtracted(newSkylightSubtracted: Int) {
        world.get()!!.skylightSubtracted = newSkylightSubtracted
    }

    override fun getLastLightningBolt(): Int = world.get()!!.lastLightningBolt

    override fun setLastLightningBolt(lastLightningBoltIn: Int) {
        world.get()!!.lastLightningBolt = lastLightningBoltIn
    }

    override fun getVillageCollection(): VillageCollection = world.get()!!.getVillageCollection()

    override fun getWorldBorder(): WorldBorder = world.get()!!.worldBorder

    override fun isSpawnChunk(x: Int, z: Int): Boolean = world.get()!!.isSpawnChunk(x, z)

    override fun getPersistentChunks(): ImmutableSetMultimap<ChunkPos, ForgeChunkManager.Ticket> =
        world.get()!!.persistentChunks

    override fun getPersistentChunkIterable(chunkIterator: MutableIterator<Chunk>): MutableIterator<Chunk> =
        world.get()!!.getPersistentChunkIterable(chunkIterator)

    override fun getBlockLightOpacity(pos: BlockPos): Int = world.get()!!.getBlockLightOpacity(pos)

    override fun markTileEntitiesInChunkForRemoval(chunk: Chunk) = world.get()!!.markTileEntitiesInChunkForRemoval(chunk)

    override fun initCapabilities() {}

    override fun getPerWorldStorage(): MapStorage = world.get()!!.perWorldStorage

    override fun sendPacketToServer(packetIn: Packet<*>) = world.get()!!.sendPacketToServer(packetIn)

    override fun getLootTableManager(): LootTableManager = world.get()!!.lootTableManager

    override fun findNearestStructure(structureName: String, position: BlockPos, findUnexplored: Boolean): BlockPos? =
        world.get()!!.findNearestStructure(structureName, position, findUnexplored)
}