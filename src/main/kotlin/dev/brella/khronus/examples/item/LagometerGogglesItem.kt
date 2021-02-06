package dev.brella.khronus.examples.item

import net.minecraft.inventory.EquipmentSlotType
import net.minecraft.item.ArmorItem
import net.minecraft.item.ArmorMaterial

class LagometerGogglesItem(properties: Properties): ArmorItem(ArmorMaterial.LEATHER, EquipmentSlotType.HEAD, properties), ILagometer