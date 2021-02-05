package dev.brella.khronus.api;

import net.minecraft.util.ITickable;

public interface IKhronusTickable extends ITickable {
    @Override
    default void update() {
        update(1, 0);
    }

    void update(int ticks, int bonusTicks);
}
