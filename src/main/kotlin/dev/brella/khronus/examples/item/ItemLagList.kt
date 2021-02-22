package dev.brella.khronus.examples.item

import dev.brella.khronus.Khronus
import dev.brella.khronus.proxy.ClientProxy
import dev.brella.khronus.setFromLong
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import java.lang.Integer.min

class ItemLagList(override val name: String) : Item(), IModItem {
    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        super.addInformation(stack, worldIn, tooltip, flagIn)

        if (worldIn == null) {
            tooltip.add("${TextFormatting.ITALIC}Something seems to be interfering with the lag list${TextFormatting.RESET}")

            return
        }

        (Khronus.proxy as? ClientProxy)?.let { clientProxy ->
            if (worldIn.provider.dimension != clientProxy.tickTimesDimension) {
                tooltip.add("${TextFormatting.ITALIC}Something's wrong, it seems that the map points to positions from another world (${clientProxy.tickTimesDimension})${TextFormatting.RESET}")
            }

            for (index in 0 until min(clientProxy.lagList.size, 20)) {
                val data = if (index < clientProxy.lagList.size) clientProxy.lagList[index] else break
                clientProxy.blockPos.setPos(data.posX, data.posY, data.posZ)

                tooltip.add("${TextFormatting.BOLD}${index + 1})${TextFormatting.RESET} ${worldIn.getBlockState(clientProxy.blockPos).block.localizedName}  (${TextFormatting.DARK_PURPLE}${data.timing} ${if (data.usingMilliseconds) "ms" else "μs"}${TextFormatting.RESET}) [${TextFormatting.GOLD}${data.posX},${data.posY},${data.posZ}${TextFormatting.RESET}]")
            }
        }
    }

    init {
        initialise()
    }
}