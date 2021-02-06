package dev.brella.khronus.overrides.vanilla

import dev.brella.khronus.overrides.vanilla.blocks.KhronusFurnaceBlock
import net.minecraft.block.AbstractBlock
import net.minecraft.block.material.Material
import net.minecraft.state.properties.BlockStateProperties
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries

object KhronusVanilla {
    private val BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "minecraft")

    val blockFurnace =
        BLOCKS.register("furnace") {
            KhronusFurnaceBlock(AbstractBlock.Properties.create(Material.ROCK)
                .setRequiresTool()
                .hardnessAndResistance(3.5f)
                .setLightLevel { state -> if (state.get(BlockStateProperties.LIT)) 13 else 0 }
            )
        }

    fun register() {
        BLOCKS.register(thedarkcolour.kotlinforforge.forge.MOD_CONTEXT.getKEventBus())
    }
}