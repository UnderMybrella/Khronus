package dev.brella.khronus.examples.item

import dev.brella.khronus.Khronus
import net.minecraft.item.Item
import net.minecraftforge.registries.IForgeRegistry

object KhronusItems {
    val lagometerGoggles = ItemLagometerGoggles()
    val lagList = ItemLagList("lag_list")

    val items: Array<Item> = arrayOf(lagometerGoggles, lagList)

    fun register(registry: IForgeRegistry<Item>) {
        items.forEach(registry::register)
    }

    fun registerModels() {
        items.forEach { item -> Khronus.proxy.registerItemRenderer(item, 0, (item as IModItem).name) }
//        Minegate.proxy.registerItemRenderer(debugZeus, 0, debugZeus.name)
    }
}