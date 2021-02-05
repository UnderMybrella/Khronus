package dev.brella.khronus

import dev.brella.khronus.commands.BasicChildCommand
import dev.brella.khronus.commands.BasicChildCommandWithCompletion
import dev.brella.khronus.commands.PipeCommand
import net.minecraft.block.state.IBlockState
import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.profiler.Profiler
import net.minecraft.server.MinecraftServer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.EnumSkyBlock
import net.minecraft.world.World
import net.minecraft.world.WorldType
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.storage.ExtendedBlockStorage
import net.minecraftforge.common.util.BlockSnapshot
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper
import net.minecraftforge.server.command.CommandTreeBase

/*fun World.setBlockState(pos: BlockPos, newState: IBlockState, flags: Int, configureTileEntity: (tileEntity: TileEntity) -> Unit): Boolean {
    var pos = pos
    if (this.isOutsideBuildHeight(pos)) {
        return false
    } else if (!this.isRemote && this.worldInfo.terrainType === WorldType.DEBUG_ALL_BLOCK_STATES) {
        return false
    } else {
        val chunk: Chunk = this.getChunk(pos)
        pos = pos.toImmutable() // Forge - prevent mutable BlockPos leaks
        var blockSnapshot: BlockSnapshot? = null
        if (this.captureBlockSnapshots && !this.isRemote) {
            blockSnapshot = BlockSnapshot.getBlockSnapshot(this, pos, flags)
            this.capturedBlockSnapshots.add(blockSnapshot)
        }
        val oldState: IBlockState = getBlockState(pos)
        val oldLight = oldState.getLightValue(this, pos)
        val oldOpacity = oldState.getLightOpacity(this, pos)
        val blockState = chunk.setBlockState(pos, newState, configureTileEntity)
        if (blockState == null) {
            if (blockSnapshot != null) this.capturedBlockSnapshots.remove(blockSnapshot)
            return false
        } else {
            if (newState.getLightOpacity(this, pos) != oldOpacity || newState.getLightValue(this, pos) != oldLight) {
                this.profiler.startSection("checkLight")
                this.checkLight(pos)
                this.profiler.endSection()
            }
            if (blockSnapshot == null) // Don't notify clients or update physics while capturing blockstates
            {
                this.markAndNotifyBlock(pos, chunk, blockState, newState, flags)
            }
            return true
        }
    }
}

fun Chunk.setBlockState(pos: BlockPos, state: IBlockState, configureTileEntity: (tileEntity: TileEntity) -> Unit): IBlockState? {
    val i = pos.x and 15
    val j = pos.y
    val k = pos.z and 15
    val l = k shl 4 or i
    if (j >= this.precipitationHeightMap[l] - 1) {
        this.precipitationHeightMap[l] = -999
    }
    val i1: Int = this.heightMap[l]
    val blockState: IBlockState = this.getBlockState(pos)
    return if (blockState === state) {
        null
    } else {
        val block = state.block
        val block1 = blockState.block
        val k1 = blockState.getLightOpacity(this.world, pos) // Relocate old light value lookup here, so that it is called before TE is removed.
        var extendedblockstorage: ExtendedBlockStorage = storageArrays[j shr 4]
        var flag = false
        if (extendedblockstorage === Chunk.NULL_BLOCK_STORAGE) {
            if (block === Blocks.AIR) {
                return null
            }
            extendedblockstorage = ExtendedBlockStorage(j shr 4 shl 4, this.world.provider.hasSkyLight())
            storageArrays[j shr 4] = extendedblockstorage
            flag = j >= i1
        }
        extendedblockstorage[i, j and 15, k] = state

        //if (block1 != block)
        run {
            if (!this.world.isRemote) {
                if (block1 !== block) //Only fire block breaks when the block changes.
                    block1.breakBlock(this.world, pos, blockState)
                val te: TileEntity? = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK)
                if (te != null && te.shouldRefresh(this.world, pos, blockState, state)) this.world.removeTileEntity(pos)
            } else if (block1.hasTileEntity(blockState)) {
                val te: TileEntity? = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK)
                if (te != null && te.shouldRefresh(this.world, pos, blockState, state)) this.world.removeTileEntity(pos)
            }
        }
        if (extendedblockstorage[i, j and 15, k].block !== block) {
            null
        } else {
            // If capturing blocks, only run block physics for TE's. Non-TE's are handled in ForgeHooks.onPlaceItemIntoWorld
            if (!this.world.isRemote && block1 !== block && (!this.world.captureBlockSnapshots || block.hasTileEntity(state))) {
                block.onBlockAdded(this.world, pos, state)
            }

            if (block.hasTileEntity(state)) {
                var newTileEntity: TileEntity? = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK)
                if (newTileEntity == null) {
                    newTileEntity = block.createTileEntity(this.world, state)?.also(configureTileEntity)
                    this.world.setTileEntity(pos, newTileEntity)
                } else {
                    configureTileEntity(newTileEntity)
                }

                newTileEntity?.updateContainingBlockInfo()
            }

            //This is moved to after setting the tile entity, so that we can properly check lighting
            if (flag) {
                generateSkylightMap()
            } else {
                val j1 = state.getLightOpacity(this.world, pos)
                if (j1 > 0) {
                    if (j >= i1) {
                        relightBlock(i, j + 1, k)
                    }
                } else if (j == i1 - 1) {
                    this.relightBlock(i, j, k)
                }
                if (j1 != k1 && (j1 < k1 || this.getLightFor(EnumSkyBlock.SKY, pos) > 0 || this.getLightFor(EnumSkyBlock.BLOCK, pos) > 0)) {
                    this.propagateSkylightOcclusion(i, k)
                }
            }

            this.dirty = true
            blockState
        }
    }
}*/

