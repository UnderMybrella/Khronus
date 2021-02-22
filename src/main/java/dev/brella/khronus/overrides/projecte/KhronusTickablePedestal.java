package dev.brella.khronus.overrides.projecte;

import dev.brella.khronus.api.IKhronusTickable;
import moze_intel.projecte.api.item.IPedestalItem;
import moze_intel.projecte.gameObjs.tiles.DMPedestalTile;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

public class KhronusTickablePedestal implements IKhronusTickable<DMPedestalTile> {
    public static Field PARTICLE_COOLDOWN_FIELD;
    public static Method SPAWN_PARTICLES_METHOD;

    public static MethodHandle SPAWN_PARTICLES_METHOD_HANDLE;
    public static MethodHandle PARTICLE_COOLDOWN_GETTER;
    public static MethodHandle PARTICLE_COOLDOWN_SETTER;

    static {
        try {
            PARTICLE_COOLDOWN_FIELD = DMPedestalTile.class.getDeclaredField("particleCooldown");
            SPAWN_PARTICLES_METHOD = DMPedestalTile.class.getDeclaredMethod("spawnParticles");

            PARTICLE_COOLDOWN_FIELD.setAccessible(true);
            SPAWN_PARTICLES_METHOD.setAccessible(true);

            MethodHandles.Lookup lookup = MethodHandles.lookup();

            SPAWN_PARTICLES_METHOD_HANDLE = lookup.unreflect(SPAWN_PARTICLES_METHOD);
            PARTICLE_COOLDOWN_GETTER = lookup.unreflectGetter(PARTICLE_COOLDOWN_FIELD);
            PARTICLE_COOLDOWN_SETTER = lookup.unreflectSetter(PARTICLE_COOLDOWN_FIELD);
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    public DMPedestalTile base;

    public KhronusTickablePedestal(DMPedestalTile base) {
        this.base = base;
    }

    @Override
    public void update(int ticks, int bonusTicks) {
        base.centeredX = base.getPos().getX() + 0.5;
        base.centeredY = base.getPos().getY() + 0.5;
        base.centeredZ = base.getPos().getZ() + 0.5;

        if (base.getActive()) {
            ItemStack stack = base.getInventory().getStackInSlot(0);
            if (!stack.isEmpty()) {
                if (stack.hasCapability(KhronusProjectE.KHRONUS_PEDESTAL_CAPABILITY, null)) {
                    Objects.requireNonNull(stack.getCapability(KhronusProjectE.KHRONUS_PEDESTAL_CAPABILITY, null))
                            .updateInPedestal(base.getWorld(), base.getPos(), ticks, bonusTicks);
                } else {
                    Item item = stack.getItem();
                    if (item instanceof IPedestalItem) {
                        ((IPedestalItem) item).updateInPedestal(base.getWorld(), base.getPos());
                    }
                }

                try {
                    int particleCooldown = (int) PARTICLE_COOLDOWN_GETTER.invokeExact(base);

                    if (particleCooldown <= 0) {
                        SPAWN_PARTICLES_METHOD_HANDLE.invokeExact(base);
                        particleCooldown = 10;
                    } else {
                        particleCooldown--;
                    }

                    PARTICLE_COOLDOWN_SETTER.invokeExact(base, particleCooldown);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            } else {
                base.setActive(false);
            }
        }
    }

    @Override
    public DMPedestalTile getSource() {
        return base;
    }
}
