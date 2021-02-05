package dev.brella.khronus.examples.item

import dev.brella.khronus.Khronus.MOD_ID
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.IItemPropertyGetter
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World

internal interface IModItem {
    val name: String

    fun Item.initialise() {
        translationKey = "${MOD_ID}.$name"
        setRegistryName(MOD_ID, name)
    }
}