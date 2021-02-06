//package dev.brella.khronus.integration
//
//import dev.brella.khronus.examples.entity.CounterTileEntity
//import mcp.mobius.waila.api.IWailaConfigHandler
//import mcp.mobius.waila.api.IWailaDataAccessor
//import mcp.mobius.waila.api.IWailaDataProvider
//import net.minecraft.entity.player.EntityPlayerMP
//import net.minecraft.item.ItemStack
//import net.minecraft.nbt.NBTTagCompound
//import net.minecraft.tileentity.TileEntity
//import net.minecraft.util.math.BlockPos
//import net.minecraft.world.World
//import java.text.NumberFormat
//import java.time.Instant
//import java.time.ZoneId
//import java.time.format.DateTimeFormatter
//import java.time.format.FormatStyle
//import kotlin.time.ExperimentalTime
//
//class CounterDataProvider : IWailaDataProvider {
//    companion object {
//        val CLICK_FORMAT = NumberFormat.getIntegerInstance()
//        val FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
//    }
//
//    @UseExperimental(ExperimentalTime::class)
//    override fun getWailaBody(itemStack: ItemStack, tooltip: MutableList<String>, accessor: IWailaDataAccessor, config: IWailaConfigHandler): MutableList<String> {
//        val nbt = accessor.nbtData
//
//        val startupTime = nbt.getLong("startup_time")
//        val counter = nbt.getLong("counter")
//
//        tooltip.add("Clicked ${CLICK_FORMAT.format(counter)} times")
//        tooltip.add("Born on ${Instant.ofEpochMilli(startupTime).atZone(ZoneId.systemDefault()).format(FORMATTER)}")
//        tooltip.add("The clock strikes ${Instant.ofEpochMilli(startupTime + (counter * 50)).atZone(ZoneId.systemDefault()).format(FORMATTER)}")
//
//        tooltip.add("Click time is ${(counter * 50).milliseconds}")
//
//        return tooltip
//    }
//
//    override fun getNBTData(player: EntityPlayerMP, te: TileEntity?, tag: NBTTagCompound?, world: World, pos: BlockPos): NBTTagCompound {
//        val compound = tag ?: NBTTagCompound()
//
//        (te as? CounterTileEntity)?.writeToNBT(compound)
//
//        return compound
//    }
//}