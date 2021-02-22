package dev.brella.khronus.examples.block

import dev.brella.khronus.Khronus
import dev.brella.khronus.Khronus.MOD_ID
import net.minecraft.block.Block
import net.minecraft.block.BlockFurnace
import net.minecraft.block.SoundType
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.IForgeRegistry

object KhronusBlocks {
    val lavaFurnace = BlockLavaFurnace(false)
        .setHardness(3.5f)
        .setSoundType(SoundType.STONE)
        .setCreativeTab(Khronus.creativeTab)

    val litLavaFurnace = BlockLavaFurnace(true)
        .setHardness(3.5f)
        .setSoundType(SoundType.STONE)
        .setLightLevel(0.875f)

    val warpDrive = BlockWarpDrive()
        .setHardness(99f)
        .setSoundType(SoundType.GLASS)

    val counter = BlockCounter()
        .setHardness(1f)
        .setSoundType(SoundType.GLASS)

    val blocks = mapOf(
        lavaFurnace to "lava_furnace",
        litLavaFurnace to "lit_lava_furnace",
        warpDrive to "warp_drive",
        counter to "counter"
    )

    fun register(registry: IForgeRegistry<Block>) {
        blocks.forEach { (block, name) ->
            registry.register(block.setRegistryName(ResourceLocation(MOD_ID, name)).setTranslationKey("$MOD_ID.$name"))
        }
    }

    fun registerItemBlocks(registry: IForgeRegistry<Item>) {
        blocks.forEach { (block, name) ->
            registry.register(ItemBlock(block).setRegistryName(MOD_ID, name).setTranslationKey("$MOD_ID.$name"))
        }
    }

    fun registerModels() {
        blocks.forEach { (block, name) ->
            Khronus.proxy.registerItemRenderer(Item.getItemFromBlock(block), 0, name)
        }
    }
}