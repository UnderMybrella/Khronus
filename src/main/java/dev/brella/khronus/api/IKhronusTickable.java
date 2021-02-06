package dev.brella.khronus.api;

import net.minecraft.tileentity.ITickableTileEntity;

public interface IKhronusTickable extends ITickableTileEntity {
    @Override
    default void tick() {
        tick(1, 0);
    }

    void tick(int ticks, int bonusTicks);
}
