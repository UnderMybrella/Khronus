package dev.brella.khronus.examples.item

import dev.brella.khronus.api.EnumLagTuning
import dev.brella.khronus.api.ILagTester
import dev.brella.khronus.api.KhronusApi
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.EnumHelper

class ItemLagometerGoggles : ItemArmor(LAGOMETER_MATERIAL, 0, EntityEquipmentSlot.HEAD), IModItem {
    companion object {
        val LAGOMETER_MATERIAL = EnumHelper.addArmorMaterial(
            "lagometer",
            "lagometer",
            -1,
            intArrayOf(0, 0, 0, 0),
            1,
            ArmorMaterial.LEATHER.soundEvent,
            0f
        ) ?: ArmorMaterial.CHAIN

        val lagTuning: ILagTester =
            ILagTester { _, player, slot ->
                if (slot == EntityEquipmentSlot.HEAD) {
                    if (player.isSneaking) EnumLagTuning.XRAY else EnumLagTuning.BASIC
                } else EnumLagTuning.OFF
            }

        val provider: ICapabilityProvider = object : ICapabilityProvider {
            override fun <T : Any?> getCapability(capability: Capability<T>, facing: EnumFacing?): T? =
                if (capability == KhronusApi.LAG_TESTING) lagTuning as T else null

            override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
                capability == KhronusApi.LAG_TESTING
        }
    }

    override val name: String = "lagometer_goggles"

    override fun initCapabilities(stack: ItemStack, nbt: NBTTagCompound?): ICapabilityProvider? =
        provider

    init {
        initialise()
    }
}