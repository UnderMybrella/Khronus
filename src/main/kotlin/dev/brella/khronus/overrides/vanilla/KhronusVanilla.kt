package dev.brella.khronus.overrides.vanilla

import dev.brella.khronus.Khronus
import dev.brella.khronus.Khronus.MOD_ID
import dev.brella.khronus.api.IKhronusTickable
import dev.brella.khronus.api.KhronusApi
import dev.brella.khronus.overrides.IKhronusOverrides
import dev.brella.khronus.overrides.KhronusOverrides
import dev.brella.khronus.overrides.vanilla.te.KhronusTickableFurnace
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityFurnace
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.common.capabilities.CapabilityDispatcher
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KhronusVanilla: IKhronusOverrides {
//    val blockFurnace =
//        BlockKhronusFurnace(false)
//            .setHardness(3.5F)
//            .setSoundType(SoundType.STONE)
//            .setTranslationKey("furnace")
//            .setCreativeTab(CreativeTabs.DECORATIONS)
//            .setRegistryName(ResourceLocation("furnace"))
//
//    val blockFurnaceLit = BlockKhronusFurnace(true)
//        .setHardness(3.5F)
//        .setSoundType(SoundType.STONE)
//        .setLightLevel(0.875F)
//        .setTranslationKey("furnace")
//        .setRegistryName(ResourceLocation("lit_furnace"))

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun registerBlocks(event: RegistryEvent.Register<Block>) {
//        val existingFurnace = event.registry.getValue(blockFurnace.registryName)
//        if (existingFurnace == null || existingFurnace::class.java == BlockFurnace::class.java) {
//            event.registry.register(blockFurnace)
//            event.registry.register(blockFurnaceLit)
//
//            println(existingFurnace === Blocks.FURNACE)
//        }
//
//        GameRegistry.registerTileEntity(TileEntityKhronusFurnace::class.java, ResourceLocation(Khronus.MOD_ID, "khronus_furnace"))
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

    override val key: ResourceLocation = ResourceLocation(MOD_ID, "vanilla")

    @Suppress("UNCHECKED_CAST")
    override fun <T : TileEntity> capabilityForTileEntity(tileEntity: T): IKhronusTickable<T>? =
        when (tileEntity) {
            is TileEntityFurnace -> KhronusTickableFurnace(tileEntity) as IKhronusTickable<T>
            else -> null
        }
}