package dev.brella.khronus.api;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class KhronusApi {
    public static boolean isApiInitialised() {
        return false;
    }

    public static @NotNull Map<TileEntity, TemporalBounds> getKhronusTileEntities(World world) {
        throw new IllegalStateException("Api has not been initialised yet!");
    }

    public static @NotNull Map<TileEntity, Integer> getTickAcceleration(World world) {
        throw new IllegalStateException("Api has not been initialised yet!");
    }

    public static @NotNull Map<TileEntity, Long> getTickCheckup(World world) {
        throw new IllegalStateException("Api has not been initialised yet!");
    }

    public static @NotNull Map<TileEntity, Long> getTickLength(World world) {
        throw new IllegalStateException("Api has not been initialised yet!");
    }

    public static void addTickAccelerationTo(World world, TileEntity tile, int ticks) {
        getTickAcceleration(world).compute(tile, (key, existing) -> existing == null ? ticks : existing);
    }
}
