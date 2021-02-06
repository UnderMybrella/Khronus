//package dev.brella.khronus.integration
//
//import dev.brella.khronus.*
//import dev.brella.khronus.api.IKhronusTickable
//import dev.brella.khronus.watchdogs.KhronusWorld
//import dev.brella.khronus.watchdogs.khronusTickableTileEntities
//import dev.brella.khronus.watchdogs.tickLength
//import mcp.mobius.waila.api.IWailaConfigHandler
//import mcp.mobius.waila.api.IWailaDataAccessor
//import mcp.mobius.waila.api.IWailaDataProvider
//import net.minecraft.entity.player.EntityPlayerMP
//import net.minecraft.item.ItemStack
//import net.minecraft.nbt.NBTTagCompound
//import net.minecraft.tileentity.TileEntity
//import net.minecraft.util.math.BlockPos
//import net.minecraft.util.text.TextFormatting
//import net.minecraft.world.World
//
//object WatchdogProvider : IWailaDataProvider {
//    override fun getWailaBody(
//        itemStack: ItemStack,
//        tooltip: MutableList<String>,
//        accessor: IWailaDataAccessor,
//        config: IWailaConfigHandler
//    ): MutableList<String> {
//        if (accessor.nbtData.hasKey("khronus_rate")) {
//            tooltip.add("${TextFormatting.RED}${TextFormatting.ITALIC}Ticks once every ${accessor.nbtData.getInteger("khronus_rate")} ticks")
//        } else {
//            accessor.tileEntity?.let { it.world.khronusTickableTileEntities[it] }?.let { tickRate ->
//                tooltip.add("${TextFormatting.LIGHT_PURPLE}${TextFormatting.ITALIC}Ticks once every ${tickRate.tickRate} ticks")
//            }
//        }
//
//        if (accessor.nbtData.hasKey("khronus_length")) {
//            tooltip.add("${TextFormatting.RED}${TextFormatting.ITALIC}Took ${
//                accessor.nbtData.getLong("khronus_length").microsecondApproxToFloat().toTwoDecimalPlaces()
//            } μs last tick")
//        } else {
//            accessor.tileEntity?.let { it.world.tickLength[it] }?.let { tickLength ->
//                tooltip.add("${TextFormatting.LIGHT_PURPLE}${TextFormatting.ITALIC}Took ${tickLength.microsecondApproxToFloat()} μs last tick")
//            }
//        }
//
//        return tooltip
//    }
//
//    override fun getWailaTail(
//        itemStack: ItemStack,
//        tooltip: MutableList<String>,
//        accessor: IWailaDataAccessor,
//        config: IWailaConfigHandler
//    ): MutableList<String> {
//        if (accessor.tileEntity is IKhronusTickable) tooltip.add("${TextFormatting.BLUE}${TextFormatting.ITALIC}Supports Khronus Ticks")
//
//        return tooltip
//    }
//
//    override fun getNBTData(
//        player: EntityPlayerMP,
//        te: TileEntity,
//        tag: NBTTagCompound,
//        world: World,
//        pos: BlockPos
//    ): NBTTagCompound {
//        world.khronusTickableTileEntities[te]?.let { tag.setInteger("khronus_rate", it.tickRate) }
//        world.tickLength[te]?.let { tag.setLong("khronus_length", it) }
//
//        return tag
//    }
//}