package dev.brella.khronus.overrides.projecte

import dev.brella.khronus.api.KhronusApi
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import java.util.function.Supplier

class KhronusPedestalItemProvider : ICapabilityProvider {
    private var pedestal: IKhronusPedestalItem

    constructor(supplier: Supplier<IKhronusPedestalItem>) {
        pedestal = supplier.get()
    }

    constructor(pedestal: IKhronusPedestalItem) {
        this.pedestal = pedestal
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean {
        return capability == KhronusProjectE.KHRONUS_PEDESTAL_CAPABILITY
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
        return if (capability == KhronusProjectE.KHRONUS_PEDESTAL_CAPABILITY) pedestal as T? else null
    }
}