package dev.brella.khronus.api;

import dev.brella.khronus.overrides.projecte.KhronusProjectE;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public class KhronusApi {
    @CapabilityInject(ILagTester.class)
    public static Capability<ILagTester> LAG_TESTING = null;

    @CapabilityInject(IKhronusTickable.class)
    public static Capability<IKhronusTickable> KHRONUS_TICKABLE = null;

    public static @NotNull Map<TileEntity, TemporalBounds> getDelayedTileEntities(World world) {
        throw new IllegalStateException("Khronus API not initialised yet!");
    }

    public static @NotNull Map<IKhronusTickable<?>, TemporalBounds> getKhronusTileEntities(World world) {
        throw new IllegalStateException("Khronus API not initialised yet!");
    }

    public static @NotNull Map<TileEntity, Integer> getTickAcceleration(World world) {
        throw new IllegalStateException("Khronus API not initialised yet!");
    }

    public static @NotNull Map<TileEntity, Long> getTickCheckup(World world) {
        throw new IllegalStateException("Khronus API not initialised yet!");
    }

    public static @NotNull Map<TileEntity, Long> getTickLength(World world) {
        throw new IllegalStateException("Khronus API not initialised yet!");
    }

    public static void addTickAccelerationTo(World world, TileEntity tile, int ticks) {
        getTickAcceleration(world).compute(tile, (key, existing) -> existing == null ? ticks : existing);
    }
}
