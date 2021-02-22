package dev.brella.khronus.api;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class KhronusTickableProvider<TE extends TileEntity> implements ICapabilityProvider {
    private IKhronusTickable<TE> tickable;

    public KhronusTickableProvider(Supplier<IKhronusTickable<TE>> supplier) {
        this.tickable = supplier.get();
    }

    public KhronusTickableProvider(IKhronusTickable<TE> tickable) {
        this.tickable = tickable;
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == KhronusApi.KHRONUS_TICKABLE;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == KhronusApi.KHRONUS_TICKABLE) return (T) tickable;
        return null;
    }
}
