package dev.brella.khronus.examples.block

import dev.brella.khronus.Khronus
import dev.brella.khronus.Khronus.MOD_ID
import dev.brella.khronus.examples.entity.LavaFurnaceTileEntity
import dev.brella.khronus.examples.entity.CounterTileEntity
import dev.brella.khronus.examples.entity.WarpDriveTileEntity
import net.minecraft.block.AbstractBlock
import net.minecraft.block.material.Material
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.state.properties.BlockStateProperties
import net.minecraft.tileentity.TileEntityType
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import thedarkcolour.kotlinforforge.forge.KDeferredRegister
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.MOD_CONTEXT

object KhronusBlocks {
    private val BLOCKS = KDeferredRegister(ForgeRegistries.BLOCKS, MOD_ID)
    private val ITEMS = KDeferredRegister(ForgeRegistries.ITEMS, MOD_ID)
    private val TILES = KDeferredRegister(ForgeRegistries.TILE_ENTITIES, MOD_ID)

    val lavaFurnace = BLOCKS.registerObject("lava_furnace") {
        BlockLavaFurnace(AbstractBlock.Properties.create(Material.ROCK)
            .setRequiresTool()
            .hardnessAndResistance(3.5f)
            .setLightLevel { state -> if (state.get(BlockStateProperties.LIT)) 13 else 0 }
        )
    }

    val lavaFurnaceItem = ITEMS.registerObject("lava_furnace") {
        BlockItem(lavaFurnace.get(), Item.Properties().group(Khronus.itemGroup))
    }

    val lavaFurnaceTile = TILES.registerObject("lava_furnace") {
        TileEntityType.Builder.create({ LavaFurnaceTileEntity() }, lavaFurnace.get()).build(null)
    }

    val counter = BLOCKS.registerObject("counter") {
        CounterBlock(AbstractBlock.Properties.create(Material.GLASS))
    }

    val counterItem = ITEMS.registerObject("counter") {
        BlockItem(counter.get(), Item.Properties().group(Khronus.itemGroup))
    }

    val counterTile = TILES.registerObject("counter") {
        TileEntityType.Builder.create({ CounterTileEntity() }, counter.get()).build(null)
    }

    val warpDrive = BLOCKS.registerObject("warp_drive") {
        WarpDriveBlock(AbstractBlock.Properties.create(Material.PORTAL).setBlocksVision { p_test_1_, p_test_2_, p_test_3_ -> false })
    }

    val warpDriveItem = ITEMS.registerObject("warp_drive") {
        BlockItem(warpDrive.get(), Item.Properties().group(Khronus.itemGroup))
    }

    val warpDriveTile = TILES.registerObject("warp_drive") {
        TileEntityType.Builder.create({ WarpDriveTileEntity() }, warpDrive.get()).build(null)
    }

    fun register() {
        BLOCKS.register(MOD_BUS)
        ITEMS.register(MOD_BUS)
        TILES.register(MOD_BUS)
    }
}