fun ItemStack.equals(other: ItemStack, ignoreStackSize: Boolean): Boolean {
    return if (!ignoreStackSize && count != other.count) {
        false
    } else if (item !== other.item) {
        false
    } else if (itemDamage != other.itemDamage) {
        false
    } else if (tagCompound == null && other.tagCompound != null) {
        false
    } else {
        (tagCompound == null || tagCompound == other.tagCompound) && areCapsCompatible(other)
    }
}

/**
 * Sends this message to everyone tracking a point.
 * The [IMessageHandler] for this message type should be on the CLIENT side.
 * The `range` field of the [TargetPoint] is ignored.
 *
 * @param message The message to send
 * @param point The tracked [TargetPoint] around which to send
 */
inline fun SimpleNetworkWrapper.sendToAllTracking(message: IMessage?, entity: TileEntity) =
    sendToAllTracking(message, entity.pos.toTargetPoint(entity.world.provider.dimension, 1.0))

inline fun BlockPos.toTargetPoint(dimension: Int, range: Double): NetworkRegistry.TargetPoint =
    NetworkRegistry.TargetPoint(dimension, x.toDouble(), y.toDouble(), z.toDouble(), range)

const val NUM_X_BITS = 26
const val NUM_Z_BITS = NUM_X_BITS
const val NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS
const val Y_SHIFT = 0 + NUM_Z_BITS
const val X_SHIFT = Y_SHIFT + NUM_Y_BITS
const val X_MASK = (1L shl NUM_X_BITS) - 1L
const val Y_MASK = (1L shl NUM_Y_BITS) - 1L
const val Z_MASK = (1L shl NUM_Z_BITS) - 1L

inline fun BlockPos.MutableBlockPos.setFromLong(long: Long): BlockPos.MutableBlockPos {
    val x =
        (long shl 64 - X_SHIFT - NUM_X_BITS shr 64 - NUM_X_BITS).toInt()
    val y =
        (long shl 64 - Y_SHIFT - NUM_Y_BITS shr 64 - NUM_Y_BITS).toInt()
    val z = (long shl 64 - NUM_Z_BITS shr 64 - NUM_Z_BITS).toInt()

    return setPos(x, y, z)
}

inline fun buildPipeCommand(name: String, block: PipeCommand.() -> Unit) =
    PipeCommand(name).apply(block)

inline fun FMLServerStartingEvent.registerPipeCommand(name: String, block: PipeCommand.() -> Unit) =
    registerServerCommand(buildPipeCommand(name, block))

inline fun <T : CommandTreeBase> T.addPipe(name: String, block: PipeCommand.() -> Unit): T {
    addSubcommand(PipeCommand(name).apply(block))
    return this
}

inline fun <T : CommandTreeBase> T.addCommand(cmd: ICommand): T {
    addSubcommand(cmd)
    return this
}

inline fun <T : CommandTreeBase> T.addCommand(
    name: String,
    noinline execute: (server: MinecraftServer, sender: ICommandSender, args: Array<String>) -> Unit
): T {
    addSubcommand(BasicChildCommand(name, execute))
    return this
}

inline fun <T : CommandTreeBase> T.addCommand(
    name: String,
    noinline tabCompletion: (
        server: MinecraftServer,
        sender: ICommandSender,
        args: Array<String>,
        targetPos: BlockPos?
    ) -> MutableList<String>,
    noinline execute: (server: MinecraftServer, sender: ICommandSender, args: Array<String>) -> Unit
): T {
    addSubcommand(BasicChildCommandWithCompletion(name, tabCompletion, execute))
    return this
}

inline fun <T : CommandTreeBase> T.addCommand(
    name: String,
    values: List<String>,
    noinline execute: (server: MinecraftServer, sender: ICommandSender, args: Array<String>) -> Unit
): T {
    addSubcommand(BasicChildCommandWithCompletion(name, { _, _, args, _ ->
        args.last().let { existing ->
            values.mapNotNullTo(ArrayList()) { name ->
                name.takeIf {
                    existing.isBlank() ||
                            it.startsWith(existing, true)
                }
            }
        }
    }, execute))
    return this
}

inline fun <T : CommandTreeBase> T.addCommand(
    name: String,
    crossinline values: () -> List<String>,
    noinline execute: (server: MinecraftServer, sender: ICommandSender, args: Array<String>) -> Unit
): T {
    addSubcommand(BasicChildCommandWithCompletion(name, { _, _, args, _ ->
        args.last().let { existing ->
            values().mapNotNullTo(ArrayList()) { name ->
                name.takeIf {
                    existing.isBlank() ||
                            it.startsWith(existing, true)
                }
            }
        }
    }, execute))
    return this
}

/**
 *
 */

inline fun <R> Profiler.section(name: String, block: () -> R): R =
    try {
        startSection(name)
        block()
    } finally {
        endSection()
    }

inline fun <R> Profiler.section(noinline name: () -> String, block: () -> R): R =
    try {
        func_194340_a(name)

        block()
    } finally {
        endSection()
    }

inline fun TileEntity.distinctHashCode(): Int =
    pos.hashCode() * 92821 + world.hashCode()

inline operator fun <V> Map<Int, V>.get(te: TileEntity): V? =
    get(te.distinctHashCode())

inline operator fun <V> MutableMap<Int, V>.set(te: TileEntity, value: V): V? =
    put(te.distinctHashCode(), value)

inline fun <V> MutableMap<Int, V>.remove(te: TileEntity): V? =
    remove(te.distinctHashCode())