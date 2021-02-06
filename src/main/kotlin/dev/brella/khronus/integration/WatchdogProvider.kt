package dev.brella.khronus.integration

import dev.brella.khronus.api.IKhronusTickable
import dev.brella.khronus.microsecondApproxToFloat
import dev.brella.khronus.toTwoDecimalPlaces
import dev.brella.khronus.watchdogs.khronusTickableTileEntities
import dev.brella.khronus.watchdogs.tickLength
import mcp.mobius.waila.api.IComponentProvider
import mcp.mobius.waila.api.IDataAccessor
import mcp.mobius.waila.api.IPluginConfig
import mcp.mobius.waila.api.IServerDataProvider
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World

object WatchdogProvider : IServerDataProvider<TileEntity>, IComponentProvider {
    override fun appendBody(tooltip: MutableList<ITextComponent>, accessor: IDataAccessor, config: IPluginConfig?) {
        if (accessor.serverData.contains("khronus_rate")) {
            tooltip.add(StringTextComponent("${TextFormatting.RED}${TextFormatting.ITALIC}Ticks once every ${accessor.serverData.getInt("khronus_rate")} ticks"))
        } else {
            accessor.tileEntity?.let { it.world?.khronusTickableTileEntities?.get(it) }?.let { tickRate ->
                tooltip.add(StringTextComponent("${TextFormatting.LIGHT_PURPLE}${TextFormatting.ITALIC}Ticks once every ${tickRate.tickRate} ticks"))
            }
        }

        if (accessor.serverData.contains("khronus_length")) {
            tooltip.add(StringTextComponent("${TextFormatting.RED}${TextFormatting.ITALIC}Took ${
                accessor.serverData.getLong("khronus_length").microsecondApproxToFloat().toTwoDecimalPlaces()
            } μs last tick"))
        } else {
            accessor.tileEntity?.let { it.world?.tickLength?.get(it) }?.let { tickLength ->
                tooltip.add(StringTextComponent("${TextFormatting.LIGHT_PURPLE}${TextFormatting.ITALIC}Took ${tickLength.microsecondApproxToFloat()} μs last tick"))
            }
        }
    }

    override fun appendTail(tooltip: MutableList<ITextComponent>, accessor: IDataAccessor, config: IPluginConfig?) {
        if (accessor.tileEntity is IKhronusTickable) tooltip.add(StringTextComponent("${TextFormatting.BLUE}${TextFormatting.ITALIC}Supports Khronus Ticks"))
    }

    override fun appendServerData(tag: CompoundNBT, player: ServerPlayerEntity, world: World, te: TileEntity) {
        world.khronusTickableTileEntities[te]?.let { tag.putInt("khronus_rate", it.tickRate) }
        world.tickLength[te]?.let { tag.putLong("khronus_length", it) }
    }
}