package dev.brella.khronus.overrides

import dev.brella.khronus.Khronus
import dev.brella.khronus.api.IKhronusTickable
import dev.brella.khronus.api.KhronusApi
import dev.brella.khronus.api.KhronusTickableProvider
import dev.brella.khronus.overrides.projecte.KhronusProjectE
import dev.brella.khronus.overrides.vanilla.KhronusVanilla
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KhronusOverrides {
    val overrides: MutableMap<ResourceLocation, IKhronusOverrides> = HashMap()

    inline fun addOverride(override: IKhronusOverrides) = overrides.put(override.key, override)

    fun loadOverrides() {
        addOverride(KhronusVanilla)

        if (Loader.isModLoaded("projecte")) {
            Khronus.logger.info("Loading ProjectE Overrides")
            addOverride(KhronusProjectE)
        }
    }

    @SubscribeEvent
    fun gatherCapabilities(event: AttachCapabilitiesEvent<TileEntity>) {
        if (event.capabilities.values.any { provider -> provider.hasCapability(KhronusApi.KHRONUS_TICKABLE, null) }) return
        else if (event.`object` is IKhronusTickable<*>) return

        overrides.forEach { (key, overrides) ->
            val capability = overrides.capabilityForTileEntity(event.`object`) ?: return@forEach

            event.addCapability(key, KhronusTickableProvider(capability))

            return
        }
    }
}