package dev.brella.khronus.examples.item

import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.ItemArmor

class ItemLagometerGoggles: ItemArmor(ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.HEAD), IModItem, ILagometer {
    override val name: String = "lagometer_goggles"

    init {
        initialise()
    }
}