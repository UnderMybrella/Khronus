package dev.brella.khronus.examples.item

import dev.brella.khronus.Khronus
import dev.brella.khronus.TickDog.abs
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.text.*
import net.minecraft.world.World

class LagListItem(properties: Item.Properties) : Item(properties) {
    override fun addInformation(
        stack: ItemStack,
        worldIn: World?,
        tooltip: MutableList<ITextComponent>,
        flagIn: ITooltipFlag,
    ) {
        super.addInformation(stack, worldIn, tooltip, flagIn)

        if (worldIn == null) {
            tooltip.add(StringTextComponent("Something seems to be interfering with the lag list").modifyStyle { style ->
                style.setItalic(true)
            })

            return
        }

        Khronus.clientProxy?.let { clientProxy ->
            if (worldIn.dimensionKey.location != clientProxy.tickTimesDimension) {
                tooltip.add(StringTextComponent("Something's wrong, it seems that the map points to positions from another world (${clientProxy.tickTimesDimension})").modifyStyle { style ->
                    style.setItalic(true)
                })
            }

            clientProxy.laggiest.forEachIndexed { index, (pos, time) ->
                clientProxy.blockPos.setPos(pos)
                tooltip.add(StringTextComponent("${index + 1}) ").modifyStyle { style -> style.setBold(true) }
                    .append(worldIn.getBlockState(clientProxy.blockPos).block.translatedName.modifyStyle { style ->
                        style.setBold(false)
                    }.append(StringTextComponent(" (${time} μs)").modifyStyle { style ->
                        style.setColor(Color.fromInt(TextFormatting.DARK_PURPLE.color!!))
                    })))
            }
        }
    }
}