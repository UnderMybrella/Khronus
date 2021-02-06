package dev.brella.khronus.examples.item

import dev.brella.khronus.Khronus
import net.minecraft.item.Item
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.IForgeRegistry
import thedarkcolour.kotlinforforge.forge.KDeferredRegister
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.MOD_CONTEXT

object KhronusItems {
    private val ITEMS = KDeferredRegister(ForgeRegistries.ITEMS, Khronus.MOD_ID)

    val lagometerGoggles = ITEMS.registerObject("lagometer_goggles") {
        LagometerGogglesItem(Item.Properties().group(Khronus.itemGroup))
    }

    fun register() {
        ITEMS.register(MOD_BUS)
    }
}