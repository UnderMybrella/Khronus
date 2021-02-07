package dev.brella.khronus.integration.waila

import dev.brella.khronus.examples.entity.CounterTileEntity
import mcp.mobius.waila.api.IComponentProvider
import mcp.mobius.waila.api.IDataAccessor
import mcp.mobius.waila.api.IPluginConfig
import mcp.mobius.waila.api.IServerDataProvider
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.world.World
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

object CounterDataProvider : IComponentProvider, IServerDataProvider<TileEntity> {
        val CLICK_FORMAT = NumberFormat.getIntegerInstance()
        val FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)

    @ExperimentalTime
    override fun appendBody(tooltip: MutableList<ITextComponent>, accessor: IDataAccessor, config: IPluginConfig?) {
        val nbt = accessor.serverData.getCompound("counter")

        val startupTime = nbt.getLong("startup_time")
        val counter = nbt.getLong("counter")

        tooltip.add(StringTextComponent("Clicked ${CLICK_FORMAT.format(counter)} times"))
        tooltip.add(StringTextComponent("Born on ${Instant.ofEpochMilli(startupTime).atZone(ZoneId.systemDefault()).format(
            FORMATTER)}"))
        tooltip.add(StringTextComponent("The clock strikes ${Instant.ofEpochMilli(startupTime + (counter * 50)).atZone(ZoneId.systemDefault()).format(
            FORMATTER)}"))

        tooltip.add(StringTextComponent("Click time is ${(counter * 50).milliseconds}"))
    }

    override fun appendServerData(tag: CompoundNBT, player: ServerPlayerEntity, world: World, te: TileEntity) {
        (te as? CounterTileEntity)?.write(tag.put("counter", CompoundNBT()).let { tag.getCompound("counter") })
    }
}