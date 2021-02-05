package dev.brella.khronus.overrides.vanilla

import dev.brella.khronus.Khronus
import dev.brella.khronus.examples.block.KhronusBlocks
import dev.brella.khronus.overrides.vanilla.blocks.BlockKhronusFurnace
import dev.brella.khronus.overrides.vanilla.te.TileEntityKhronusFurnace
import net.minecraft.block.Block
import net.minecraft.block.BlockFurnace
import net.minecraft.block.SoundType
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.GameRegistry


object KhronusVanilla {
    val blockFurnace =
        BlockKhronusFurnace(false)
            .setHardness(3.5F)
            .setSoundType(SoundType.STONE)
            .setTranslationKey("furnace")
            .setCreativeTab(CreativeTabs.DECORATIONS)
            .setRegistryName(ResourceLocation("furnace"))

    val blockFurnaceLit = BlockKhronusFurnace(true)
        .setHardness(3.5F)
        .setSoundType(SoundType.STONE)
        .setLightLevel(0.875F)
        .setTranslationKey("furnace")
        .setRegistryName(ResourceLocation("lit_furnace"))

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun registerBlocks(event: RegistryEvent.Register<Block>) {
        val existingFurnace = event.registry.getValue(blockFurnace.registryName)
        if (existingFurnace == null || existingFurnace::class.java == BlockFurnace::class.java) {
            event.registry.register(blockFurnace)
            event.registry.register(blockFurnaceLit)

            println(existingFurnace === Blocks.FURNACE)
        }

        GameRegistry.registerTileEntity(TileEntityKhronusFurnace::class.java, ResourceLocation(Khronus.MOD_ID, "khronus_furnace"))
    }

    @SubscribeEvent
    fun registerItems(event: RegistryEvent.Register<Item>) {
//        KhronusBlocks.registerItemBlocks(event.registry)
//        SnugItems.register(event.registry)
    }

    @SubscribeEvent
    fun registerModels(event: ModelRegistryEvent) {
//        KhronusBlocks.registerModels()
//        SnugItems.registerModels()
    }
}