package dev.brella.khronus.overrides.projecte

import dev.brella.khronus.EmptyStorage
import dev.brella.khronus.Khronus.MOD_ID
import dev.brella.khronus.api.IKhronusTickable
import dev.brella.khronus.overrides.IKhronusOverrides
import moze_intel.projecte.api.item.IPedestalItem
import moze_intel.projecte.gameObjs.items.TimeWatch
import moze_intel.projecte.gameObjs.tiles.CollectorMK1Tile
import moze_intel.projecte.gameObjs.tiles.DMPedestalTile
import moze_intel.projecte.gameObjs.tiles.RelayMK1Tile
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KhronusProjectE : IKhronusOverrides {
    @CapabilityInject(IKhronusPedestalItem::class)
    @JvmField
    var KHRONUS_PEDESTAL_CAPABILITY: Capability<IKhronusPedestalItem>? = null

    override val key: ResourceLocation = ResourceLocation(MOD_ID, "projecte")

    @Suppress("UNCHECKED_CAST")
    override fun <T : TileEntity> capabilityForTileEntity(tileEntity: T): IKhronusTickable<T>? =
        when (tileEntity) {
            is DMPedestalTile -> KhronusTickablePedestal(tileEntity) as IKhronusTickable<T>
            is CollectorMK1Tile -> KhronusTickableCollector(tileEntity) as IKhronusTickable<T>
            is RelayMK1Tile -> KhronusTickableRelay(tileEntity) as IKhronusTickable<T>
            else -> null
        }

    @SubscribeEvent
    fun gatherCapabilities(event: AttachCapabilitiesEvent<ItemStack>) {
        val item = event.`object`.item

        when (item) {
            is TimeWatch -> event.addCapability(ResourceLocation(MOD_ID, "project_e_time_watch"),
                KhronusPedestalItemProvider(KhronusTimeWatch()))
        }
    }

    init {
        CapabilityManager.INSTANCE.register(IKhronusPedestalItem::class.java, EmptyStorage()) { null }

        MinecraftForge.EVENT_BUS.register(this)
    }
}