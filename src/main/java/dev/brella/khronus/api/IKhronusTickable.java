package dev.brella.khronus.api;

import dev.brella.khronus.watchdogs.KhronusWatchdog;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;

public interface IKhronusTickable<T extends TileEntity> extends ITickable {
    @Override
    default void update() {
        update(1, 0);
    }

    void update(int ticks, int bonusTicks);

    T getSource();

    default void onAddedTo(World world) {}
    default void onRemovedFrom(World world) {}

    default void onWatchdogChanged(World world, KhronusWatchdog oldWatchdog, KhronusWatchdog newWatchdog) {}
}
