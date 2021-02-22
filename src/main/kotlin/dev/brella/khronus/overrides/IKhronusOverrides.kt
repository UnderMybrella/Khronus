package dev.brella.khronus.overrides

import dev.brella.khronus.api.IKhronusTickable
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation

interface IKhronusOverrides {
    val key: ResourceLocation

    fun <T: TileEntity> capabilityForTileEntity(tileEntity: T): IKhronusTickable<T>?
}