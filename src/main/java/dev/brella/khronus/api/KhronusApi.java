package dev.brella.khronus.api;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public class KhronusApi {
    public static @NotNull Map<TileEntity, TemporalBounds> getKhronusTileEntities(World world) {
        return Collections.emptyMap();
    }

    public static @NotNull Map<TileEntity, Integer> getTickAcceleration(World world) {
        return Collections.emptyMap();
    }

    public static @NotNull Map<TileEntity, Long> getTickCheckup(World world) {
        return Collections.emptyMap();
    }

    public static @NotNull Map<TileEntity, Long> getTickLength(World world) {
        return Collections.emptyMap();
    }

    public static void addTickAccelerationTo(World world, TileEntity tile, int ticks) {
        getTickAcceleration(world).compute(tile, (key, existing) -> existing == null ? ticks : existing);
    }
}
